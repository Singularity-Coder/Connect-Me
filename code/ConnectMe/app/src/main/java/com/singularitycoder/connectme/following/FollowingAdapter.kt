package com.singularitycoder.connectme.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFollowingBinding

class FollowingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var followingList = emptyList<Following>()
    private var itemClickListener: (following: Following) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFollowingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowingViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FollowingViewHolder).setData(followingList[position])
    }

    override fun getItemCount(): Int = followingList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnClickListener(listener: (following: Following) -> Unit) {
        itemClickListener = listener
    }

    inner class FollowingViewHolder(
        private val itemBinding: ListItemFollowingBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(following: Following) {
            itemBinding.apply {
                ivFollowingIcon.load(following.imageUrl) {
                    placeholder(R.color.black)
                }
                val source = if (following.source.isNullOrBlank()) {
                    following.link?.substringAfter("//")?.substringBefore("/")?.replace("www.", "")
                } else {
                    following.source
                }
                tvFollowingSite.text = "$source"
                tvPostsToday.text = "Posted ${following.posts} articles today"
                root.setOnClickListener {
                    itemClickListener.invoke(following)
                }
            }
        }
    }
}
