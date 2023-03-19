package com.singularitycoder.connectme

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.databinding.FragmentMainBinding
import com.singularitycoder.connectme.feed.FeedFragment
import com.singularitycoder.connectme.following.FollowingFragment
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.locationData.PlayServicesAvailabilityChecker
import com.singularitycoder.connectme.history.HistoryFragment
import com.singularitycoder.connectme.profile.UserProfileFragment
import com.singularitycoder.connectme.search.SearchFragment
import com.singularitycoder.treasurehunt.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var playServicesAvailabilityChecker: PlayServicesAvailabilityChecker

    private var lastUpdatedLocation: Location? = null
    private var topicParam: String? = null
    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentMainBinding

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
        binding.viewpagerHome.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
    }

    private fun FragmentMainBinding.setupUI() {
        setUpViewPager()
        ivProfileImage.setImageDrawable(requireContext().drawable(R.drawable.hithesh))
    }

    private fun FragmentMainBinding.setupUserActionListeners() {
//        tvAppSubtitle.setOnClickListener {
//            requireContext().clipboard()?.text = binding.tvAppSubtitle.text
//            binding.root.showSnackBar("Copied location: ${requireContext().clipboard()?.text}")
//        }

        fabSearch.onSafeClick {
            (requireActivity() as MainActivity).showScreen(SearchFragment.newInstance(""), FragmentsTag.SEARCH, isAdd = true)
        }

        cardProfileImage.onSafeClick {
            (requireActivity() as MainActivity).showScreen(UserProfileFragment.newInstance(), FragmentsTag.USER_PROFILE, isAdd = true)
        }
    }

    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = viewModel.lastLocation) { lastLocation: Location? ->
            println("lastLocation: ${lastLocation?.latitude}, ${lastLocation?.longitude}")
            lastUpdatedLocation = lastLocation
            if (playServicesAvailabilityChecker.isGooglePlayServicesAvailable()) {
//                binding.tvAppSubtitle.text = if (lastLocation != null) {
//                    getString(
//                        R.string.location_lat_lng,
//                        lastLocation.latitude,
//                        lastLocation.longitude
//                    )
//                } else {
//                    getString(R.string.waiting_for_location)
//                }
            } else {
//                binding.tvAppSubtitle.text = getString(R.string.play_services_unavailable)
            }
        }
    }

    private fun FragmentMainBinding.setUpViewPager() {
        viewpagerHome.apply {
            adapter = HomeViewPagerAdapter(fragmentManager = requireActivity().supportFragmentManager, lifecycle = lifecycle)
            registerOnPageChangeCallback(viewPager2PageChangeListener)
        }
        tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = Unit
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
        TabLayoutMediator(tabLayoutHome, viewpagerHome) { tab, position ->
            tab.text = Tab.values()[position].value
        }.attach()
    }

    inner class HomeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = Tab.values().size
        override fun createFragment(position: Int): Fragment = when (position) {
            Tab.EXPLORE.ordinal -> FeedFragment.newInstance(screenType = Tab.EXPLORE.value)
            Tab.FEED.ordinal -> FeedFragment.newInstance(screenType = Tab.FEED.value)
            Tab.COLLECTIONS.ordinal -> FeedFragment.newInstance(screenType = Tab.COLLECTIONS.value)
//            Tab.REMAINDERS.ordinal -> FeedFragment.newInstance(screenType = Tab.REMAINDERS.value)
//            Tab.NOTES.ordinal -> FeedFragment.newInstance(screenType = Tab.NOTES.value)
            Tab.FOLLOWING.ordinal -> FollowingFragment.newInstance(screenType = Tab.FOLLOWING.value)
            Tab.HISTORY.ordinal -> HistoryFragment.newInstance(screenType = Tab.HISTORY.value)
            else -> HistoryFragment.newInstance(screenType = Tab.DOWNLOADS.value)
        }
    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
