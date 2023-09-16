package com.singularitycoder.connectme.downloads

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentMarkupBinding
import com.singularitycoder.connectme.helpers.changeColor
import com.singularitycoder.connectme.helpers.color
import com.singularitycoder.connectme.helpers.constants.MarkupColor
import com.singularitycoder.connectme.helpers.deviceHeight
import com.singularitycoder.connectme.helpers.deviceWidth
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.drawable
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.scale
import com.singularitycoder.connectme.helpers.setNavigationBarColor
import com.singularitycoder.connectme.helpers.showAlertDialog
import com.singularitycoder.connectme.helpers.showPopupMenuWithIcons
import com.singularitycoder.connectme.helpers.toBitmap
import com.singularitycoder.connectme.helpers.toBitmapOf
import com.singularitycoder.connectme.helpers.toMutableBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var brushThickness: Int = 10
    private var brushTransparency: Int = 255
    private var eraserThickness: Int = 30
    private var selectedColor: Int = MarkupColor.RED.colorInt
    private var eraserSelectedCount = 0
    private var brushSelectedCount = 1 // Since brush is selected by default

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
        tvTitle.text = file.name
        val canvasHeight = (deviceHeight() - 32.dpToPx() - cardToolbar.layoutParams.height - cardMarkupTools.layoutParams.height).toInt()
        val bitmap = file.toBitmap()
            ?.scale(
                maxWidth = (deviceWidth() - 32.dpToPx()).toInt(),
                maxHeight = canvasHeight
            )
            ?.toMutableBitmap()
        drawingView.canvasBitmap = bitmap
        drawingView.layoutParams.height = bitmap?.height ?: 0
        drawingView.layoutParams.width = bitmap?.width ?: 0

        ibColor.setColorFilter(requireContext().color(MarkupColor.RED.colorInt))
        drawingView.setBrushColor(requireContext().color(MarkupColor.RED.colorInt))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sliderEraserThickness.min = 0
            sliderBrushThickness.min = 0
            sliderBrushTransparency.min = 0
        }

        sliderEraserThickness.max = 200
        sliderBrushThickness.max = 200
        sliderBrushTransparency.max = 255

        sliderEraserThickness.progress = 30
        sliderBrushThickness.progress = 10
        sliderBrushTransparency.progress = 255

        tvBrushThicknessValue.text = sliderBrushThickness.progress.toString()
        tvEraserThicknessValue.text = sliderEraserThickness.progress.toString()
        tvBrushTransparencyValue.text = sliderBrushTransparency.progress.toString()

        drawingView.setSizeForBrush(sliderBrushThickness.progress)
        drawingView.setBrushAlpha(sliderBrushTransparency.progress)

        ibBrush.background = requireContext().drawable(R.drawable.shape_rounded_layout)?.changeColor(requireContext(), R.color.purple_50)
    }

    private fun FragmentMarkupBinding.setupUserActionListeners() {
        root.setOnClickListener(null)

        ibClose.onSafeClick {
            if (drawingView.getDrawing().isEmpty()) {
                activity?.supportFragmentManager?.popBackStackImmediate()
                return@onSafeClick
            }
            requireContext().showAlertDialog(
                title = "Discard changes",
                message = "This action cannot be undone.",
                positiveBtnText = "Discard",
                negativeBtnText = "Cancel",
                positiveBtnColor = R.color.md_red_700,
                positiveAction = {
                    activity?.supportFragmentManager?.popBackStackImmediate()
                }
            )
        }

        btnDone.onSafeClick {
            btnDone.visibility = View.INVISIBLE
            progressCircular.isVisible = true
            lifecycleScope.launch {
                val bitmapDrawableOfLayout = drawingView.toBitmapOf(
                    width = drawingView.width,
                    height = drawingView.height
                )?.toDrawable(requireContext().resources)
                downloadsViewModel.setMarkedUpBitmap(bitmapDrawableOfLayout?.bitmap)
                withContext(Main) {
                    activity?.supportFragmentManager?.popBackStackImmediate()
                }
            }
        }

        ibColor.onSafeClick {
            setupColorSelectionPopupMenu(view = it.first)
        }

        ibUndo.onSafeClick {
            drawingView.undo()
        }

        ibRedo.onSafeClick {
            drawingView.redo()
        }

        ibBrush.onSafeClick {
            brushSelectedCount++
            eraserSelectedCount = 0
            drawingView.setSizeForBrush(sliderBrushThickness.progress)
            ibColor.setColorFilter(requireContext().color(selectedColor))
            drawingView.setBrushColor(requireContext().color(selectedColor))
            sliderBrushThickness.progress = brushThickness
            sliderBrushTransparency.progress = brushTransparency
            ibBrush.background = requireContext().drawable(R.drawable.shape_rounded_layout)?.changeColor(requireContext(), R.color.purple_50)
            ibEraser.background = requireContext().drawable(R.drawable.ic_empty_drawable)
            clEraserSettings.isVisible = false

            when (brushSelectedCount) {
                1 -> {
                    clBrushSettings.isVisible = false
                }

                2 -> {
                    clBrushSettings.isVisible = true
                    brushSelectedCount = 0
                }
            }
        }

        ibEraser.onSafeClick {
            eraserSelectedCount++
            brushSelectedCount = 0
            drawingView.setSizeForBrush(sliderEraserThickness.progress)
            sliderEraserThickness.progress = eraserThickness
            drawingView.erase()
            ibEraser.background = requireContext().drawable(R.drawable.shape_rounded_layout)?.changeColor(requireContext(), R.color.purple_50)
            ibBrush.background = requireContext().drawable(R.drawable.ic_empty_drawable)
            clBrushSettings.isVisible = false

            when (eraserSelectedCount) {
                1 -> {
                    clEraserSettings.isVisible = false
                }

                2 -> {
                    clEraserSettings.isVisible = true
                    eraserSelectedCount = 0
                }
            }
        }

        drawingView.getOnTouchListener { isDown: Boolean ->
            if (clBrushSettings.isVisible || clEraserSettings.isVisible) return@getOnTouchListener
            cardToolbar.isVisible = isDown.not()
            cardMarkupTools.isVisible = isDown.not()
        }

        ibReduceBrushTransparency.onSafeClick {
            sliderBrushTransparency.progress = sliderBrushTransparency.progress - 1
        }

        ibIncreaseBrushTransparency.onSafeClick {
            sliderBrushTransparency.progress = sliderBrushTransparency.progress + 1
        }

        ibReduceBrushThickness.onSafeClick {
            sliderBrushThickness.progress = sliderBrushThickness.progress - 1
        }

        ibIncreaseBrushThickness.onSafeClick {
            sliderBrushThickness.progress = sliderBrushThickness.progress + 1
        }

        ibReduceEraserThickness.onSafeClick {
            sliderEraserThickness.progress = sliderEraserThickness.progress - 1
        }

        ibIncreaseEraserThickness.onSafeClick {
            sliderEraserThickness.progress = sliderEraserThickness.progress + 1
        }

        sliderBrushThickness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                brushThickness = progress
                tvBrushThicknessValue.text = progress.toString()
                drawingView.setSizeForBrush(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        sliderBrushTransparency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                brushTransparency = progress
                tvBrushTransparencyValue.text = progress.toString()
                drawingView.setBrushAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        sliderEraserThickness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("seekbar progress: $progress")
                eraserThickness = progress
                tvEraserThicknessValue.text = progress.toString()
                drawingView.setSizeForBrush(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ibClose.performClick()
            }
        })
    }

    private fun observeForData() = Unit

    private fun setupColorSelectionPopupMenu(view: View?) {
        val optionsList = MarkupColor.values().map {
            Pair(it.colorName, R.drawable.baseline_circle_24)
        }
        val colorIntsList = MarkupColor.values().map { it.colorInt }
        requireContext().showPopupMenuWithIcons(
            view = view,
            menuList = optionsList,
            title = "Colors",
            colorsList = colorIntsList
        ) { menuItem: MenuItem? ->
            val position = optionsList.indexOf(optionsList.find { it.first == menuItem?.title?.toString()?.trim() })
            binding.ibColor.setColorFilter(requireContext().color(colorIntsList[position]))
            binding.drawingView.setBrushColor(requireContext().color(colorIntsList[position]))
            selectedColor = colorIntsList[position]
            if (brushSelectedCount >= 1) return@showPopupMenuWithIcons
            binding.ibBrush.performClick()
        }
    }
}