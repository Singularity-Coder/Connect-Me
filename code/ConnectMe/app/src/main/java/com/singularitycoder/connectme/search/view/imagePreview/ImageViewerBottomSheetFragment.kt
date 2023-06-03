package com.singularitycoder.connectme.search.view.imagePreview

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentImageViewerBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.FILE_PROVIDER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.*

private const val ARG_PARAM_IMAGE_LIST = "ARG_PARAM_IMAGE_LIST"
private const val ARG_PARAM_CURRENT_IMAGE_POSITION = "ARG_PARAM_CURRENT_IMAGE_POSITION"

@AndroidEntryPoint
class ImageViewerBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(imageList: Array<String?>, currentImagePosition: Int) = ImageViewerBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putStringArray(ARG_PARAM_IMAGE_LIST, imageList)
                putInt(ARG_PARAM_CURRENT_IMAGE_POSITION, currentImagePosition)
            }
        }
    }

    private var imageList: Array<String?> = emptyArray()
    private var currentImagePosition: Int = 0

    private val insightImageFileDir: String by lazy {
        "${requireContext().filesDir.absolutePath}/insight_images"
    }

    private lateinit var binding: FragmentImageViewerBottomSheetBinding

    private val saveImageLauncher: ActivityResultLauncher<String> = registerForActivityResult(CreateDocumentAdvanced()) { uri: Uri? ->
        this.copyImageFromStorage(uri)
    }

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) = Unit
        override fun onPageSelected(position: Int) {
            prepareCurrentImageFile(position)
            currentImagePosition = position
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageList = arguments?.getStringArray(ARG_PARAM_IMAGE_LIST) ?: emptyArray()
        currentImagePosition = arguments?.getInt(ARG_PARAM_CURRENT_IMAGE_POSITION) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageViewerBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().setNavigationBarColor(R.color.white)
        binding.viewpagerImageViewer.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
        deleteAllFilesFrom(directory = requireContext().internalFilesDir(directory = "insight_images"), withName = "insight_image_")
    }

    private fun FragmentImageViewerBottomSheetBinding.setupUI() {
        setTransparentBackground()
        requireActivity().setNavigationBarColor(R.color.black)
        setBottomSheetBehaviour()
        setUpViewPager()
        prepareCurrentImageFile(currentImagePosition)
    }

    private fun FragmentImageViewerBottomSheetBinding.setupUserActionListeners() {
        btnClose.onSafeClick {
            dismiss()
        }

        btnDownload.onSafeClick {
            requireContext().showAlertDialog(
                title = getString(R.string.dialog_title_save_image),
                message = getString(R.string.dialog_message_save_image),
                icon = requireContext().drawable(R.drawable.outline_security_24)?.changeColor(requireContext(), R.color.purple_500),
                positiveBtnText = getString(R.string.save_image),
                negativeBtnText = "Cancel",
                positiveAction = {
//                    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                    if (path.exists().not()) path.mkdirs()
                    saveImageLauncher.launch(getFileName(currentImagePosition))
                }
            )
        }

        btnShare.onSafeClick {
            // https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi
            val imageUri = FileProvider.getUriForFile(
                requireContext(),
                FILE_PROVIDER,
                File(insightImageFileDir, "${getFileName(currentImagePosition)}.png")
            )
            requireContext().shareImageAndTextViaApps(
                uri = imageUri,
                title = "Image from \"Connect Me\" app!",
                intentTitle = "Share image to..."
            )
        }
    }

    private fun setUpViewPager() {
        binding.viewpagerImageViewer.apply {
            adapter = MainViewPagerAdapter(
                fragmentManager = requireActivity().supportFragmentManager,
                lifecycle = lifecycle
            )
            currentItem = currentImagePosition
            registerOnPageChangeCallback(viewPager2PageChangeListener)
        }
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheet.layoutParams.height = deviceHeight()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> {
//                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        dismiss()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }

    private fun prepareCurrentImageFile(position: Int) {
        lifecycleScope.launch(IO) {
            val imageRequest = ImageRequest.Builder(requireContext()).data(imageList.getOrNull(position)).listener(
                onStart = {
                    // set your progressbar visible here
                },
                onSuccess = { request, metadata ->
                    // set your progressbar invisible here
                }
            ).build()
            val drawable = ImageLoader(requireContext()).execute(imageRequest).drawable
            val bitmapToSave = (drawable as BitmapDrawable).bitmap
            bitmapToSave.saveToStorage(
                fileName = "${getFileName(position)}.png",
                fileDir = insightImageFileDir,
            )
        }
    }

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = imageList.size
        override fun createFragment(position: Int): Fragment = ImageFragment.newInstance(imageList[position] ?: "")
    }

    /** Saves the attachment to a writeable [Uri]. */
    private fun copyImageFromStorage(uri: Uri?) {
        lifecycleScope.launch(IO) {
            try {
                val imageFile = File(insightImageFileDir, "${getFileName(currentImagePosition)}.png")
                if (imageFile.exists().not()) return@launch
                val outputStream = FileOutputStream(imageFile)
//                val inputStream: InputStream = ByteArrayInputStream(body, offset, body.size - offset)
                val inputStream: InputStream = ByteArrayInputStream(uri?.path?.toByteArray())
                copyAndClose(inputStream, outputStream)
                MediaScannerConnection.scanFile(
                    /* context = */ requireContext(),
                    /* paths = */ arrayOf<String>(imageFile.toString()),
                    /* mimeTypes = */ null,
                    /* callback = */ null
                )
            } catch (_: IOException) {
            }
        }
    }

    private fun copyAndClose(inputStream: InputStream, outputStream: OutputStream) {
        val buf = ByteArray(4096)
        try {
            while (true) {
                val read = inputStream.read(buf)
                if (read == -1) break
                outputStream.write(buf, 0, read)
            }
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            tryToClose(inputStream)
            tryToClose(outputStream)
        }
    }

    private fun tryToClose(c: Closeable?) {
        try {
            c?.close()
        } catch (_: IOException) {
        }
    }

    private fun getFileName(position: Int): String = "insight_image_$position"

    class CreateDocumentAdvanced : ActivityResultContracts.CreateDocument("image/*") {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = super.createIntent(context, input).apply {
                putExtra(if (VERSION.SDK_INT <= 28) "android.content.extra.SHOW_ADVANCED" else "android.provider.extra.SHOW_ADVANCED", true)
            }
            return intent
        }
    }
}