package com.singularitycoder.connectme.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemUserFollowingBinding
import com.singularitycoder.connectme.helpers.UserProfile

class UserFollowingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var followingList = emptyList<UserFollowing>()
    var profileScreenType: String? = UserProfile.FOLLOW.value
    private var itemClickListener: (userFollowing: UserFollowing) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemUserFollowingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowingViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FollowingViewHolder).setData(followingList[position])
    }

    override fun getItemCount(): Int = followingList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnClickListener(listener: (userFollowing: UserFollowing) -> Unit) {
        itemClickListener = listener
    }

    inner class FollowingViewHolder(
        private val itemBinding: ListItemUserFollowingBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(userFollowing: UserFollowing) {
            when (profileScreenType) {
                UserProfile.FOLLOW.value -> {
                    itemBinding.btnFollow.isVisible = true
                    itemBinding.btnFollow.text = "Follow"
                    itemBinding.btnDenyFollowRequest.isVisible = false
                    itemBinding.btnAcceptFollowRequest.isVisible = false
                }
                UserProfile.FOLLOWING.value -> {
                    itemBinding.btnFollow.isVisible = true
                    itemBinding.btnDenyFollowRequest.isVisible = false
                    itemBinding.btnAcceptFollowRequest.isVisible = false
                }
                UserProfile.FOLLOWERS.value -> {
                    itemBinding.btnFollow.isVisible = true
                    itemBinding.btnFollow.text = "Follow"
                    itemBinding.btnDenyFollowRequest.isVisible = false
                    itemBinding.btnAcceptFollowRequest.isVisible = false
                }
                UserProfile.FOLLOW_REQUESTS.value -> {
                    itemBinding.btnFollow.isVisible = false
                    itemBinding.btnDenyFollowRequest.isVisible = true
                    itemBinding.btnAcceptFollowRequest.isVisible = true
                }
            }
            itemBinding.apply {
                ivProfileImage.load(userFollowing.imageUrl) {
                    placeholder(R.color.black)
                }
                tvFollowingSite.text = userFollowing.title
                tvPostsToday.text = "May the cringe be with you!"
                root.setOnClickListener {
                    itemClickListener.invoke(userFollowing)
                }
            }
        }
    }
}
