package com.singularitycoder.connectme.followingWebsite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemFollowingBinding
import com.singularitycoder.connectme.helpers.decodeBase64StringToBitmap
import com.singularitycoder.connectme.helpers.getHostFrom
import com.singularitycoder.connectme.helpers.onSafeClick

class FollowingWebsiteAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var followingWebsiteList = emptyList<FollowingWebsite?>()
    private var itemClickListener: (followingWebsite: FollowingWebsite?) -> Unit = {}
    private var followClickListener: (followingWebsite: FollowingWebsite?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemFollowingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowingViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FollowingViewHolder).setData(followingWebsiteList[position])
    }

    override fun getItemCount(): Int = followingWebsiteList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnClickListener(listener: (followingWebsite: FollowingWebsite?) -> Unit) {
        itemClickListener = listener
    }

    fun setOnFollowClickListener(listener: (followingWebsite: FollowingWebsite?) -> Unit) {
        followClickListener = listener
    }

    inner class FollowingViewHolder(
        private val itemBinding: ListItemFollowingBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(followingWebsite: FollowingWebsite?) {
            itemBinding.apply {
                ivFollowingIcon.load(decodeBase64StringToBitmap(followingWebsite?.favicon)) {
                    placeholder(R.drawable.ic_placeholder_rectangle)
                    error(R.drawable.ic_placeholder_rectangle)
                }
                tvFollowingSite.text = getHostFrom(url = followingWebsite?.link)
                tvPostsToday.text = "Posted ${followingWebsite?.postCount} articles today"
                root.onSafeClick {
                    itemClickListener.invoke(followingWebsite)
                }
                btnFollow.onSafeClick {
                    followClickListener.invoke(followingWebsite)
                }
            }
        }
    }
}