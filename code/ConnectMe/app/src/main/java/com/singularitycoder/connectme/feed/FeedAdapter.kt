package com.singularitycoder.connectme.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFeedBinding

class FeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Feed>()
    private var newsClickListener: (feed: Feed) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (feed: Feed) -> Unit) {
        newsClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemFeedBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(feed: Feed) {
            itemBinding.apply {
                ivNewsImage.load(feed.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (feed.source.isNullOrBlank()) {
                    feed.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    feed.source
                }
                tvSource.text = "$source  \u2022  ${feed.time}"
                tvTitle.text = feed.title
                root.setOnClickListener {
                    newsClickListener.invoke(feed)
                }
            }
        }
    }
}
