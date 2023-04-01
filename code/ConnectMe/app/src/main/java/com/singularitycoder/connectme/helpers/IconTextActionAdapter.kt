package com.singularitycoder.connectme.helpers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.databinding.ListItemIconTextBinding

class IconTextActionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var iconTextActionList = emptyList<IconTextAction>()
    private var onItemClickListener: (iconTextAction: IconTextAction) -> Unit = {}
    private var query: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemIconTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconTextActionViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IconTextActionViewHolder).setData(iconTextActionList[position])
    }

    override fun getItemCount(): Int = iconTextActionList.size

    override fun getItemViewType(position: Int): Int = position

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<IconTextAction>) {
        iconTextActionList = list
        notifyDataSetChanged()
    }

    fun getList(): List<IconTextAction> = iconTextActionList

    fun setOnItemClickListener(listener: (iconTextAction: IconTextAction) -> Unit) {
        onItemClickListener = listener
    }

    fun setQuery(query: String) {
        this.query = query
    }

    inner class IconTextActionViewHolder(
        private val itemBinding: ListItemIconTextBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(iconTextAction: IconTextAction) {
            itemBinding.apply {
                tvText.text = iconTextAction.title
                tvText.highlightQueriedText(query = query, result = tvText.text.toString())
                ivIcon.setImageResource(iconTextAction.icon)
                ivAction.setImageResource(iconTextAction.actionIcon)
                ivAction.isVisible = true
                root.setOnClickListener {
                    onItemClickListener.invoke(iconTextAction)
                }
            }
        }
    }
}
