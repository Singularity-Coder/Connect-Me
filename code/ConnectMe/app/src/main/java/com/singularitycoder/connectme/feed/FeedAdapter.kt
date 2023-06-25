package com.singularitycoder.connectme.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFeedBinding
import com.singularitycoder.connectme.helpers.onCustomLongClick
import com.singularitycoder.connectme.helpers.onSafeClick
import com.singularitycoder.connectme.helpers.toIntuitiveDateTime
import com.singularitycoder.connectme.helpers.toShortTime

class FeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Feed>()
    private var itemClickListener: (feed: Feed?) -> Unit = {}
    private var itemLongClickListener: (feed: Feed?, view: View?) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (feed: Feed?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (feed: Feed?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemFeedBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(feed: Feed) {
            itemBinding.apply {
                ivNewsImage.load(feed.image) {
                    placeholder(R.color.black)
                }
                tvSource.text = "${feed.website}  \u2022  ${feed.time}"
                tvTitle.text = feed.title
                root.onSafeClick {
                    itemClickListener.invoke(feed)
                }
                root.onCustomLongClick {
                    viewDummyCenter.performClick()
                }
                viewDummyCenter.setOnClickListener {
                    itemLongClickListener.invoke(feed, it)
                }
            }
        }
    }
}
