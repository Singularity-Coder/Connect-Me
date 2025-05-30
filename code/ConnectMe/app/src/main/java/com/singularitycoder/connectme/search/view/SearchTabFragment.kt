package com.singularitycoder.connectme.search.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.*
import android.util.Base64
import android.view.*
import android.webkit.*
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentSearchTabBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.Preferences
import com.singularitycoder.connectme.helpers.constants.SearchEngine
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

// Lots of references from https://github.com/LineageOS/android_packages_apps_Jelly

private const val ARG_PEEK_URL = "ARG_PEEK_URL"
private const val ARG_TAB_URL = "ARG_TAB_URL"

@AndroidEntryPoint
class SearchTabFragment : Fragment() {

    companion object {
        private const val DESKTOP_DEVICE = "X11; Linux x86_64"
        private const val DESKTOP_USER_AGENT_FALLBACK = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"
        private const val HEADER_DNT = "DNT"
        private const val ERROR_PAGE_TITLE = "Something went wrong, try again or try something different!"
        private const val DEFAULT_ERROR_PAGE_PATH = "file:///android_res/raw/error_webpage.html"

        @JvmStatic
        fun newInstance(
            peekUrl: String? = null,
            tabUrl: String? = null,
        ) = SearchTabFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PEEK_URL, peekUrl)
                putString(ARG_TAB_URL, tabUrl)
            }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSearchTabBinding

    private val requestHeadersMap: MutableMap<String?, String?> = HashMap()
    private val hideProgressHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var progressListener: (progress: Int) -> Unit = {}
    private var webViewListener: (webView: WebView?) -> Unit = {}

    private var peekUrl: String? = null
    private var tabUrl: String? = null
    private var mobileUserAgent: String? = null
    private var hideProgressRunnable = Runnable {}
    private var desktopUserAgent: String? = null
    private var isIncognito = false
    private var lastLoadedUrl: String? = null
    private var searchFragment: SearchFragment? = null
    private var favicon: Bitmap? = null
    private var xTouchPos = 0F
    private var yTouchPos = 0F
    private var menuDummyView: View? = null
    private var customViewForWeb: View? = null
    private var fullScreenCallback: WebChromeClient.CustomViewCallback? = null

    var isWebpageLoadedAtLeastOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        peekUrl = arguments?.getString(ARG_PEEK_URL)
        tabUrl = arguments?.getString(ARG_TAB_URL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.webView.webChromeClient = null
        }
        binding.webView.webViewClient = WebViewClient()
    }

    // https://guides.codepath.com/android/Working-with-the-WebView
    @SuppressLint("SetJavaScriptEnabled")
    private fun FragmentSearchTabBinding.setupUI() {
        searchFragment = getSearchFragment()
        ConnectMeUtils.webpageFragmentIdList.add(tag)
        searchFragment?.doOnTabClick()
        setupWebView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun FragmentSearchTabBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        // https://stackoverflow.com/questions/14752523/how-to-make-a-scroll-listener-for-webview-in-android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
                btnScrollToTop.isVisible = scrollY > 200

                if (oldScrollY < scrollY) {
                    // Scrolling Upwards
                    searchFragment?.showUrlSearchBar(false)
                }

                if (oldScrollY > scrollY) {
                    // Scrolling Downwards
                    searchFragment?.showUrlSearchBar(true)
                }
            }
        }

        btnScrollToTop.onSafeClick {
//            (webView.scrollY downTo 0).forEach {
//                webView.scrollTo(0, it) // Does not smooth scroll. Adding delay doesnt work
//            }
            webView.scrollTo(0, 0)
        }

        webView.setOnTouchListener(View.OnTouchListener { view, event ->
            xTouchPos = event.x
            yTouchPos = event.y
//            when(event.action) {
//                MotionEvent.ACTION_DOWN -> Unit
//                MotionEvent.ACTION_MOVE -> Unit
//                MotionEvent.ACTION_UP -> Unit
//            }
//            view.performClick()
            return@OnTouchListener false
        })

        // FIXME some images do not give extra
        webView.onCustomLongClick {
            val result = webView.hitTestResult
            result.extra ?: return@onCustomLongClick
            when (result.type) {
                WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    showWebViewLongClickMenu()
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    showWebViewLongClickMenu()
                }
            }
        }

        webView.setDownloadListener { url: String?, _, contentDisposition: String?, mimeType: String?, _ ->
//            downloadFileAsk(url, contentDisposition, mimeType)
        }
    }

    private fun showWebViewLongClickMenu() {
        /** Create a dummy view at the touch coordinate and pass that dummy view to the menu.
         * Setting min width and height are necessary for menu to inflate at exact position at corners. */
        menuDummyView = View(requireContext()).apply {
            layoutParams = binding.llDummyViewGroup.layoutParams
            layoutParams.height = 1
            layoutParams.width = 1
            this.x = xTouchPos
            this.y = yTouchPos
        }
        binding.llDummyViewGroup.addView(menuDummyView)
        binding.showWebViewPopupMenu(view = menuDummyView ?: return, menuRes = R.menu.webview_long_click_popup_menu)
    }

    fun loadUrl(url: String) {
        lastLoadedUrl = peekUrl ?: tabUrl ?: url
        followUrl(lastLoadedUrl ?: "")
    }

    private fun followUrl(url: String) {
        var fixedUrl = smartUrlFilter(url)
        if (fixedUrl != null) {
            binding.webView.loadUrl(fixedUrl, requestHeadersMap)
            return
        }
        val selectedSearchEngine = preferences.getString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.GOOGLE.name)
        val searchEngineEnum = SearchEngine.valueOf(selectedSearchEngine ?: SearchEngine.GOOGLE.name)
        fixedUrl = getFormattedUri(templateUri = searchEngineEnum.url, query = url)
        binding.webView.loadUrl(fixedUrl, requestHeadersMap)
    }

    private fun FragmentSearchTabBinding.setupWebView() {
        webView.webChromeClient = setupWebChromeClient()
        webView.webViewClient = setupWebViewClient()
        webView.addJavascriptInterface(
            /* object = */ SimpleWebJavascriptInterface(),
            /* name = */ "Android"
        )
//        webView.clearHistory()
//        webView.clearCache(/* includeDiskFiles = */ true)
//        webView.clearFormData()
//        webView.clearMatches()
//        webView.clearSslPreferences()
        webView.settings.apply {
            loadsImagesAutomatically = true
            javaScriptEnabled = preferences.getBoolean(Preferences.KEY_ENABLE_JAVASCRIPT, true)
            javaScriptCanOpenWindowsAutomatically = preferences.getBoolean(Preferences.KEY_ENABLE_JAVASCRIPT_OPEN_WINDOWS_AUTO, true)
            setSupportMultipleWindows(true)
//            useWideViewPort = true
//            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true // allow pinch to zooom
            displayZoomControls = false  // disable the default zoom controls on the page
            databaseEnabled = isIncognito.not()
            domStorageEnabled = isIncognito.not()

            /** Need these to load custom error page from raw res */
            allowContentAccess = true
            allowFileAccess = true
        }

        // Mobile: Remove "wv" from the WebView's user agent. Some websites don't work
        // properly if the browser reports itself as a simple WebView.
        // Desktop: Generate the desktop user agent starting from the mobile one so that
        // we always report the current engine version.
        val pattern = Pattern.compile("([^)]+ \\()([^)]+)(\\) .*)")
        val matcher = pattern.matcher(webView.settings.userAgentString)
        if (matcher.matches()) {
            val mobileDevice = matcher.group(2)?.toString()?.replace("; wv", "")
            mobileUserAgent = matcher.group(1)?.toString() + mobileDevice + matcher.group(3)
            desktopUserAgent = matcher.group(1)?.toString() + DESKTOP_DEVICE + matcher.group(3)?.replace(" Mobile ", " ")
            webView.settings.userAgentString = mobileUserAgent
        } else {
            println("Couldn't parse the user agent")
            mobileUserAgent = webView.settings.userAgentString
            desktopUserAgent = DESKTOP_USER_AGENT_FALLBACK
        }
        if (preferences.getBoolean(Preferences.KEY_DO_NOT_TRACK, false)) {
            requestHeadersMap[HEADER_DNT] = "1"
        }

        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        if (peekUrl != null || tabUrl != null) {
            loadUrl(url = peekUrl ?: tabUrl ?: "")
        }
    }

    private fun setupWebChromeClient() = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            searchFragment?.setLinearProgress(progress)
            progressListener.invoke(progress)
            if (progress == 100) {
                isWebpageLoadedAtLeastOnce = true
                searchFragment?.doOnWebPageLoaded()
                hideProgressHandler.removeCallbacks(hideProgressRunnable)
                hideProgressRunnable = Runnable { searchFragment?.showLinearProgress(false) }
                hideProgressHandler.postDelayed(hideProgressRunnable, 1.seconds())
                if (view.title?.contains(other = ERROR_PAGE_TITLE, ignoreCase = true) == true) {
                    searchFragment?.setWebsiteProfileLayoutVisibility(isVisible = false)
                }
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
        }

        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            if (icon == null || icon.isRecycled) return
            favicon = icon.copy(icon.config, true)
//            applyThemeColor(UiUtils.getColor(favicon, binding.webView.isIncognito))
            if (icon.isRecycled.not()) icon.recycle()
            searchFragment?.getFaviconImageView()?.setImageBitmap(favicon)
            searchFragment?.setWebViewData()
            webViewListener.invoke(view)
        }

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        /** [onShowCustomView] is for full screen stuff. https://stackoverflow.com/questions/62573334/android-webview-fullscreen-on-videos-not-working */
        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            if (customViewForWeb != null) {
                callback?.onCustomViewHidden()
                return
            }
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            customViewForWeb = view
            fullScreenCallback = callback
            requireActivity().setImmersiveMode(true)
            customViewForWeb?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            requireActivity().addContentView(
                customViewForWeb, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            binding.webView.visibility = View.GONE
        }

        override fun onHideCustomView() {
            if (customViewForWeb == null) return
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            requireActivity().setImmersiveMode(false)
            binding.webView.visibility = View.VISIBLE
            val viewGroup = customViewForWeb?.parent as? ViewGroup
            viewGroup?.removeView(customViewForWeb)
            fullScreenCallback?.onCustomViewHidden()
            fullScreenCallback = null
            customViewForWeb = null
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }
    }

    private fun setupWebViewClient() = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            searchFragment?.showLinearProgress(true)
        }

        override fun onPageCommitVisible(view: WebView, url: String) {
            super.onPageCommitVisible(view, url)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            // https://www.androprogrammer.com/2018/09/load-custom-error-page-in-android.html
            // https://github.com/WasimMemon/Myapplications/blob/master/Tutorials/app/src/main/java/com/androprogrammer/tutorials/samples/WebViewCustomizationDemo.java
            val message = when (error?.primaryError) {
                SslError.SSL_UNTRUSTED -> "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> "The certificate has expired."
                SslError.SSL_IDMISMATCH -> "The certificate Hostname mismatch."
                SslError.SSL_INVALID -> "SSL connection is invalid."
                else -> ""
            }
            if (message.isBlank()) return
            requireContext().showAlertDialog(message = message, positiveBtnText = "Okay")
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            // https://github.com/arunkumar9t2/lynket-browser
            // https://stackoverflow.com/questions/3961589/android-webview-and-loaddata
            if (request?.isForMainFrame == true && error != null) {
                view?.settings?.defaultTextEncodingName = "utf-8"
                view?.loadData(
                    /* data = */ Base64.encodeToString(getString(R.string.error_webpage).toByteArray(), Base64.DEFAULT),
                    /* mimeType = */ "text/html; charset=utf-8",
                    /* encoding = */ "base64"
                )
            }
        }
    }

    fun getWebView(): WebView = binding.webView

    fun getFavicon(): Bitmap? = favicon

    private fun FragmentSearchTabBinding.showWebViewPopupMenu(
        view: View?,
        @MenuRes menuRes: Int
    ) {
        view ?: return
        val popupMenu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            android.widget.PopupMenu(
                /* context = */ requireContext(),
                /* anchor = */ view,
                /* gravity = */ 0,
                /* popupStyleAttr = */ 0,
                /* popupStyleRes = */ R.style.PopupMenuTheme
            )
        } else {
            android.widget.PopupMenu(
                /* context = */ requireContext(),
                /* anchor = */ view
            )
        }
        popupMenu.apply {
            this.menu.invokeSetMenuIconMethod()
            menuInflater.inflate(menuRes, this.menu)
//            this.menu.removeItemAt(3)
//            this.menu.findItem(R.id.menu_item_new_private_disappearing_tab).isVisible = false
            MenuCompat.setGroupDividerEnabled(this.menu, true)
        }
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_peek -> {
                }
                R.id.menu_item_open_new_tab -> {
//            openInNewTab(requireContext(), url, isIncognito)
                }
                R.id.menu_item_open_new_private_tab -> {
                }
                R.id.menu_item_open_new_disappearing_tab -> {
                }
                R.id.menu_item_copy_link_address -> {
                    if (webView.hitTestResult.extra.isNullOrBlank().not()) {
                        requireContext().clipboard()?.text = webView.hitTestResult.extra
                        binding.root.showSnackBar("Copied: ${requireContext().clipboard()?.text}")
                    }
                }
                R.id.menu_item_copy_link_text -> {
                }
                R.id.menu_item_add_to_collection -> {
                    addToCollection(title = webView.hitTestResult.extra, url = webView.hitTestResult.extra)
                }
                R.id.menu_item_qr_code -> {
                }
                R.id.menu_item_save_offline -> {
                    downloadFileAsk(url = webView.hitTestResult.extra, contentDisposition = null, mimeType = null)
                }
                R.id.menu_item_share_link -> {
                    requireContext().shareUrl(url = webView.hitTestResult.extra, webView = webView)
                }
                else -> Unit
            }
            root.removeView(menuDummyView)
            false
        }
        popupMenu.setOnDismissListener {
            root.removeView(menuDummyView)
        }
        popupMenu.menu.setMarginBtwMenuIconAndText(
            context = requireContext(),
            iconMarginDp = 10
        )
        popupMenu.menu.forEach { it: MenuItem ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.iconTintList = ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
            }
        }
        popupMenu.show()
    }

    private fun downloadFileAsk(url: String?, contentDisposition: String?, mimeType: String?) {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request: DownloadManager.Request = try {
            DownloadManager.Request(Uri.parse(url))
        } catch (e: IllegalArgumentException) {
            println("Cannot download non http or https scheme")
            return
        }

        // Let this downloaded file be scanned by MediaScanner - so that it can
        // show up in Gallery app, for example.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            request.allowScanningByMediaScanner()
        }
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setMimeType(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(url)
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireContext().getSystemService(DownloadManager::class.java).enqueue(request)
        }
    }

    private fun addToCollection(title: String?, url: String?) {
        if (title == null || url == null) return
        // TODO add to db
    }

    private fun getSearchFragment(): SearchFragment? {
        if (peekUrl != null) return null
        return parentFragmentManager.fragments.firstOrNull {
            it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
        } as? SearchFragment
    }

    fun getWebViewProgressListener(progressListener: (progress: Int) -> Unit) {
        this.progressListener = progressListener
    }

    fun getWebViewListener(webViewListener: (webView: WebView?) -> Unit) {
        this.webViewListener = webViewListener
    }

    fun clearCache() {
        binding.webView.clearCache(true)
        binding.webView.clearSslPreferences()
        binding.webView.clearMatches()
        binding.webView.clearHistory()
        binding.webView.clearFormData()
        binding.webView.clearAnimation()
        binding.webView.clearChildFocus(null)
        binding.webView.clearDisappearingChildren()
        binding.webView.clearFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.webView.clearViewTranslationCallback()
        }
    }

    class SimpleWebJavascriptInterface {
        @JavascriptInterface
        fun reloadWebPage() {
        }
    }
}