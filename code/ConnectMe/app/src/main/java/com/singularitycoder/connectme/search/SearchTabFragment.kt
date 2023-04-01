package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentSearchTabBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class SearchTabFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = SearchTabFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentSearchTabBinding

    private var topicParam: String? = null

    private val hideProgressHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var hideProgressRunnable = Runnable {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
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
        layoutVpn.apply {
            layoutIconText.tvText.text = "Enable VPN"
//            layoutIconText.tvText.setTypeface(tvText.typeface, Typeface.BOLD)
            layoutIconText.ivIcon.setImageDrawable(requireActivity().drawable(R.drawable.round_vpn_key_24))
            layoutIconText.ivIcon.setMargins(start = 0, top = -2.dpToPx().toInt(), end = 0, bottom = 0)
            layoutIconText.ivIcon.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
        layoutAdBlocker.apply {
            layoutIconText.tvText.text = "Enable Ad Blocker"
//            layoutIconText.tvText.setTypeface(tvText.typeface, Typeface.BOLD)
            layoutIconText.ivIcon.setImageDrawable(requireActivity().drawable(R.drawable.outline_block_24))
            layoutIconText.ivIcon.setMargins(start = 0, top = -2.dpToPx().toInt(), end = 0, bottom = 0)
            layoutIconText.ivIcon.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
        layoutCollections.apply {
            tvTitle.text = "Collections"
            tvTitle.setTextColor(requireContext().color(R.color.purple_500))
            ivDropdownArrow.isVisible = true
        }
        layoutFollowing.apply {
            tvTitle.text = "Following"
        }
        listOf(layoutFollowing, layoutCollections).forEach {
            it.apply {
                layoutFollowingApp1.ivAppIcon.load(dummyFaviconUrls[0])
                layoutFollowingApp1.tvAppName.text = "Doodle"
                layoutFollowingApp2.ivAppIcon.load(dummyFaviconUrls[1])
                layoutFollowingApp2.tvAppName.text = "Stupify"
                layoutFollowingApp3.ivAppIcon.load(dummyFaviconUrls[2])
                layoutFollowingApp3.tvAppName.text = "Hitgub"
                layoutFollowingApp4.ivAppIcon.load(dummyFaviconUrls[3])
                layoutFollowingApp4.tvAppName.text = "Coldstar"
            }
        }
        layoutHistory.apply {
            tvTitle.text = "History"
        }
        layoutDownloads.apply {
            tvTitle.text = "Downloads"
        }
        val searchFragment = requireActivity().supportFragmentManager.fragments.firstOrNull {
            it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
        } as? SearchFragment
        webView.apply {
            settings.apply {
                loadsImagesAutomatically = true
                javaScriptEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true // allow pinch to zooom
                displayZoomControls = false // disable the default zoom controls on the page
            }
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, progress: Int) {
                    searchFragment?.setLinearProgress(progress)
                    if (progress == 100) {
                        hideProgressRunnable = Runnable { searchFragment?.showLinearProgress(false) }
                        hideProgressHandler.removeCallbacks(hideProgressRunnable)
                        hideProgressHandler.postDelayed(hideProgressRunnable, 1.seconds())
//                        doAfter(1.seconds()) {
//                            searchFragment?.showLinearProgress(false)
//                        }
                    }
                }
            }
            webViewClient = object : WebViewClient() {
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
            }
            loadUrl("")
        }
    }

    private fun FragmentSearchTabBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        layoutCollections.apply {
            viewDummyForDropdown.onSafeClick {
                val collectionsList = listOf("Collection 1", "Collection 2", "Collection 3")
                val adapter = ArrayAdapter(
                    /* context = */ requireContext(),
                    /* resource = */ android.R.layout.simple_list_item_1,
                    /* objects = */ collectionsList
                )
                ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle).apply {
                    anchorView = it.first
                    setAdapter(adapter)
                    setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                        layoutCollections.tvTitle.text = collectionsList[position]
                        this.dismiss()
                    }
                    show()
                }
            }
            tvTitle.setOnClickListener {
                layoutCollections.viewDummyForDropdown.performClick()
            }
            ivDropdownArrow.setOnClickListener {
                layoutCollections.tvTitle.performClick()
            }
            clShowMore.setOnClickListener {
                layoutCollections.ivShowMore.performClick()
            }
            ivShowMore.onSafeClick {
                requireContext().showToast("Show more")
            }
        }

        layoutVpn.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        layoutAdBlocker.apply {
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

    fun showWebView(isShow: Boolean) {
        if (this::binding.isInitialized) {
            binding.webView.isVisible = isShow
        }
    }
}