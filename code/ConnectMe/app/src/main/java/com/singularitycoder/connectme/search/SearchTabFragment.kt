package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentSearchTabBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint


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
        layoutPrivateMode.apply {
            layoutIconText.tvText.text = "Enable Private Mode"
//            layoutIconText.tvText.setTypeface(tvText.typeface, Typeface.BOLD)
            layoutIconText.ivIcon.setImageDrawable(requireActivity().drawable(R.drawable.round_policy_24))
            layoutIconText.ivIcon.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
        layoutVpn.apply {
            layoutIconText.tvText.text = "Enable VPN"
//            layoutIconText.tvText.setTypeface(tvText.typeface, Typeface.BOLD)
            layoutIconText.ivIcon.setImageDrawable(requireActivity().drawable(R.drawable.round_vpn_key_24))
            layoutIconText.ivIcon.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
        layoutCollections.apply {
            tvTitle.text = "Collections"
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
            loadUrl("https://www.github.com")
        }
    }

    private fun FragmentSearchTabBinding.setupUserActionListeners() {
        layoutPrivateMode.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
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
    }

    fun showWebView(isShow: Boolean) {
        if (this::binding.isInitialized) {
            binding.webView.isVisible = isShow
        }
    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
