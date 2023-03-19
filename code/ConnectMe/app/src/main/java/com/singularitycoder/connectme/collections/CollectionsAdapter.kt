package com.singularitycoder.connectme.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFeedBinding

class CollectionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Collection>()
    private var newsClickListener: (collection: Collection) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (collection: Collection) -> Unit) {
        newsClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemFeedBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(collection: Collection) {
            itemBinding.apply {
                ivNewsImage.load(collection.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (collection.source.isNullOrBlank()) {
                    collection.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    collection.source
                }
                tvSource.text = "$source  \u2022  ${collection.time}"
                tvTitle.text = collection.title
                root.setOnClickListener {
                    newsClickListener.invoke(collection)
                }
            }
        }
    }
}
