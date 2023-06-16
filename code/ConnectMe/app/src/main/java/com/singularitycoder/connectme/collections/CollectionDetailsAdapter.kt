package com.singularitycoder.connectme.collections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemHistoryBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.history.History

class CollectionDetailsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var webPageList = emptyList<CollectionWebPage?>()
    private var itemClickListener: (collectionWebPage: CollectionWebPage?) -> Unit = {}
    private var itemLongClickListener: (collectionWebPage: CollectionWebPage?, view: View?) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(webPageList[position])
    }

    override fun getItemCount(): Int = webPageList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (collectionWebPage: CollectionWebPage?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnLongClickListener(listener: (collectionWebPage: CollectionWebPage?, view: View?) -> Unit) {
        itemLongClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemHistoryBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(collectionWebPage: CollectionWebPage?) {
            itemBinding.apply {
                tvDate.isVisible = false
                tvTitle.text = collectionWebPage?.title
                tvSubtitle.text = getHostFrom(url = collectionWebPage?.link).replace("www.", "")
                tvTime.text = collectionWebPage?.time?.toIntuitiveDateTime()
                val bitmap = decodeBase64StringToBitmap(collectionWebPage?.favicon)
                ivHistoryIcon.load(bitmap) {
                    placeholder(R.color.white)
                    error(R.color.md_red_900)
                }
                root.onSafeClick {
                    itemClickListener.invoke(collectionWebPage)
                }
                root.onCustomLongClick {
                    itemLongClickListener.invoke(collectionWebPage, it)
                }
            }
        }
    }
}
