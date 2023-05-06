package com.singularitycoder.connectme.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentUserProfileBinding
import com.singularitycoder.connectme.downloads.DownloadsFragment
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.UserProfile
import dagger.hilt.android.AndroidEntryPoint

private const val IS_SELF_PROFILE = "IS_SELF_PROFILE"

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(isSelfProfile: Boolean) = UserProfileFragment().apply {
            arguments = Bundle().apply {
                putBoolean(IS_SELF_PROFILE, isSelfProfile)
            }
        }
    }

    private lateinit var binding: FragmentUserProfileBinding

    private var isSelfProfile = false

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            println("viewpager2: onPageScrollStateChanged")
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            println("viewpager2: onPageSelected")
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            println("viewpager2: onPageScrolled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSelfProfile = arguments?.getBoolean(IS_SELF_PROFILE) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    private fun FragmentUserProfileBinding.setupUI() {
        llFollowButtons.isVisible = isSelfProfile.not()
        if (isSelfProfile.not()) {
            btnFollow.setMargins(start = 0, top = 0, end = 0, bottom = 16.dpToPx().toInt())
        }
        tabLayoutUserProfile.isVisible = isSelfProfile
        setUpViewPager()
        ivProfileImage.setImageDrawable(requireContext().drawable(R.drawable.hithesh))
    }

    private fun FragmentUserProfileBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        btnMenu.onSafeClick {
            val optionsList = listOf("Close")
            requireContext().showPopupMenu(
                view = it.first,
                menuList = optionsList
            ) { menuPosition: Int ->
                when (optionsList[menuPosition]) {
                    optionsList[0] -> {
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    }
                }
            }
        }
    }

    private fun FragmentUserProfileBinding.observeForData() {

    }

    private fun FragmentUserProfileBinding.setUpViewPager() {
        viewpagerUserProfile.apply {
            adapter = UserProfileViewPagerAdapter(fragmentManager = requireActivity().supportFragmentManager, lifecycle = lifecycle)
            registerOnPageChangeCallback(viewPager2PageChangeListener)
        }
        tabLayoutUserProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = Unit
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        tabLayoutUserProfile.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
        TabLayoutMediator(tabLayoutUserProfile, viewpagerUserProfile) { tab, position ->
            tab.text = UserProfile.values()[position].value
        }.attach()
    }

    inner class UserProfileViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return if (isSelfProfile) {
                UserProfile.values().size
            } else 1
        }
        override fun createFragment(position: Int): Fragment {
            return if (isSelfProfile) {
                when (position) {
                    UserProfile.CHAT.ordinal -> ChatFragment.newInstance(screenType = UserProfile.FOLLOW.value)
                    UserProfile.DOWNLOADS.ordinal -> DownloadsFragment.newInstance(screenType = UserProfile.FOLLOW.value, isSelfProfile = true)
                    UserProfile.FOLLOW.ordinal -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOW.value)
                    UserProfile.FOLLOWING.ordinal -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOWING.value)
                    UserProfile.FOLLOWERS.ordinal -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOWERS.value)
                    else -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOW_REQUESTS.value)
                }
            } else {
                DownloadsFragment.newInstance(screenType = UserProfile.DOWNLOADS.value, isSelfProfile = false)
            }
        }
    }
}