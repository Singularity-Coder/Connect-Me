package com.singularitycoder.connectme.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemCollectionBinding
import com.singularitycoder.connectme.helpers.decodeBase64StringToBitmap

class CollectionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var collectionsList = emptyList<LinksCollection?>()
    private var itemClickListener: (linksCollection: LinksCollection?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(collectionsList[position])
    }

    override fun getItemCount(): Int = collectionsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnItemClickListener(listener: (linksCollection: LinksCollection?) -> Unit) {
        itemClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemCollectionBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(linksCollection: LinksCollection?) {
            itemBinding.apply {
                tvTitle.text = linksCollection?.title
                tvCount.apply {
                    isVisible = true
                    text = "(${linksCollection?.count})"
                }
                listOf(layoutFollowingApp1, layoutFollowingApp2, layoutFollowingApp3, layoutFollowingApp4).forEachIndexed { index, listItemAppBinding ->
                    if ((linksCollection?.linkList?.lastIndex ?: 0) >= index) {
                        listItemAppBinding.root.isVisible = true
                        val bitmap = decodeBase64StringToBitmap(linksCollection?.linkList?.get(index)?.favicon)
                        listItemAppBinding.ivAppIcon.load(bitmap) {
                            placeholder(R.color.white)
                            error(R.color.md_red_900)
                        }
                        listItemAppBinding.tvAppName.text = linksCollection?.linkList?.get(index)?.title
                    } else {
                        listItemAppBinding.root.isInvisible = true
                    }
                }
                root.setOnClickListener {
                    itemClickListener.invoke(linksCollection)
                }
            }
        }
    }
}
