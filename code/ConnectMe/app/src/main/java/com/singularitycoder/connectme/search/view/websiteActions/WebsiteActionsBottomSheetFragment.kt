package com.singularitycoder.connectme.search.view.websiteActions

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.http.SslCertificate
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentWebsiteActionsBottomSheetBinding
import com.singularitycoder.connectme.feed.RssFollowWorker
import com.singularitycoder.connectme.followingWebsite.FollowingWebsite
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteViewModel
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.WorkerData
import com.singularitycoder.connectme.helpers.constants.WorkerTag
import com.singularitycoder.connectme.search.model.ApiResult
import com.singularitycoder.connectme.search.model.ApiState
import com.singularitycoder.connectme.search.model.WebViewData
import com.singularitycoder.connectme.search.view.SearchFragment
import com.singularitycoder.connectme.search.view.SearchTabFragment
import com.singularitycoder.connectme.search.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat

@AndroidEntryPoint
class WebsiteActionsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = WebsiteActionsBottomSheetFragment()
    }

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val followingWebsiteViewModel by activityViewModels<FollowingWebsiteViewModel>()

    private lateinit var binding: FragmentWebsiteActionsBottomSheetBinding

    private var searchFragment: SearchFragment? = null
    private var webViewData: WebViewData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWebsiteActionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    // https://stackoverflow.com/questions/15543186/how-do-i-create-colorstatelist-programmatically
    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentWebsiteActionsBottomSheetBinding.setupUI() {
        searchFragment = activity?.supportFragmentManager?.fragments?.firstOrNull {
            it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
        } as? SearchFragment
        setTransparentBackground()
        setBottomSheetBehaviour()

        val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(
            ConnectMeUtils.webpageIdList[searchFragment?.getTabsTabLayout()?.selectedTabPosition ?: 0]
        ) as? SearchTabFragment
        val sslCertificate = selectedWebpage?.getWebView()?.certificate
        ivSiteIcon.setImageBitmap(selectedWebpage?.getFavicon())
        tvSiteName.text = getHostFrom(selectedWebpage?.getWebView()?.url)
        tvLink.text = selectedWebpage?.getWebView()?.title
        binding.setupWebsiteSecurity(sslCertificate)
        lifecycleScope.launch {
            val isPresent = followingWebsiteViewModel.isItemPresent(getHostFrom(url = selectedWebpage?.getWebView()?.url))
            withContext(Main) {
                if (isPresent) {
                    setButtonStyleToFollowing()
                } else {
                    setButtonStyleToFollow()
                }
            }
        }

        itemHistory.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.round_history_24))
            tvTitle.text = "History"
            tvSubtitle.text = "Last visited on 18 April 2093"
            ivArrow.isVisible = true
        }
        itemDownloads.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_file_download_24))
            tvTitle.text = "Downloads"
            tvSubtitle.text = "Last downloaded on 18 April 2093"
            ivArrow.isVisible = true
        }
        itemPermissions.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.round_tune_24))
            tvTitle.text = "Permissions"
            tvSubtitle.text = "Notifications blocked"
            ivArrow.isVisible = true
        }
        itemClearCookies.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_cookie_24))
            tvTitle.text = "Clear Cookies"
            tvSubtitle.text = "114 cookies stored"
        }
        itemClearCache.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.round_storage_24))
            tvTitle.text = "Clear Cache"
            tvSubtitle.text = "41 MB stored data"
        }
        itemDesktopSite.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_desktop_windows_24))
            tvTitle.text = "Request desktop site"
            tvSubtitle.text = "Shows you desktop version"
            switchOnOff.isVisible = true
        }
        itemAdBlocker.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_block_24))
            tvTitle.text = "Block Ads"
            tvSubtitle.text = "Blocks all Ads and Trackers"
            switchOnOff.isVisible = true
        }
        itemReadingMode.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_chrome_reader_mode_24))
            tvTitle.text = "Reading Mode"
            tvSubtitle.text = "Distraction free reading"
            switchOnOff.isVisible = true
        }
        itemVpn.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_vpn_key_24))
            tvTitle.text = "VPN"
            tvSubtitle.text = "Safe & private browsing"
            switchOnOff.isVisible = true
        }
        itemBlockCookiePopups.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.remove_circle_outline_black_24dp))
            tvTitle.text = "Block cookie popups"
            tvSubtitle.text = "Blocks cookie consent popups."
            switchOnOff.isVisible = true
        }
    }

    private fun FragmentWebsiteActionsBottomSheetBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        btnFollow.onSafeClick {
            lifecycleScope.launch {
                val followingWebsite = FollowingWebsite(
                    favicon = encodeBitmapToBase64String(webViewData?.favIcon),
                    title = webViewData?.title,
                    time = timeNow,
                    website = getHostFrom(url = webViewData?.url),
                    link = webViewData?.url ?: "",
                    postCount = 0,
                    rssUrl = ""
                )
                val isPresent = followingWebsiteViewModel.isItemPresent(getHostFrom(url = webViewData?.url))
                withContext(Main) {
                    if (isPresent) {
                        setButtonStyleToFollow()
                        followingWebsiteViewModel.deleteItem(followingWebsite)
                    } else {
                        setButtonStyleToFollowing()
                        followingWebsiteViewModel.addFollowingWebsite(followingWebsite)
                        parseRssFollowFromWorker("https://www.theverge.com/rss/index.xml")
//                        parseRssFeedFromWorker("https://mashable.com/feeds/rss/all")
                        searchViewModel.getTextInsight(
                            prompt = """
                                 What is the rss link for ${getHostFrom(url = webViewData?.url)}.
                                 Put the link in this format: \"\"\"rss_link\"\"\"
                            """.trimIndentsAndNewLines(),
                            isSaveToDb = false,
                            isSendResponse = false,
                            screen = this@WebsiteActionsBottomSheetFragment.javaClass.simpleName
                        )
                    }
                }
            }
        }

        cardSiteSecurity.onSafeClick {
            llSslCertificateDetails.isVisible = llSslCertificateDetails.isVisible.not()
            ivSiteSecurityArrow.setImageDrawable(
                requireContext().drawable(
                    if (llSslCertificateDetails.isVisible) {
                        R.drawable.round_keyboard_arrow_up_24
                    } else {
                        R.drawable.round_keyboard_arrow_down_24
                    }
                )
            )
        }

        itemHistory.root.onSafeClick { }

        itemDownloads.root.onSafeClick { }

        itemPermissions.root.onSafeClick { }

        itemClearCookies.root.onSafeClick { }

        itemClearCache.root.onSafeClick { }

//        btnMenu.onSafeClick {
//            val optionsList = listOf("Close")
//            requireContext().showPopup(
//                view = it.first,
//                menuList = optionsList
//            ) { menuPosition: Int ->
//                when (optionsList[menuPosition]) {
//                    optionsList[0] -> dismiss()
//                }
//            }
//        }

        itemDesktopSite.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        itemAdBlocker.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        itemReadingMode.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        itemVpn.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        itemBlockCookiePopups.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentWebsiteActionsBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = searchViewModel.webViewDataStateFlow) { it: WebViewData ->
            this@WebsiteActionsBottomSheetFragment.webViewData = it
            ivSiteIcon.setImageBitmap(it.favIcon)
            tvSiteName.text = getHostFrom(it.url)
            tvLink.text = it.title
            binding.setupWebsiteSecurity(sslCertificate = it.certificate)
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = searchViewModel.insightSharedFlow) { it: ApiResult ->
            if (it.apiState == ApiState.NONE) return@collectLatestLifecycleFlow
            if (it.screen != this@WebsiteActionsBottomSheetFragment.javaClass.simpleName) return@collectLatestLifecycleFlow

            when (it.apiState) {
                ApiState.SUCCESS -> {
                    val rssUrl = if (it.insight?.insight?.contains("\"\"\"") == true) {
                        it.insight.insight.substringAfter("\"\"\"").substringBefore("\"\"\"")
                    } else ""
                    val isValidRssUrl = rssUrl.isNotBlank() && rssUrl.contains("http")
                    if (isValidRssUrl) parseRssFollowFromWorker(rssUrl)
                }
                ApiState.ERROR -> {
                    parseRssFollowFromWorker(rssUrl = "${getHostFrom(url = webViewData?.url)}feed")
                    // TODO try default rss urls
                    // Provide field to enter url manually. if that also fa
                    // Inform user open ai api failed. if key issue. else ignore
                }
                else -> Unit
            }

            searchViewModel.resetInsight()
        }
    }

    private fun parseRssFollowFromWorker(rssUrl: String?) {
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder().apply {
            putString(WorkerData.RSS_URL, rssUrl)
        }.build()
        val workRequest = OneTimeWorkRequestBuilder<RssFollowWorker>()
            .setInputData(data)
            .setConstraints(workConstraints)
            .build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(WorkerTag.RSS_FOLLOW_PARSER, ExistingWorkPolicy.KEEP, workRequest)
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(workRequest.id).observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> println("RUNNING: show Progress")
                WorkInfo.State.ENQUEUED -> println("ENQUEUED: show Progress")
                WorkInfo.State.SUCCEEDED -> {
                    println("SUCCEEDED: stop Progress")
                    // TODO show manual rss url field
                }
                WorkInfo.State.FAILED -> println("FAILED: stop showing Progress")
                WorkInfo.State.BLOCKED -> println("BLOCKED: show Progress")
                WorkInfo.State.CANCELLED -> println("CANCELLED: stop showing Progress")
                else -> Unit
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentWebsiteActionsBottomSheetBinding.setupWebsiteSecurity(sslCertificate: SslCertificate?) {
        if (sslCertificate == null) {
            tvSiteSecurity.text = "Connection is insecure"
            tvSiteSecurity.setTextColor(requireContext().color(R.color.md_red_500))
            llSslCertificateDetails.isVisible = false
            ivSiteSecurityArrow.isVisible = false
            return
        }

        tvSiteSecurity.text = "Connection is secure"
        tvSiteSecurity.setTextColor(requireContext().color(R.color.md_green_500))

        tvIssuedToCn.isVisible = sslCertificate.issuedTo?.cName.isNullOrBlank().not()
        tvIssuedToO.isVisible = sslCertificate.issuedTo?.oName.isNullOrBlank().not()
        tvIssuedToUn.isVisible = sslCertificate.issuedTo?.uName.isNullOrBlank().not()
        tvIssuedByCn.isVisible = sslCertificate.issuedBy?.cName.isNullOrBlank().not()
        tvIssuedByO.isVisible = sslCertificate.issuedBy?.oName.isNullOrBlank().not()
        tvIssuedByUn.isVisible = sslCertificate.issuedBy?.uName.isNullOrBlank().not()
        tvIssuedOn.isVisible = sslCertificate.validNotBeforeDate != null
        tvExpiresOn.isVisible = sslCertificate.validNotAfterDate != null

        tvIssuedToPlaceholder.isVisible = tvIssuedToCn.isVisible || tvIssuedToO.isVisible || tvIssuedToUn.isVisible
        tvIssuedByPlaceholder.isVisible = tvIssuedByCn.isVisible || tvIssuedByO.isVisible || tvIssuedByUn.isVisible
        tvValidityPeriodPlaceholder.isVisible = tvIssuedOn.isVisible || tvExpiresOn.isVisible

        tvIssuedToCn.text = "${getString(R.string.ssl_cert_dialog_common_name)}: ${sslCertificate.issuedTo?.cName}"
        tvIssuedToO.text = "${getString(R.string.ssl_cert_dialog_organization)}: ${sslCertificate.issuedTo?.oName}"
        tvIssuedToUn.text = "${getString(R.string.ssl_cert_dialog_organizational_unit)}: ${sslCertificate.issuedTo?.uName}"
        tvIssuedByCn.text = "${getString(R.string.ssl_cert_dialog_common_name)}: ${sslCertificate.issuedBy?.cName}"
        tvIssuedByO.text = "${getString(R.string.ssl_cert_dialog_organization)}: ${sslCertificate.issuedBy?.oName}"
        tvIssuedByUn.text = "${getString(R.string.ssl_cert_dialog_organizational_unit)}: ${sslCertificate.issuedBy?.uName}"
        tvIssuedOn.text = "${getString(R.string.ssl_cert_dialog_issued_on)}: ${DateFormat.getDateTimeInstance().format(sslCertificate.validNotBeforeDate)}"
        tvExpiresOn.text = "${getString(R.string.ssl_cert_dialog_expires_on)}: ${DateFormat.getDateTimeInstance().format(sslCertificate.validNotAfterDate)}"
    }

    private fun setButtonStyleToFollowing() {
        binding.btnFollow.apply {
            text = "Following"
            setBackgroundColor(requireContext().color(R.color.purple_50))
            strokeWidth = 0
        }
    }

    private fun setButtonStyleToFollow() {
        binding.btnFollow.apply {
            text = "Follow"
            setBackgroundColor(requireContext().color(R.color.white))
            strokeWidth = 1.dpToPx().toInt()
            strokeColor = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheet.layoutParams.height = deviceHeight()
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        var oldState = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        oldState = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> {
                        if (oldState == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}