package com.singularitycoder.connectme.downloads

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentMarkupBinding
import com.singularitycoder.connectme.helpers.collectLatestLifecycleFlow
import com.singularitycoder.connectme.helpers.getHostFrom
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.setNavigationBarColor
import com.singularitycoder.connectme.helpers.toBitmap
import com.singularitycoder.connectme.helpers.toBitmapOf
import com.singularitycoder.connectme.helpers.toMutableBitmap
import com.singularitycoder.connectme.search.model.WebViewData
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val ARG_PARAM_IMAGE_PATH = "ARG_PARAM_IMAGE_PATH"

@AndroidEntryPoint
class MarkupFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(imagePath: String?) = MarkupFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_IMAGE_PATH, imagePath) }
        }
    }

    private lateinit var binding: FragmentMarkupBinding

    private var imagePath: String? = null

    private val downloadsViewModel by activityViewModels<DownloadsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePath = arguments?.getString(ARG_PARAM_IMAGE_PATH)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMarkupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentMarkupBinding.setupUI() {
        requireActivity().setNavigationBarColor(R.color.white)
        val file = File(imagePath ?: "")
        val bitmap = file.toBitmap()?.toMutableBitmap()
        tvTitle.text = file.name
        drawingView.setBitmap(bitmap)
        // TODO brush, color, eraser, clear, undo, redo
    }

    private fun FragmentMarkupBinding.setupUserActionListeners() {
        root.setOnClickListener(null)

        ibClose.onSafeClick {
            activity?.supportFragmentManager?.popBackStackImmediate()
        }

        btnDone.onSafeClick {
            val bitmapDrawableOfLayout = drawingView.toBitmapOf(
                width = drawingView.width,
                height = drawingView.height
            )?.toDrawable(requireContext().resources)
            downloadsViewModel.setMarkedUpBitmap(bitmapDrawableOfLayout?.bitmap)
            activity?.supportFragmentManager?.popBackStackImmediate()
        }
    }

    private fun observeForData() = Unit
}