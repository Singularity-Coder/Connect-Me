package com.singularitycoder.connectme.search.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import android.net.http.SslCertificate
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentWebsiteActionsBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.search.model.WebViewData
import com.singularitycoder.connectme.search.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat

@AndroidEntryPoint
class WebsiteActionsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = WebsiteActionsBottomSheetFragment()
    }

    private val searchViewModel by activityViewModels<SearchViewModel>()

    private lateinit var binding: FragmentWebsiteActionsBottomSheetBinding

    private var searchFragment: SearchFragment? = null

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
        searchFragment = requireActivity().supportFragmentManager.fragments.firstOrNull {
            it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
        } as? SearchFragment
        setTransparentBackground()
        setBottomSheetBehaviour()
        try {
            val selectedWebpage = requireActivity().supportFragmentManager.findFragmentByTag(
                ConnectMeUtils.webpageIdList[searchFragment?.getTabsTabLayout()?.selectedTabPosition ?: 0]
            ) as? SearchTabFragment
            val sslCertificate = selectedWebpage?.getWebView()?.certificate
            ivSiteIcon.setImageBitmap(selectedWebpage?.getFavicon())
            tvSiteName.text = getHostFrom(selectedWebpage?.getWebView()?.url)
            tvLink.text = selectedWebpage?.getWebView()?.title
            binding.setupWebsiteSecurity(sslCertificate)
        } catch (_: Exception) {
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
        itemAddToCollections.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_library_add_24))
            tvTitle.text = "Add to collections"
            tvSubtitle.text = "Last added on 18 April 2093"
            ivArrow.isVisible = true
        }
        itemFindInPage.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_find_in_page_24))
            tvTitle.text = "Find in page"
            tvSubtitle.text = "Search for text in this web page"
        }
        itemAddShortcut.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_add_home_24))
            tvTitle.text = "Add shortcut"
            tvSubtitle.text = "Add shortcut to home screen"
        }
        itemPrint.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_print_24))
            tvTitle.text = "Print"
            tvSubtitle.text = "Print this web page"
        }
        itemTranslate.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_translate_24))
            tvTitle.text = "Translate"
            tvSubtitle.text = "Translate this web page"
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

    private fun FragmentWebsiteActionsBottomSheetBinding.setupUserActionListeners() {
        root.setOnClickListener { }

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

        itemAddToCollections.root.onSafeClick { }

        itemFindInPage.root.onSafeClick { }

        itemAddShortcut.root.onSafeClick {
            val selectedWebpage = requireActivity().supportFragmentManager.findFragmentByTag(
                ConnectMeUtils.webpageIdList[searchFragment?.getTabsTabLayout()?.selectedTabPosition ?: 0]
            ) as? SearchTabFragment
            requireContext().addShortcut(webView = selectedWebpage?.getWebView(), favicon = selectedWebpage?.getFavicon())
        }

        itemPrint.root.onSafeClick { }

        itemTranslate.root.onSafeClick { }

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
            ivSiteIcon.setImageBitmap(it.favIcon)
            tvSiteName.text = getHostFrom(it.url)
            tvLink.text = it.title
            binding.setupWebsiteSecurity(sslCertificate = it.certificate)
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