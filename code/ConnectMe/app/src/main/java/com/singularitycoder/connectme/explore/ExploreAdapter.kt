package com.singularitycoder.connectme.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFeedBinding

class ExploreAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Explore>()
    private var newsClickListener: (explore: Explore) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (explore: Explore) -> Unit) {
        newsClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemFeedBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(explore: Explore) {
            itemBinding.apply {
                ivNewsImage.load(explore.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (explore.source.isNullOrBlank()) {
                    explore.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    explore.source
                }
                tvSource.text = "$source  \u2022  ${explore.time}"
                tvTitle.text = explore.title
                root.setOnClickListener {
                    newsClickListener.invoke(explore)
                }
            }
        }
    }
}
