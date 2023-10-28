package com.singularitycoder.connectme.downloads

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build.VERSION.SDK_INT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.load
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemDownloadBinding
import com.singularitycoder.connectme.helpers.color
import com.singularitycoder.connectme.helpers.constants.AndroidFormat
import com.singularitycoder.connectme.helpers.constants.ArchiveFormat
import com.singularitycoder.connectme.helpers.constants.AudioFormat
import com.singularitycoder.connectme.helpers.constants.DocumentFormat
import com.singularitycoder.connectme.helpers.constants.ImageFormat
import com.singularitycoder.connectme.helpers.constants.VideoFormat
import com.singularitycoder.connectme.helpers.deviceWidth
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.onCustomLongClick
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.setMargins
import com.singularitycoder.connectme.helpers.toLowCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DownloadsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var downloadsList = emptyList<Download?>()
    private var itemClickListener: (download: Download?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (download: Download?, view: View?, position: Int?) -> Unit = { _, _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(downloadsList[position])
    }

    override fun getItemCount(): Int = downloadsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (download: Download?, position: Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(
        listener: (
            download: Download?,
            view: View?,
            position: Int?
        ) -> Unit
    ) {
        itemLongClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemDownloadBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetJavaScriptEnabled")
        fun setData(download: Download?) {
            itemBinding.apply {
                ivItemImage.layoutParams.height = (deviceWidth() / 3) - 20.dpToPx().toInt() // 20 is margin size
                ivItemImage.layoutParams.width = (deviceWidth() / 2) - 20.dpToPx().toInt()

                webView.isVisible = false

                ivItemIcon.layoutParams.height = ivItemImage.layoutParams.height / 3
                ivItemIcon.layoutParams.width = ivItemImage.layoutParams.height / 3

                tvSource.text = download?.size
                tvTitle.text = download?.title

                ivItemImage.visibility = View.INVISIBLE
                ivItemIconDummy.isVisible = false
                ivItemIcon.isVisible = true

                val fileExtension = download?.extension?.toLowCase()?.trim()
                when {
                    fileExtension in ImageFormat.values().map { it.value.toLowCase().trim() } -> {
                        ivItemImage.isVisible = true
                        ivItemIcon.isVisible = false
                        if (download?.extension?.contains(ImageFormat.GIF.value, true) == true) {
                            CoroutineScope(IO).launch {
                                val imageLoader = ImageLoader.Builder(root.context)
                                    .components {
                                        if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
                                    }
                                    .build()
                                val imageRequest = ImageRequest.Builder(root.context).data(download.path).build()
                                val drawable = imageLoader.execute(imageRequest).drawable
                                withContext(Main) {
                                    ivItemImage.load(drawable)
                                }
                            }
                        } else {
                            ivItemImage.load(download?.path)
                        }
                    }

                    fileExtension in VideoFormat.values().map { it.value.toLowCase().trim() } -> {
                        ivItemImage.isVisible = true
                        ivItemIcon.apply {
                            isVisible = true
                            setImageResource(R.drawable.round_play_arrow_24)
                            imageTintList = ColorStateList.valueOf(root.context.color(R.color.purple_50))
                        }
                        ivItemIconDummy.apply {
                            isVisible = true
                            setImageResource(R.drawable.round_play_arrow_24)
                            imageTintList = ColorStateList.valueOf(root.context.color(R.color.purple_500))
                            setMargins(all = -8.dpToPx().toInt())
                        }
                        CoroutineScope(IO).launch {
                            val imageLoader = ImageLoader.Builder(root.context)
                                .components {
                                    add(VideoFrameDecoder.Factory())
                                }
                                .build()
                            val imageRequest = ImageRequest.Builder(root.context).data(download?.path).build()
                            val drawable = imageLoader.execute(imageRequest).drawable
                            withContext(Main) {
                                ivItemImage.load(drawable) {
                                    videoFrameMillis(1000)
                                }
                            }
                        }
                    }

                    fileExtension in AudioFormat.values().map { it.value.toLowCase().trim() } -> {
                        ivItemIcon.setImageResource(R.drawable.outline_audiotrack_24)
                    }

                    fileExtension in DocumentFormat.values().map { it.value.toLowCase().trim() } -> {
                        ivItemIcon.setImageResource(R.drawable.outline_article_24)

                        fun showPdfPreview() {
                            // https://djangocas.dev/blog/android/various-ways-to-load-pdf-in-android-webview/
                            // https://stackoverflow.com/questions/18302603/where-to-place-the-assets-folder-in-android-studio
                            // https://github.com/mozilla/pdf.js
                            // https://mozilla.github.io/pdf.js/
                            // https://juejin.cn/post/7017840637450043422
                            webView.isVisible = true
                            webView.settings.javaScriptEnabled = true
                            webView.settings.allowFileAccess = true
//                        webView.settings.allowFileAccessFromFileURLs = true
//                        webView.settings.allowUniversalAccessFromFileURLs = true
                            webView.settings.builtInZoomControls = false
                            webView.settings.setSupportZoom(false)
                            webView.settings.displayZoomControls = false
//                            webView.settings.useWideViewPort = true
//                            webView.isNestedScrollingEnabled = false
//                            webView.isHorizontalScrollBarEnabled = false
//                            webView.isVerticalScrollBarEnabled = false
//                            webView.webChromeClient = WebChromeClient()

                            webView.loadUrl("file:///android_asset/pdf_viewer/pdfviewer.html?file=" + download?.path)
//                           webView.loadUrl("file:///android_asset/pdf_viewer/index.html?file=" + download?.path)
//                        webView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + download?.path)
                        }

//                        showPdfPreview()
                    }

                    fileExtension in ArchiveFormat.values().map { it.value.toLowCase().trim() } -> {
                        ivItemIcon.setImageResource(R.drawable.outline_folder_zip_24)
                    }

                    fileExtension in AndroidFormat.values().map { it.value.toLowCase().trim() } -> {
                        ivItemIcon.setImageResource(R.drawable.outline_android_24)
                    }

                    download?.isDirectory == true -> {
                        ivItemIcon.setImageResource(R.drawable.outline_folder_24)
                    }

                    else -> {
                        ivItemIcon.setImageResource(R.drawable.outline_insert_drive_file_24)
                    }
                }

                viewTouch.onSafeClick {
                    itemClickListener.invoke(download, bindingAdapterPosition)
                }
                viewDummyCenter.setOnLongClickListener {
                    itemLongClickListener.invoke(download, it, bindingAdapterPosition)
                    false
                }
                viewTouch.onCustomLongClick {
                    viewDummyCenter.performLongClick()
                }
            }
        }
    }
}
