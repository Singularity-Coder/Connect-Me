package com.singularitycoder.connectme.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemHistoryBinding
import com.singularitycoder.connectme.helpers.*

class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var historyList = emptyList<History?>()
    private var itemClickListener: (history: History?) -> Unit = {}
    private var itemLongClickListener: (history: History?, view: View?) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoryViewHolder).setData(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnClickListener(listener: (history: History?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnLongClickListener(listener: (history: History?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class HistoryViewHolder(
        private val itemBinding: ListItemHistoryBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(history: History?) {
            history ?: return
            itemBinding.apply {
                tvDate.isVisible = history.isDateShown
                tvDate.text = history.time.toShortDate()
                ivWebappIcon.load(decodeBase64StringToBitmap(history.favicon)) {
                    placeholder(R.drawable.ic_placeholder_rectangle)
                    error(R.drawable.ic_placeholder_rectangle)
                }
                tvTitle.text = if (history.title.isNullOrBlank()) {
                    getHostFrom(url = history.link)
                } else {
                    history.title
                }
                tvSubtitle.text = getHostFrom(url = history.link).replace("www.", "")
                tvTime.text = history.time?.toShortTime()
                root.onSafeClick {
                    itemClickListener.invoke(history)
                }
                viewDummyCenter.setOnClickListener {
                    itemLongClickListener.invoke(history, it)
                }
                root.onCustomLongClick {
                    viewDummyCenter.performClick()
                }
            }
        }
    }
}
