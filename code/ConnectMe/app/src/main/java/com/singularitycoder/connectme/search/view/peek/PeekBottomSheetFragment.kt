package com.singularitycoder.connectme.search.view.peek

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentPeekBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.FragmentsTag
import com.singularitycoder.connectme.search.view.SearchTabFragment
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PEEK_URL = "ARG_PEEK_URL"

@AndroidEntryPoint
class PeekBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(peekUrl: String?) = PeekBottomSheetFragment().apply {
            arguments = Bundle().apply { putString(ARG_PEEK_URL, peekUrl) }
        }
    }


    private lateinit var binding: FragmentPeekBottomSheetBinding

    private var peekUrl: String? = null
    private var searchTabFragment: SearchTabFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        peekUrl = arguments?.getString(ARG_PEEK_URL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPeekBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentPeekBottomSheetBinding.setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        searchTabFragment = SearchTabFragment.newInstance(peekUrl = peekUrl)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_sub_container_view,
                searchTabFragment!!,
                FragmentsTag.SEARCH_TAB
            )
            .commit()
        tvHeader.text = prepareHeader(url = peekUrl)
        linearProgress.trackThickness = clHeader.maxHeight
//        val searchTabFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment_search_tab) as? SearchTabFragment
//        searchTabFragment?.loadUrl(url = peekUrl ?: "")
    }

    private fun FragmentPeekBottomSheetBinding.setupUserActionListeners() {
        searchTabFragment?.getWebViewProgressListener { progress: Int ->
            linearProgress.progress = progress
            linearProgressDummy.progress = progress
            linearProgress.isVisible = progress != 100
            linearProgressDummy.isVisible = progress != 100
            if (progress == 100) {
                ivNavigateBack.isVisible = searchTabFragment?.getWebView()?.canGoBack() == true
                ivNavigateForward.isVisible = searchTabFragment?.getWebView()?.canGoForward() == true
                nestedScrollView.scrollTo(0, 0)
            }
        }

        searchTabFragment?.getWebViewListener { webView: WebView? ->
            tvHeader.text = prepareHeader(url = webView?.url)
        }

        tvHeader.onSafeClick {
            requireContext().clipboard()?.text = tvHeader.text
            requireContext().showToast("Copied!")
        }

        ivNavigateBack.onSafeClick {
            if (searchTabFragment?.getWebView()?.canGoBack() == true) {
                searchTabFragment?.getWebView()?.goBack()
            }
        }

        ivNavigateForward.onSafeClick {
            if (searchTabFragment?.getWebView()?.canGoForward() == true) {
                searchTabFragment?.getWebView()?.goForward()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentPeekBottomSheetBinding.observeForData() {
    }

    private fun prepareHeader(url: String?): String {
        return getHostFrom(url = url)
            .replace(oldValue = "https://", newValue = "")
            .replace(oldValue = "http://", newValue = "")
            .replace(oldValue = "/", newValue = "")
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheet.layoutParams.height = deviceHeight()
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        var oldState = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
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
                    BottomSheetBehavior.STATE_HIDDEN -> dismiss()
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