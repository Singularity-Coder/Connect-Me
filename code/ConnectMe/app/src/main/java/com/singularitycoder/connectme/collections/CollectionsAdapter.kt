package com.singularitycoder.connectme.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemCollectionBinding

class CollectionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var collectionsList = emptyList<Collection?>()
    private var newsClickListener: (collection: Collection?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(collectionsList[position])
    }

    override fun getItemCount(): Int = collectionsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnNewsClickListener(listener: (collection: Collection?) -> Unit) {
        newsClickListener = listener
    }

    inner class ThisViewHolder(
        private val itemBinding: ListItemCollectionBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(collection: Collection?) {
            itemBinding.apply {
                tvTitle.text = collection?.title
                listOf(layoutFollowingApp1, layoutFollowingApp2, layoutFollowingApp3, layoutFollowingApp4).forEachIndexed { index, listItemAppBinding ->
                    listItemAppBinding.ivAppIcon.load(collection?.websitesList?.get(index)?.imageUrl) {
                        placeholder(R.color.black)
                    }
                }
                root.setOnClickListener {
                    newsClickListener.invoke(collection)
                }
            }
        }
    }
}
