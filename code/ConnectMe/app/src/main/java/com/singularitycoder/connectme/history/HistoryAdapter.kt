package com.singularitycoder.connectme.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFollowingBinding
import com.singularitycoder.connectme.databinding.ListItemHistoryBinding

class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var historyList = emptyList<History>()
    private var itemClickListener: (history: History) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoryViewHolder).setData(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnClickListener(listener: (history: History) -> Unit) {
        itemClickListener = listener
    }

    inner class HistoryViewHolder(
        private val itemBinding: ListItemHistoryBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(history: History) {
            itemBinding.apply {
                ivImage.load(history.imageUrl) {
                    placeholder(R.drawable.ic_placeholder_rectangle)
                    error(R.drawable.ic_placeholder_rectangle)
                }
                val source = if (history.source.isNullOrBlank()) {
                    history.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    history.source
                }
                tvTitle.text = "$source"
                tvSubtitle.text = "Posted ${history.posts} articles today"
                root.setOnClickListener {
                    itemClickListener.invoke(history)
                }
            }
        }
    }
}
