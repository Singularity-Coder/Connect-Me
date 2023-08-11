package com.singularitycoder.connectme

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
import com.singularitycoder.connectme.collections.CollectionsFragment
import com.singularitycoder.connectme.databinding.FragmentMainBinding
import com.singularitycoder.connectme.downloads.DownloadsFragment
import com.singularitycoder.connectme.explore.ExploreFragment
import com.singularitycoder.connectme.feed.FeedFragment
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteFragment
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.FragmentsTag
import com.singularitycoder.connectme.helpers.constants.FeatureTab
import com.singularitycoder.connectme.history.HistoryFragment
import com.singularitycoder.connectme.profile.UserProfileFragment
import com.singularitycoder.connectme.search.view.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

// TODO Notifications for remainders
// TODO replace webviews with chrome views

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = MainFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private var topicParam: String? = null

    private lateinit var dateTimeTimer: Timer
    private lateinit var binding: FragmentMainBinding

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            println("viewpager2: onPageScrollStateChanged")
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            println("viewpager2: onPageSelected")
            if (position == 0) {
                setTimeAndDate()
                binding.btnExploreMore.isVisible = true
                binding.cardProfileImage.isVisible = false
            } else {
                binding.tvAppName.text = getString(R.string.app_name)
                binding.btnExploreMore.isVisible = false
                binding.cardProfileImage.isVisible = true
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            println("viewpager2: onPageScrolled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    override fun onDestroy() {
        super.onDestroy()
        dateTimeTimer.purge()
        binding.viewpagerHome.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
    }

    private fun FragmentMainBinding.setupUI() {
        setUpViewPager()
        requireActivity().setNavigationBarColor(R.color.white)
        refreshDateTime()
        ivProfileImage.setImageDrawable(requireContext().drawable(R.drawable.hithesh))
    }

    private fun FragmentMainBinding.setupUserActionListeners() {
        root.setOnClickListener { }

//        tvAppSubtitle.setOnClickListener {
//            requireContext().clipboard()?.text = binding.tvAppSubtitle.text
//            binding.root.showSnackBar("Copied location: ${requireContext().clipboard()?.text}")
//        }

        fabSearch.onSafeClick {
            (requireActivity() as MainActivity).showScreen(SearchFragment.newInstance(""), FragmentsTag.SEARCH, isAdd = true)
        }

        cardProfileImage.onSafeClick {
            (requireActivity() as MainActivity).showScreen(UserProfileFragment.newInstance(isSelfProfile = true), FragmentsTag.USER_PROFILE, isAdd = true)
        }
    }

    private fun observeForData() {

    }

    private fun refreshDateTime() {
        dateTimeTimer = Timer()
        dateTimeTimer.doEvery(
            duration = 1.seconds(),
            withInitialDelay = 0.seconds(),
        ) {
            if (binding.tabLayoutHome.selectedTabPosition == 0) {
                setTimeAndDate()
            }
        }
    }

    private fun setTimeAndDate() {
        val time = timeNow toTimeOfType DateType.h_mm_a
        val hours = time.substringBefore(":")
        val formattedHours = if (hours.length == 1) "0${hours}" else hours
        val minutes = time.substringAfter(":").substringBefore(" ")
        val dayPeriod = time.substringAfter(" ").toUpCase()
        val html = "$formattedHours : $minutes <small><small><small>$dayPeriod</small></small></small>"
        val day = Calendar.getInstance().time.toString().substringBefore(" ")
        binding.tvAppName.text = getHtmlFormattedTime(html)
    }

    private fun FragmentMainBinding.setUpViewPager() {
        viewpagerHome.apply {
            adapter = HomeViewPagerAdapter(fragmentManager = parentFragmentManager, lifecycle = lifecycle)
            registerOnPageChangeCallback(viewPager2PageChangeListener)
        }
        tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = Unit
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        tabLayoutHome.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
        TabLayoutMediator(tabLayoutHome, viewpagerHome) { tab, position ->
            tab.text = FeatureTab.values()[position].value
        }.attach()
        tabLayoutHome.selectTab(tabLayoutHome.getTabAt(2))
    }

    inner class HomeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = FeatureTab.values().size
        override fun createFragment(position: Int): Fragment = when (position) {
            FeatureTab.EXPLORE.ordinal -> ExploreFragment.newInstance(screenType = FeatureTab.EXPLORE.value)
            FeatureTab.FEED.ordinal -> FeedFragment.newInstance(screenType = FeatureTab.FEED.value)
            FeatureTab.COLLECTIONS.ordinal -> CollectionsFragment.newInstance(screenType = FeatureTab.COLLECTIONS.value)
//            Tab.REMAINDERS.ordinal -> FeedFragment.newInstance(screenType = Tab.REMAINDERS.value)
//            Tab.NOTES.ordinal -> FeedFragment.newInstance(screenType = Tab.NOTES.value)
            FeatureTab.FOLLOWING.ordinal -> FollowingWebsiteFragment.newInstance(screenType = FeatureTab.FOLLOWING.value)
            FeatureTab.HISTORY.ordinal -> HistoryFragment.newInstance(screenType = FeatureTab.HISTORY.value)
            else -> DownloadsFragment.newInstance(screenType = FeatureTab.DOWNLOADS.value, isSelfProfile = true)
        }
    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
