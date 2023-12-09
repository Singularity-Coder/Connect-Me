package com.singularitycoder.connectme.downloads

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentBookReaderBinding
import com.singularitycoder.connectme.helpers.changeColor
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.drawable
import com.singularitycoder.connectme.helpers.getTextFromPdf
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.setNavigationBarColor
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val ARG_PARAM_FILE_PATH = "ARG_PARAM_FILE_PATH"

@AndroidEntryPoint
class BookReaderFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(imagePath: String?) = BookReaderFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_FILE_PATH, imagePath) }
        }
    }

    private lateinit var binding: FragmentBookReaderBinding

    private var filePath: String? = null
    private var eraserThickness: Int = 30

    private val downloadsViewModel by activityViewModels<DownloadsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePath = arguments?.getString(ARG_PARAM_FILE_PATH)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentBookReaderBinding.setupUI() {
        requireActivity().setNavigationBarColor(R.color.white)

        val file = File(filePath ?: "")
        tvTitle.text = file.name
        // TODO get this from Worker -> Store in DB -> get in flow -> remember word position
        tvFileText.text = file.getTextFromPdf()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sliderEraserThickness.min = 0
        }

        // https://stackoverflow.com/questions/3332924/textview-marquee-not-working
        tvTitle.isSelected = true

        sliderEraserThickness.max = 200

        sliderEraserThickness.progress = 30

        tvEraserThicknessValue.text = sliderEraserThickness.progress.toString()

//        ibBrush.background = requireContext().drawable(R.drawable.shape_rounded_layout)?.changeColor(requireContext(), R.color.purple_50)

        scrollViewFileText.isSmoothScrollingEnabled = true
    }

    private fun FragmentBookReaderBinding.setupUserActionListeners() {
        root.setOnClickListener(null)

        ibClose.onSafeClick {
            activity?.supportFragmentManager?.popBackStackImmediate()
        }

        ibColor.onSafeClick {
        }

        ibSettings.onSafeClick {
            BookReaderFiltersBottomSheetFragment.newInstance().show(parentFragmentManager, BottomSheetTag.TAG_BOOK_READER_FILTERS)
        }

        ibNavigate.onSafeClick {
            sliderEraserThickness.progress = eraserThickness
            val navDrawable = if (clEraserSettings.isVisible) {
                R.drawable.ic_empty_drawable
            } else {
                R.drawable.shape_rounded_layout
            }
            ibNavigate.background = requireContext().drawable(navDrawable)?.changeColor(requireContext(), R.color.purple_50)
            clEraserSettings.isVisible = clEraserSettings.isVisible.not()
        }

        ibBackward.onSafeClick {
        }

        ibForward.onSafeClick {
        }

        ibReduceEraserThickness.onSafeClick {
            sliderEraserThickness.progress = sliderEraserThickness.progress - 1
        }

        ibIncreaseEraserThickness.onSafeClick {
            sliderEraserThickness.progress = sliderEraserThickness.progress + 1
        }

        sliderEraserThickness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                eraserThickness = progress
                tvEraserThicknessValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ibClose.performClick()
            }
        })

        scrollViewFileText.setOnScrollChangeListener { view: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
//            scrollViewFileText.scrollY = scrollViewFileText.verticalScrollbarPosition
            // Store scrollY position to resume reading from the same
        }
    }

    private fun observeForData() = Unit
}