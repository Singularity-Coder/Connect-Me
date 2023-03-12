package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentSearchBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.flowlauncher.helper.pinterestView.CircleImageView
import com.singularitycoder.flowlauncher.helper.pinterestView.PinterestView
import com.singularitycoder.flowlauncher.helper.quickActionView.Action
import com.singularitycoder.flowlauncher.helper.quickActionView.QuickActionView
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
        setTabMenuTouchOptions()
//        setTabMenuTouchOptions2()

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

    @SuppressLint("ClickableViewAccessibility")
    private fun setTabMenuTouchOptions2() {
        fun createChildView(
            imageId: Int,
            tip: String?,
            @ColorRes colorRes: Int
        ): View = CircleImageView(requireContext()).apply {
            borderWidth = 0
            scaleType = ImageView.ScaleType.CENTER_CROP
            fillColor = requireContext().color(colorRes)
            setImageDrawable(requireContext().drawable(imageId)?.changeColor(requireContext(), R.color.purple_500))
            tag = tip // just for save Menu item tips
        }
        binding.pinterestView.addMenuItem(
            createChildView(R.drawable.ic_empty_drawable, QuickActionTabMenu.NONE.value, R.color.purple_50),
            createChildView(R.drawable.round_arrow_back_24, QuickActionTabMenu.NAVIGATE_BACK.value, R.color.purple_50),
            createChildView(R.drawable.round_arrow_forward_24, QuickActionTabMenu.NAVIGATE_FORWARD.value, R.color.purple_50),
            createChildView(R.drawable.other_houses_black_24dp, QuickActionTabMenu.HOME.value, R.color.purple_50),
            createChildView(R.drawable.round_close_24, QuickActionTabMenu.CLOSE_TAB.value, R.color.purple_50),
            createChildView(R.drawable.round_refresh_24, QuickActionTabMenu.REFRESH_TAB.value, R.color.purple_50),
            createChildView(R.drawable.round_share_24, QuickActionTabMenu.SHARE_TAB.value, R.color.purple_50),
        )
        binding.pinterestView.setPinClickListener(object : PinterestView.PinMenuClickListener {
            override fun onMenuItemClick(checkedView: View?, clickItemPos: Int) {
                requireContext().showToast(checkedView?.tag.toString() + " clicked!")
            }

            override fun onAnchorViewClick() {
                requireContext().showToast("button clicked!")
            }
        })
        binding.btnTabMenu.setOnTouchListener { v, event ->
            binding.pinterestView.dispatchTouchEvent(event)
            true
        }
    }

    private fun setTabMenuTouchOptions() {
        val icon1 = requireContext().drawable(R.drawable.round_arrow_back_24)?.changeColor(requireContext(), R.color.purple_500)
        val action1 = Action(id = QuickActionTabMenu.NAVIGATE_BACK.ordinal, icon = icon1!!, title = QuickActionTabMenu.NAVIGATE_BACK.value)

        val icon2 = requireContext().drawable(R.drawable.other_houses_black_24dp)?.changeColor(requireContext(), R.color.purple_500)
        val action2 = Action(id = QuickActionTabMenu.HOME.ordinal, icon = icon2!!, title = QuickActionTabMenu.HOME.value)

        val icon3 = requireContext().drawable(R.drawable.round_share_24)?.changeColor(requireContext(), R.color.purple_500)
        val action3 = Action(id = QuickActionTabMenu.SHARE_TAB.ordinal, icon = icon3!!, title = QuickActionTabMenu.SHARE_TAB.value)

        val icon4 = requireContext().drawable(R.drawable.round_close_24)?.changeColor(requireContext(), R.color.purple_500)
        val action4 = Action(id = QuickActionTabMenu.CLOSE_TAB.ordinal, icon = icon4!!, title = QuickActionTabMenu.CLOSE_TAB.value)

        val icon5 = requireContext().drawable(R.drawable.round_refresh_24)?.changeColor(requireContext(), R.color.purple_500)
        val action5 = Action(id = QuickActionTabMenu.REFRESH_TAB.ordinal, icon = icon5!!, title = QuickActionTabMenu.REFRESH_TAB.value)

        val icon6 = requireContext().drawable(R.drawable.round_arrow_forward_24)?.changeColor(requireContext(), R.color.purple_500)
        val action6 = Action(id = QuickActionTabMenu.NAVIGATE_FORWARD.ordinal, icon = icon6!!, title = QuickActionTabMenu.NAVIGATE_FORWARD.value)

        val addFabQuickActionView = QuickActionView.make(requireContext()).apply {
            addAction(action1)
            addAction(action2)
            addAction(action3)
            addAction(action4)
            addAction(action5)
            addAction(action6)
            register(binding.btnTabMenu)
            setBackgroundColor(requireContext().color(R.color.purple_50))
            setIndicatorDrawable(createGradientDrawable(width = deviceWidth() / 14, height = deviceWidth() / 14))
        }
        addFabQuickActionView.setOnActionHoverChangedListener { action: Action?, quickActionView: QuickActionView?, isHovering: Boolean ->
            // FIXME selection issue
//            if (isHovering) {
//                quickActionView?.setBackgroundColor(requireContext().color(R.color.purple_500))
//                quickActionView?.setIconColor(R.color.purple_50)
//            } else {
//                quickActionView?.setBackgroundColor(requireContext().color(R.color.purple_50))
//                quickActionView?.setIconColor(R.color.purple_500)
//            }
        }
        addFabQuickActionView.setOnActionSelectedListener { action: Action?, quickActionView: QuickActionView? ->
            when (action?.id) {
                QuickActionTabMenu.NAVIGATE_BACK.ordinal -> {}
                QuickActionTabMenu.NAVIGATE_FORWARD.ordinal -> {}
                QuickActionTabMenu.HOME.ordinal -> {}
                QuickActionTabMenu.CLOSE_TAB.ordinal -> {}
                QuickActionTabMenu.REFRESH_TAB.ordinal -> {}
                QuickActionTabMenu.SHARE_TAB.ordinal -> {}
            }
        }
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
