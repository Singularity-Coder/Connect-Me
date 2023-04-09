package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.singularitycoder.connectme.databinding.FragmentSearchTabBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.Preferences
import com.singularitycoder.connectme.helpers.constants.SearchEngine
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

//private const val ARG_PARAM_TAB = "ARG_PARAM_TAB"

@AndroidEntryPoint
class SearchTabFragment : Fragment() {

    companion object {
        private const val DESKTOP_DEVICE = "X11; Linux x86_64"
        private const val DESKTOP_USER_AGENT_FALLBACK = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"
        private const val HEADER_DNT = "DNT"

        @JvmStatic
        fun newInstance(paramTab: String) = SearchTabFragment().apply {
//            arguments = Bundle().apply { putString(ARG_PARAM_TAB, paramTab) }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSearchTabBinding

    private val requestHeadersMap: MutableMap<String?, String?> = HashMap()
    private val hideProgressHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var hideProgressRunnable = Runnable {}
//    private var paramTab: String? = null
    private var mobileUserAgent: String? = null
    private var desktopUserAgent: String? = null
    private var isIncognito = false
    private var lastLoadedUrl: String? = null
    private var searchFragment: SearchFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        paramTab = arguments?.getString(ARG_PARAM_TAB)
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

    // https://guides.codepath.com/android/Working-with-the-WebView
    @SuppressLint("SetJavaScriptEnabled")
    private fun FragmentSearchTabBinding.setupUI() {
        ConnectMeUtils.webpageIdList.add(tag)
        searchFragment = requireActivity().supportFragmentManager.fragments.firstOrNull {
            it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
        } as? SearchFragment
        setupWebView()
    }

    private fun FragmentSearchTabBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        var shouldAllowDownload = false
        webView.setOnLongClickListener {
            val result = webView.hitTestResult
            result.extra ?: return@setOnLongClickListener false
            when (result.type) {
                WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    shouldAllowDownload = true
//                    showSheetMenu(it, shouldAllowDownload)
                    shouldAllowDownload = false
                    return@setOnLongClickListener true
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
//                    showSheetMenu(it, shouldAllowDownload)
                    shouldAllowDownload = false
                    return@setOnLongClickListener true
                }
            }
            false
        }

        webView.setDownloadListener { url: String?, _, contentDisposition: String?, mimeType: String?, _ ->
//            downloadFileAsk(url, contentDisposition, mimeType)
        }
    }

    fun loadUrl(url: String) {
        lastLoadedUrl = url
        followUrl(url)
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
        webView.webChromeClient = setupWebChromeClient(searchFragment)
        webView.webViewClient = setupWebViewClient(searchFragment)
        webView.settings.apply {
            loadsImagesAutomatically = true
            javaScriptEnabled = preferences.getBoolean(Preferences.KEY_ENABLE_JAVASCRIPT, true)
            javaScriptCanOpenWindowsAutomatically = preferences.getBoolean(Preferences.KEY_ENABLE_JAVASCRIPT_OPEN_WINDOWS_AUTO, true)
            setSupportMultipleWindows(true)
//            useWideViewPort = true
//            loadWithOverviewMode = true
//            setSupportZoom(true)
            builtInZoomControls = true // allow pinch to zooom
            displayZoomControls = false  // disable the default zoom controls on the page
            databaseEnabled = isIncognito.not()
            domStorageEnabled = isIncognito.not()
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
    }

    private fun setupWebChromeClient(searchFragment: SearchFragment?) = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            searchFragment?.setLinearProgress(progress)
            if (progress == 100) {
                hideProgressHandler.removeCallbacks(hideProgressRunnable)
                hideProgressRunnable = Runnable { searchFragment?.showLinearProgress(false) }
                hideProgressHandler.postDelayed(hideProgressRunnable, 1.seconds())
//                    doAfter(1.seconds()) {
//                        searchFragment?.showLinearProgress(false)
//                    }
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
        }

        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            if (icon == null || icon.isRecycled) return
            val favicon = icon.copy(icon.config, true)
//            applyThemeColor(UiUtils.getColor(favicon, binding.webView.isIncognito))
            if (icon.isRecycled.not()) icon.recycle()
            searchFragment?.getFaviconImageView()?.setImageBitmap(favicon)
//            super.onReceivedIcon(view, icon)
        }

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }
    }

    private fun setupWebViewClient(searchFragment: SearchFragment?) = object : WebViewClient() {
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
    }

    private fun setDesktopMode(isDesktopMode: Boolean) {
        binding.webView.settings.apply {
            userAgentString = if (isDesktopMode) desktopUserAgent else mobileUserAgent
            useWideViewPort = isDesktopMode
            loadWithOverviewMode = isDesktopMode
        }
        binding.webView.reload()
    }

    fun getWebView(): WebView = binding.webView

    fun refreshWebpage() {
        binding.webView.reload()
    }
}