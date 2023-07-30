package com.singularitycoder.connectme.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemDownloadBinding
import com.singularitycoder.connectme.helpers.deviceWidth
import com.singularitycoder.connectme.helpers.dpToPx
import com.singularitycoder.connectme.helpers.onCustomLongClick
import com.singularitycoder.connectme.helpers.onSafeClick

class DownloadsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Download>()
    private var itemClickListener: (download: Download) -> Unit = {}
    private var itemLongClickListener: (download: Download) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (download: Download) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (download: Download) -> Unit) {
        itemLongClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemDownloadBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(download: Download) {
            itemBinding.apply {
                ivNewsImage.layoutParams.height = (deviceWidth() / 3) - 20.dpToPx().toInt() // 20 is margin size
                ivNewsImage.layoutParams.width = (deviceWidth() / 2) - 20.dpToPx().toInt()
                ivNewsImage.load(download.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (download.source.isNullOrBlank()) {
                    download.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    download.source
                }
//                "\u2022"
                tvSource.text = download.time
                tvTitle.text = download.title
                root.onSafeClick {
                    itemClickListener.invoke(download)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(download)
                }
            }
        }
    }
}
