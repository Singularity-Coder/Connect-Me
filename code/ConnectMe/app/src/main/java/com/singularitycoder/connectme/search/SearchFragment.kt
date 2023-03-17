package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
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
        binding.etSearch.setText("https://www.github.com")
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

        ivProfile.onSafeClick {
            WebsiteActionsBottomSheetFragment.newInstance().show(
                /* manager = */ requireActivity().supportFragmentManager,
                /* tag = */ BottomSheetTag.WEBSITE_ACTIONS
            )
        }

        cardAddTab.onSafeClick {
            ibAddTab.performClick()
        }

        cardAddTab.setOnLongClickListener {
            ibAddTab.performLongClick()
            true
        }

        ibAddTab.setOnLongClickListener {
            showPopupMenu(view = it, menuRes = R.menu.new_tab_popup_menu)
            true
        }

        ibAddTab.onSafeClick {
            binding.etSearch.showKeyboard()
            addTopic("New Tab".capFirstChar())
            etSearch.setSelection(0, etSearch.text.length)
            binding.etSearch.setSelectAllOnFocus(true)
        }

        btnVoiceSearch.onSafeClick {
            requireContext().showToast("Voice Search")
        }

        btnQrScan.onSafeClick {
            requireContext().showToast("QR Scan")
        }

        etSearch.onImeClick {
            etSearch.hideKeyboard()
            etSearch.clearFocus()
        }

        etSearch.setOnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                etSearch.setSelection(0, etSearch.text.length)
                binding.etSearch.setSelectAllOnFocus(true)
                btnWebsiteQuickActions.isVisible = false
                btnQrScan.isVisible = true
                btnVoiceSearch.isVisible = true
            } else {
                etSearch.clearFocus()
                btnWebsiteQuickActions.isVisible = true
                btnQrScan.isVisible = false
                btnVoiceSearch.isVisible = false
            }
            try {
                val searchTabFragment = requireActivity().supportFragmentManager.fragments.firstOrNull {
                    it.javaClass.simpleName == SearchTabFragment.newInstance("").javaClass.simpleName
                } as? SearchTabFragment
                searchTabFragment?.showWebView(isFocused.not())
            } catch (_: Exception) {
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // TODO set a neutral button to save all tabs to a collection
                requireContext().showAlertDialog(
                    title = "Close all tabs?",
                    message = """
                        Press "Close" to permanently close all tabs.
                        Press "Keep" to retain them in collections. 
                    """.trimIndent(),
                    positiveBtnText = "Close",
                    negativeBtnText = "Keep",
                    neutralBtnText = "Cancel",
                    positiveAction = {
                        etSearch.clearFocus()
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    },
                    negativeAction = {
                        etSearch.clearFocus()
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    }
                )
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
    @SuppressLint("NotifyDataSetChanged")
    private fun addTopic(topic: String) {
        topicTabsList.add(topic)
        val position = if (topicTabsList.isNotEmpty()) topicTabsList.lastIndex else 0
        binding.tabLayoutTopics.addTab(
            /* tab = */ binding.tabLayoutTopics.newTab().apply {
                text = topicTabsList[topicTabsList.lastIndex]
//                this.parent?.requestFocus()
            },
            /* position = */ position,
            /* setSelected = */ true
        )
//        binding.viewpagerReminders.adapter?.notifyItemInserted(topicTabsList.lastIndex)
        binding.viewpagerReminders.adapter?.notifyDataSetChanged()
//        binding.tabLayoutTopics.getTabAt(position)?.select()
//        binding.tabLayoutTopics.get(position).requestFocus()
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
        binding.btnWebsiteQuickActions.setOnTouchListener { v, event ->
            binding.pinterestView.dispatchTouchEvent(event)
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
            register(binding.btnWebsiteQuickActions)
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
                QuickActionTabMenu.CLOSE_TAB.ordinal -> {
                    if (topicTabsList.size == 1) {
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    } else {
                        topicTabsList.removeAt(binding.tabLayoutTopics.selectedTabPosition)
                        binding.tabLayoutTopics.removeTabAt(binding.tabLayoutTopics.selectedTabPosition)
//                        binding.viewpagerReminders.adapter?.notifyItemRemoved(binding.tabLayoutTopics.selectedTabPosition)
                        binding.viewpagerReminders.adapter?.notifyDataSetChanged()
                    }
                }
                QuickActionTabMenu.REFRESH_TAB.ordinal -> {}
                QuickActionTabMenu.SHARE_TAB.ordinal -> {}
            }
        }
    }

    fun setLinearProgress(progress: Int) {
        if (this::binding.isInitialized) {
            binding.linearProgress.progress = progress
        }
    }

    fun showLinearProgress(isShow: Boolean) {
        if (this::binding.isInitialized) {
            binding.linearProgress.isVisible = isShow
        }
    }

    private fun showPopupMenu(
        view: View,
        @MenuRes menuRes: Int
    ) {
        PopupMenu(requireContext(), view).apply {
            this.menu.invokeSetMenuIconMethod()
            menuInflater.inflate(menuRes, this.menu)
            setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.menu_item_new_private_tab -> {
                        binding.etSearch.showKeyboard()
                        addTopic("New Private Tab".capFirstChar())
                        binding.etSearch.setSelection(0, binding.etSearch.text.length)
                        binding.etSearch.setSelectAllOnFocus(true)
                        false
                    }
                    R.id.menu_item_new_disappearing_tab -> {
                        binding.etSearch.showKeyboard()
                        addTopic("New Disappearing Tab".capFirstChar())
                        binding.etSearch.setSelection(0, binding.etSearch.text.length)
                        binding.etSearch.setSelectAllOnFocus(true)
                        false
                    }
                    else -> false
                }
            }
            setOnDismissListener { it: PopupMenu? ->
            }
            setMarginBtwMenuIconAndText(
                context = requireContext(),
                menu = this.menu,
                iconMarginDp = 10
            )
            this.menu.forEach { it: MenuItem ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.iconTintList = ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
                }
            }
            show()
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
