package com.singularitycoder.connectme.search

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.databinding.FragmentSearchBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint

// TODO drag and drop the tabs outside the tab row to close the tabs

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = SearchFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentSearchBinding

    private var topicParam: String? = null
    private val topicTabsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etSearch.setText("https://www.google.com")
        setUpViewPager()
        binding.setUpUserActionListeners()
        binding.ibAddTab.performClick()
    }

    override fun onResume() {
        super.onResume()
        if (topicTabsList.size == 1 && binding.etSearch.text.isBlank()) {
            binding.etSearch.showKeyboard()
        }
    }

    private fun setUpViewPager() {
        binding.viewpagerReminders.isUserInputEnabled = false
        binding.viewpagerReminders.adapter = SearchViewPagerAdapter(
            fragmentManager = requireActivity().supportFragmentManager,
            lifecycle = lifecycle
        )
        TabLayoutMediator(binding.tabLayoutTopics, binding.viewpagerReminders) { tab, position ->
            tab.text = topicTabsList[position]
        }.attach()
    }

    private fun FragmentSearchBinding.setUpUserActionListeners() {
        cardAddTab.onSafeClick {
            ibAddTab.performClick()
        }

        ibAddTab.onSafeClick {
            binding.etSearch.showKeyboard()
            addTopic("New Tab".capFirstChar())
            etSearch.setSelection(0, etSearch.text.length)
            binding.etSearch.setSelectAllOnFocus(true)
        }

        etSearch.onImeClick {
            etSearch.hideKeyboard()
            etSearch.clearFocus()
        }

        etSearch.setOnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                etSearch.setSelection(0, etSearch.text.length)
                binding.etSearch.setSelectAllOnFocus(true)
            } else {
                etSearch.clearFocus()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                etSearch.clearFocus()
                requireActivity().supportFragmentManager.popBackStackImmediate()
            }
        })

        // https://stackoverflow.com/questions/25216749/soft-keyboard-open-and-close-listener-in-an-activity-in-android
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect() // rect will be populated with the coordinates of your view that area still visible.
            root.getWindowVisibleDisplayFrame(rect)
            val heightDiff: Int = root.rootView.height - (rect.bottom - rect.top)
//            if (heightDiff > 500) {
//            // if more than 500 pixels, its probably a keyboard...
//            } else {
//                if (etSearch.isKeyboardVisible.not()) {
//                    etSearch.hideKeyboard()
//                    etSearch.clearFocus()
//                }
//            }
        }
    }

    // https://stackoverflow.com/questions/13445594/data-sharing-between-fragments-and-activity-in-android
    private fun addTopic(topic: String) {
        topicTabsList.add(topic)
        binding.tabLayoutTopics.addTab(
            /* tab = */ binding.tabLayoutTopics.newTab().apply {
                text = topicTabsList[topicTabsList.lastIndex]
            },
            /* position = */ if (topicTabsList.isNotEmpty()) topicTabsList.lastIndex else 0,
            /* setSelected = */ true
        )
        binding.viewpagerReminders.adapter?.notifyItemInserted(topicTabsList.lastIndex)
    }

    inner class SearchViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = topicTabsList.size
        override fun createFragment(position: Int): Fragment {
            return SearchTabFragment.newInstance(topicTabsList[position])
        }
    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
