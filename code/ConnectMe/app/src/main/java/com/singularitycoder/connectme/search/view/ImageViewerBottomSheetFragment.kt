package com.singularitycoder.connectme.search.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentImageViewerBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    private lateinit var binding: FragmentImageViewerBottomSheetBinding

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
    }

    private fun FragmentImageViewerBottomSheetBinding.setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        setUpViewPager()
    }

    private fun setUpViewPager() {
        binding.viewpagerImageViewer.apply {
            adapter = MainViewPagerAdapter(
                fragmentManager = requireActivity().supportFragmentManager,
                lifecycle = lifecycle
            )
            currentItem = currentImagePosition
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

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = imageList.size
        override fun createFragment(position: Int): Fragment = ImageFragment.newInstance(imageList[position] ?: "")
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private val launcher: ActivityResultLauncher<String> = registerForActivityResult(CreateDocumentAdvanced()) { uri: Uri? ->
        this.copyImageFromDb(uri)
    }

    private fun showSaveImageDialog() {
        requireContext().showAlertDialog(
            title = getString(R.string.dialog_title_save_image),
            message = getString(R.string.dialog_message_save_image),
            icon = requireContext().drawable(R.drawable.outline_security_24)?.changeColor(requireContext(), R.color.purple_500),
            positiveBtnText = getString(R.string.save_image),
            negativeBtnText = "Cancel",
            positiveAction = {
                try {
                    val name: String = getFileName() + ".png"
                    launcher.launch(name)
                } catch (_: ActivityNotFoundException) {
                }
            }
        )
    }

    /**
     * Saves the attachment to a writeable [Uri].
     */
    private fun copyImageFromDb(uri: Uri?) {
        lifecycleScope.launch(IO) {
            try {
                val file = getImageFile()
                val outputStream = FileOutputStream(file)
//                val inputStream: InputStream = ByteArrayInputStream(body, offset, body.size - offset)
                val inputStream: InputStream = ByteArrayInputStream(uri?.path?.toByteArray())
                copyAndClose(inputStream, outputStream)
                MediaScannerConnection.scanFile(
                    requireContext(), arrayOf<String>(file.toString()), null, null
                )
            } catch (_: IOException) {
            }
        }
    }

    private fun getImageFile(): File? {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        path.mkdirs()
        val fileName = getFileName()
        val ext = ".png"
        var file = File(path, fileName + ext)
        val i = 1
        while (file.exists()) {
            file = File(path, "$fileName ($i)$ext")
        }
        return file
    }

    suspend fun copyAndClose(`in`: InputStream, out: OutputStream) {
        suspendCoroutine<Unit> { continuation: Continuation<Unit> ->
            val buf = ByteArray(4096)
            try {
                while (true) {
                    val read = `in`.read(buf)
                    if (read == -1) break
                    out.write(buf, 0, read)
                }
                `in`.close()
                out.flush()
                out.close()
                continuation.resume(Unit)
            } catch (e: IOException) {
                tryToClose(`in`)
                tryToClose(out)
            }
        }
    }

    fun tryToClose(c: Closeable?) {
        try {
            c?.close()
        } catch (_: IOException) {
        }
    }

    fun getFileName(): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US)
        return sdf.format(Date())
    }

    class CreateDocumentAdvanced : ActivityResultContracts.CreateDocument() {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = super.createIntent(context, input)
            putShowAdvancedExtra(intent)
            return intent
        }

        private fun putShowAdvancedExtra(i: Intent) {
            i.putExtra(if (VERSION.SDK_INT <= 28) "android.content.extra.SHOW_ADVANCED" else "android.provider.extra.SHOW_ADVANCED", true)
        }
    }
}