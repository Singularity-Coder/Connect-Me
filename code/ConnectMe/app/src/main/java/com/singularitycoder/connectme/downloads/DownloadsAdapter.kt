package com.singularitycoder.connectme.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFeedBinding

class DownloadsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Download>()
    private var newsClickListener: (download: Download) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (download: Download) -> Unit) {
        newsClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemFeedBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(download: Download) {
            itemBinding.apply {
                ivNewsImage.load(download.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (download.source.isNullOrBlank()) {
                    download.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    download.source
                }
                tvSource.text = "$source  \u2022  ${download.time}"
                tvTitle.text = download.title
                root.setOnClickListener {
                    newsClickListener.invoke(download)
                }
            }
        }
    }
}
