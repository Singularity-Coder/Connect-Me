package com.singularitycoder.connectme.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.ListItemChatBinding
import com.singularitycoder.connectme.databinding.ListItemUserFollowingBinding
import com.singularitycoder.connectme.helpers.constants.UserProfile

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var followingList = emptyList<UserFollowing>()
    var profileScreenType: String? = UserProfile.FOLLOW.value
    private var itemClickListener: (userFollowing: UserFollowing) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThisViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThisViewHolder).setData(followingList[position])
    }

    override fun getItemCount(): Int = followingList.size

    override fun getItemViewType(position: Int): Int = position

    fun setOnClickListener(listener: (userFollowing: UserFollowing) -> Unit) {
        itemClickListener = listener
    }

    private inner class ThisViewHolder(
        private val itemBinding: ListItemChatBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun setData(userFollowing: UserFollowing) {
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
