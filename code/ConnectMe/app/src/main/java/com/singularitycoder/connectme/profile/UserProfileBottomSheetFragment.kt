package com.singularitycoder.connectme.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentUserProfileBinding
import com.singularitycoder.connectme.feed.FeedFragment
import com.singularitycoder.connectme.helpers.Tab
import com.singularitycoder.connectme.helpers.UserProfile
import com.singularitycoder.connectme.helpers.drawable
import com.singularitycoder.connectme.helpers.setTransparentBackground
import com.singularitycoder.connectme.history.HistoryFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = UserProfileBottomSheetFragment()
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
        setTransparentBackground()
        setBottomSheetBehaviour()
        setUpViewPager()
        ivProfileImage.setImageDrawable(requireContext().drawable(R.drawable.hithesh))
    }

    private fun FragmentUserProfileBinding.setupUserActionListeners() {

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
        TabLayoutMediator(tabLayoutUserProfile, viewpagerUserProfile) { tab, position ->
            tab.text = UserProfile.values()[position].value
        }.attach()
    }

    inner class UserProfileViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = UserProfile.values().size
        override fun createFragment(position: Int): Fragment = when (position) {
            UserProfile.FOLLOW.ordinal -> FeedFragment.newInstance(screenType = Tab.EXPLORE.value)
            UserProfile.FOLLOWING.ordinal -> FeedFragment.newInstance(screenType = Tab.FEED.value)
            UserProfile.FOLLOWERS.ordinal -> FeedFragment.newInstance(screenType = Tab.COLLECTIONS.value)
            else -> HistoryFragment.newInstance(screenType = UserProfile.FOLLOW_REQUESTS.value)
        }
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheet.layoutParams.height = deviceHeight()
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        var oldState = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        oldState = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> {
                        if (oldState == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}