package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentTabMenuBottomSheetBinding
import com.singularitycoder.connectme.helpers.drawable
import com.singularitycoder.connectme.helpers.setTransparentBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TabMenuBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TabMenuBottomSheetFragment()
    }

    private lateinit var binding: FragmentTabMenuBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabMenuBottomSheetBinding.inflate(inflater, container, false)
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
    private fun FragmentTabMenuBottomSheetBinding.setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        itemHistory.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.round_history_24))
            tvTitle.text = "History"
            tvSubtitle.text = "Last visited on 18 April 2093"
        }
        itemDownloads.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_file_download_24))
            tvTitle.text = "Downloads"
            tvSubtitle.text = "Last downloaded on 18 April 2093"
        }
        itemAddToCollections.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.outline_library_add_24))
            tvTitle.text = "Add to collections"
            tvSubtitle.text = "Last added on 18 April 2093"
        }
        itemFindInPage.apply {
            ivPicture.setImageDrawable(requireContext().drawable(R.drawable.ic_round_search_24))
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
    }

    private fun FragmentTabMenuBottomSheetBinding.setupUserActionListeners() {

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentTabMenuBottomSheetBinding.observeForData() {

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