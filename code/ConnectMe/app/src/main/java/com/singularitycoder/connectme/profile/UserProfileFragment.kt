package com.singularitycoder.connectme.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentUserProfileBinding
import com.singularitycoder.connectme.feed.FeedFragment
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.history.HistoryFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = UserProfileFragment()
    }

    private lateinit var binding: FragmentUserProfileBinding

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
        setUpViewPager()
        ivProfileImage.setImageDrawable(requireContext().drawable(R.drawable.hithesh))
    }

    private fun FragmentUserProfileBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        btnMenu.onSafeClick {
            val optionsList = listOf("Close")
            requireContext().showPopup(
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
        override fun getItemCount(): Int = UserProfile.values().size
        override fun createFragment(position: Int): Fragment = when (position) {
            UserProfile.FOLLOW.ordinal -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOW.value)
            UserProfile.FOLLOWING.ordinal -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOWING.value)
            UserProfile.FOLLOWERS.ordinal -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOWERS.value)
            else -> UserFollowingFragment.newInstance(screenType = UserProfile.FOLLOW_REQUESTS.value)
        }
    }
}