package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.databinding.FragmentSearchBinding
import com.singularitycoder.connectme.databinding.FragmentSearchTabBinding
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
        setupUI()
    }

    // https://guides.codepath.com/android/Working-with-the-WebView
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupUI() {
        binding.webView.apply {
            settings.apply {
//                loadsImagesAutomatically = true
                javaScriptEnabled = true
//                useWideViewPort = true
//                loadWithOverviewMode = true
//                setSupportZoom(true)
//                builtInZoomControls = true // allow pinch to zooom
//                displayZoomControls = false // disable the default zoom controls on the page
            }
//            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
//            webViewClient = object : WebViewClient() {
//                val progressDialog: ProgressDialog = ProgressDialog(requireContext())
//                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//                    super.onPageStarted(view, url, favicon)
//                    progressDialog.setTitle("Loading...")
//                    progressDialog.setCancelable(false)
//                    progressDialog.show()
//                }
//
//                override fun onPageCommitVisible(view: WebView, url: String) {
//                    super.onPageCommitVisible(view, url)
//                    progressDialog.dismiss()
//                }
//            }
            loadUrl("https://www.google.com")
        }
    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
