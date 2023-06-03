package com.singularitycoder.connectme.search.view.getInsights

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.databinding.ListItemChatSearchBinding
import com.singularitycoder.connectme.helpers.highlightText
import com.singularitycoder.connectme.helpers.onSafeClick

class ChatSearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var chatSearchList = listOf<String?>()
    var query: String? = null
    private var itemClickListener: (chatSearch: String?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemChatSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(chatSearchList[position])
    }

    override fun getItemCount(): Int = chatSearchList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (chatSearch: String?) -> Unit) {
        itemClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemChatSearchBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(chatSearch: String?) {
            itemBinding.apply {
                tvText.text = chatSearch
                if (query != null) {
                    tvText.highlightText(query = query ?: "", result = tvText.text.toString())
                }
                root.onSafeClick {
                    itemClickListener.invoke(chatSearch)
                }
            }
        }
    }
}