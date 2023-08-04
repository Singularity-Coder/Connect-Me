package com.singularitycoder.connectme.downloads

import android.view.LayoutInflater
import android.view.View
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

    var feedList = emptyList<Download?>()
    private var itemClickListener: (download: Download?, position: Int) -> Unit = { _, _ -> }
    private var itemLongClickListener: (download: Download?, view: View?) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).setData(feedList[position])
    }

    override fun getItemCount(): Int = feedList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (download: Download?, position: Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (download: Download?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class NewsViewHolder(
        private val itemBinding: ListItemDownloadBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(download: Download?) {
            itemBinding.apply {
                ivItemImage.layoutParams.height = (deviceWidth() / 3) - 20.dpToPx().toInt() // 20 is margin size
                ivItemImage.layoutParams.width = (deviceWidth() / 2) - 20.dpToPx().toInt()
                tvSource.text = download?.time
                tvTitle.text = download?.title

                if (download?.isDirectory == true) {

                } else {
                    ivItemImage.load(download?.imageUrl) {
                        placeholder(R.color.black)
                    }
                }

                root.onSafeClick {
                    itemClickListener.invoke(download, bindingAdapterPosition)
                }
                viewDummyCenter.setOnLongClickListener {
                    itemLongClickListener.invoke(download, it)
                    false
                }
                root.onCustomLongClick {
                    viewDummyCenter.performLongClick()
                }
            }
        }
    }
}
