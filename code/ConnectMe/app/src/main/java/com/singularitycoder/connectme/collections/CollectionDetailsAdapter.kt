package com.singularitycoder.connectme.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemHistoryBinding
import com.singularitycoder.connectme.helpers.decodeBase64StringToBitmap
import com.singularitycoder.connectme.helpers.toIntuitiveDateTime

class CollectionDetailsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var webPageList = emptyList<CollectionWebPage?>()
    private var itemClickListener: (collectionWebPage: CollectionWebPage?) -> Unit = {}

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

    inner class ThisViewHolder(
        private val itemBinding: ListItemHistoryBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(collectionWebPage: CollectionWebPage?) {
            itemBinding.apply {
                tvDate.isVisible = false
                tvTitle.text = collectionWebPage?.title
                tvSubtitle.text = collectionWebPage?.link
                tvTime.text = collectionWebPage?.time?.toIntuitiveDateTime()
                val bitmap = decodeBase64StringToBitmap(collectionWebPage?.favicon)
                ivHistoryIcon.load(bitmap) {
                    placeholder(R.color.white)
                    error(R.color.md_red_900)
                }
                root.setOnClickListener {
                    itemClickListener.invoke(collectionWebPage)
                }
            }
        }
    }
}