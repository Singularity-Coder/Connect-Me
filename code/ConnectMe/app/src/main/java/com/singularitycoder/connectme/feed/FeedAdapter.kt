package com.singularitycoder.connectme.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFeedBinding
import com.singularitycoder.connectme.databinding.ListItemFeedLargeTextBinding
import com.singularitycoder.connectme.helpers.*

class FeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var feedList = emptyList<Feed?>()
    private var itemClickListener: (feed: Feed?) -> Unit = {}
    private var itemCountListener: (count: Int) -> Unit = {}
    private var itemLongClickListener: (feed: Feed?, view: View?) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFeedLargeTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int {
        itemCountListener.invoke(feedList.size)
        return feedList.size
    }

    override fun getItemViewType(position: Int): Int = position

    fun getItemCountListener(listener: (count: Int) -> Unit) {
        itemCountListener = listener
    }

    fun setOnItemClickListener(listener: (feed: Feed?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (feed: Feed?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemFeedLargeTextBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(feed: Feed?) {
            itemBinding.apply {
                ivImage.isVisible = feed?.image.isNullOrBlank().not()
                ivImage.load(feed?.image) {
                    placeholder(R.color.black)
                }
                tvSource.setMargins(
                    start = 16.dpToPx().toInt(),
                    top = if (ivImage.isVisible.not()) 16.dpToPx().toInt() else 8.dpToPx().toInt(),
                    end = 16.dpToPx().toInt(),
                    bottom = 0
                )
                val date = convertDateToLong(
                    date = feed?.time ?: "",
                    dateType = DateType.yyyy_MM_dd_T_HH_mm_ss_SS_Z.value
                ).toIntuitiveDateTime(dateType = DateType.dd_MMM_yyyy)
                tvSource.text = "${getHostFrom(url = feed?.website)}" + if (feed?.time.isNullOrBlank()) "" else "  •  $date"
                tvTitle.text = feed?.title
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
