package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentSearchBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import com.singularitycoder.connectme.helpers.searchSuggestions.*
import com.singularitycoder.flowlauncher.helper.pinterestView.CircleImageView
import com.singularitycoder.flowlauncher.helper.pinterestView.PinterestView
import com.singularitycoder.flowlauncher.helper.quickActionView.Action
import com.singularitycoder.flowlauncher.helper.quickActionView.QuickActionView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO drag and drop the tabs outside the tab row to close the tabs

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = SearchFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentSearchBinding

    private val topicTabsList = mutableListOf<String>()
    private val searchViewModel: SearchViewModel by viewModels()
    private val iconTextActionAdapter by lazy { IconTextActionAdapter() }

    private var searchQuery: String = ""
    private var topicParam: String? = null
    private var isSearchSuggestionSelected: Boolean = false

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setUpUserActionListeners()
        binding.observeForData()
        binding.ibAddTab.performClick()
    }

    override fun onResume() {
        super.onResume()
        if (topicTabsList.size == 1 && binding.etSearch.text.isBlank()) {
            binding.etSearch.showKeyboard()
        }
    }

    private fun FragmentSearchBinding.setupUI() {
        binding.etSearch.setText("https://www.github.com")
        val selectedSearchEngine = preferences.getString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.GOOGLE.name)
        ivSearchEngine.setImageResource(SearchEngine.valueOf(selectedSearchEngine ?: SearchEngine.GOOGLE.name).icon)
        layoutVpn.apply {
            layoutIconText.tvText.text = "Enable VPN"
//            layoutIconText.tvText.setTypeface(tvText.typeface, Typeface.BOLD)
            layoutIconText.ivIcon.setImageDrawable(requireActivity().drawable(R.drawable.round_vpn_key_24))
            layoutIconText.ivIcon.setMargins(start = 0, top = -2.dpToPx().toInt(), end = 0, bottom = 0)
            layoutIconText.ivIcon.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
        layoutAdBlocker.apply {
            layoutIconText.tvText.text = "Enable Ad Blocker"
//            layoutIconText.tvText.setTypeface(tvText.typeface, Typeface.BOLD)
            layoutIconText.ivIcon.setImageDrawable(requireActivity().drawable(R.drawable.outline_block_24))
            layoutIconText.ivIcon.setMargins(start = 0, top = -2.dpToPx().toInt(), end = 0, bottom = 0)
            layoutIconText.ivIcon.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
        }
        layoutCollections.apply {
            tvTitle.text = "Collections"
            tvTitle.setTextColor(requireContext().color(R.color.purple_500))
            ivDropdownArrow.isVisible = true
        }
        layoutFollowing.apply {
            tvTitle.text = "Following"
        }
        listOf(layoutFollowing, layoutCollections).forEach {
            it.apply {
                layoutFollowingApp1.ivAppIcon.load(dummyFaviconUrls[0])
                layoutFollowingApp1.tvAppName.text = "Doodle"
                layoutFollowingApp2.ivAppIcon.load(dummyFaviconUrls[1])
                layoutFollowingApp2.tvAppName.text = "Stupify"
                layoutFollowingApp3.ivAppIcon.load(dummyFaviconUrls[2])
                layoutFollowingApp3.tvAppName.text = "Hitgub"
                layoutFollowingApp4.ivAppIcon.load(dummyFaviconUrls[3])
                layoutFollowingApp4.tvAppName.text = "Coldstar"
            }
        }
        layoutHistory.apply {
            tvTitle.text = "History"
        }
        layoutDownloads.apply {
            tvTitle.text = "Downloads"
        }
        setupSearchSuggestionsRecyclerView()
        setUpViewPager()
        setupLinkEditingFeatures()
    }

    private fun FragmentSearchBinding.setUpUserActionListeners() {
        root.setOnClickListener { }

        layoutCollections.apply {
            viewDummyForDropdown.onSafeClick {
                val collectionsList = listOf("Collection 1", "Collection 2", "Collection 3")
                val adapter = ArrayAdapter(
                    /* context = */ requireContext(),
                    /* resource = */ android.R.layout.simple_list_item_1,
                    /* objects = */ collectionsList
                )
                ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle).apply {
                    anchorView = it.first
                    setAdapter(adapter)
                    setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                        layoutCollections.tvTitle.text = collectionsList[position]
                        this.dismiss()
                    }
                    show()
                }
            }
            tvTitle.setOnClickListener {
                layoutCollections.viewDummyForDropdown.performClick()
            }
            ivDropdownArrow.setOnClickListener {
                layoutCollections.tvTitle.performClick()
            }
            clShowMore.setOnClickListener {
                layoutCollections.ivShowMore.performClick()
            }
            ivShowMore.onSafeClick {
                requireContext().showToast("Show more")
            }
        }

        layoutVpn.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        layoutAdBlocker.apply {
            root.onSafeClick { switchOnOff.performClick() }
            switchOnOff.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                } else {
                    switchOnOff.thumbTintList = ColorStateList.valueOf(requireContext().color(R.color.white))
                }
            }
        }

        setTabMenuTouchOptions()

        ivWebappProfile.onSafeClick {
            WebsiteActionsBottomSheetFragment.newInstance().show(
                /* manager = */ requireActivity().supportFragmentManager,
                /* tag = */ BottomSheetTag.WEBSITE_ACTIONS
            )
        }

        ivSearchEngine.onSafeClick {
            dummyView.performClick()
        }

        dummyView.setOnClickListener {
            val adapter = ArrayAdapter(
                /* context = */ requireContext(),
                /* resource = */ android.R.layout.simple_list_item_1,
                /* objects = */ SearchEngine.values().map { it.value }
            )
            ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle).apply {
                anchorView = it
                setAdapter(adapter)
                setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                    ivSearchEngine.setImageResource(SearchEngine.values()[position].icon)
                    preferences.edit().putString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.values()[position].name).apply()
                    searchViewModel.getSearchSuggestions(searchQuery)
                    this.dismiss()
                }
                show()
            }
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
            etSearch.showKeyboard()
            addTab(NewTabType.NEW_TAB.value)
            etSearch.setSelection(0, etSearch.text.length)
            etSearch.setSelectAllOnFocus(true)
        }

        btnVoiceSearch.onSafeClick {
            requireContext().showToast("Voice Search")
        }

        btnQrScan.onSafeClick {
            requireContext().showToast("QR Scan")
        }

        etSearch.onImeClick(imeAction = EditorInfo.IME_ACTION_SEARCH) {
            etSearch.hideKeyboard()
        }

        etSearch.setOnFocusChangeListener { view, isFocused ->
            scrollViewNewTabOptions.isVisible = isFocused
            if (isFocused) {
                doWhenSearchIsFocused()
            } else {
                doWhenSearchIsNotFocused()
            }
        }

        etSearch.doAfterTextChanged { it: Editable? ->
            val query = it.toString().toLowCase().trim()
            searchQuery = query
            if (isSearchSuggestionSelected) {
                rvSearchSuggestions.isVisible = false
                etSearch.clearFocus()
                doWhenSearchIsNotFocused()
                etSearch.hideKeyboard()
                return@doAfterTextChanged
            }
            if (query.isBlank()) {
                rvSearchSuggestions.isVisible = false
                return@doAfterTextChanged
            }
            searchViewModel.getSearchSuggestions(query)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showCloseAllTabsPopup()
            }
        })

        // https://stackoverflow.com/questions/25216749/soft-keyboard-open-and-close-listener-in-an-activity-in-android
        etSearch.viewTreeObserver.addOnGlobalLayoutListener {
            if (root.isKeyboardHidden()) {
                etSearch.clearFocus()
            } else {
                etSearch.requestFocus()
            }
        }

        iconTextActionAdapter.setOnItemClickListener { iconTextAction: IconTextAction ->
            isSearchSuggestionSelected = true
            etSearch.clearFocus()
            etSearch.setText(iconTextAction.title)
        }
    }

    private fun FragmentSearchBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = searchViewModel.searchSuggestionResultsStateFlow) { searchSuggestionsList: List<String> ->
            if (searchSuggestionsList.isEmpty()) {
                rvSearchSuggestions.isVisible = false
                return@collectLatestLifecycleFlow
            }
            if (searchQuery.isBlank()) {
                rvSearchSuggestions.isVisible = false
                return@collectLatestLifecycleFlow
            }
            rvSearchSuggestions.isVisible = true
            val rect = Rect() // rect will be populated with the coordinates of your view that area still visible.
            root.getWindowVisibleDisplayFrame(rect)
            val heightDiff: Int = root.rootView.height - (rect.bottom - rect.top)
            if (searchSuggestionsList.size > 5) {
                rvSearchSuggestions.layoutParams.height = heightDiff - 32.dpToPx().toInt()
            } else {
                rvSearchSuggestions.layoutParams.height = LayoutParams.WRAP_CONTENT
            }
            val selectedSearchEngine = preferences.getString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.GOOGLE.name)
            val searchEngine = SearchEngine.valueOf(selectedSearchEngine ?: SearchEngine.GOOGLE.name)
            iconTextActionAdapter.setQuery(query = searchQuery)
            iconTextActionAdapter.setList(searchSuggestionsList.map { searchSuggestion: String ->
                IconTextAction(
                    title = searchSuggestion,
                    icon = searchEngine.icon,
                    actionIcon = R.drawable.north_west_black_24dp
                )
            })
        }
    }

    private fun FragmentSearchBinding.setupSearchSuggestionsRecyclerView() {
        rvSearchSuggestions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = iconTextActionAdapter
        }
    }

    private fun FragmentSearchBinding.setUpViewPager() {
        viewpagerReminders.apply {
            isUserInputEnabled = false
            adapter = SearchViewPagerAdapter(
                fragmentManager = requireActivity().supportFragmentManager,
                lifecycle = lifecycle
            )
            registerOnPageChangeCallback(viewPager2PageChangeListener)
        }
        TabLayoutMediator(tabLayoutTabs, viewpagerReminders) { tab, position ->
            tab.text = topicTabsList[position]
            tab.icon = when (topicTabsList[position]) {
                NewTabType.NEW_PRIVATE_DISAPPEARING_TAB.value -> requireContext().drawable(R.drawable.outline_policy_24)
                NewTabType.NEW_PRIVATE_TAB.value -> requireContext().drawable(R.drawable.outline_policy_24)
                NewTabType.NEW_DISAPPEARING_TAB.value -> requireContext().drawable(R.drawable.outline_timer_24)
                else -> null
            }
        }.attach()
    }

    private fun FragmentSearchBinding.setupLinkEditingFeatures() {
        listOf("Copy", "Share", "   /   ", "   .   ", ".com", "  .in  ", "www.", "https://", "http://", "")
            .forEach { it: String ->
                val chip = Chip(requireContext()).apply {
                    text = it
                    isCheckable = false
                    isClickable = false
                    when (it) {
                        "Copy" -> {
                            setTextColor(requireContext().color(R.color.purple_500))
                            chipBackgroundColor = ColorStateList.valueOf(requireContext().color(R.color.purple_50))
                            chipIcon = requireContext().drawable(R.drawable.baseline_content_copy_24)
                            chipIconSize = 16.dpToPx()
                            chipIconTint = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                            iconStartPadding = 6.dpToPx()
                        }
                        "Share" -> {
                            setTextColor(requireContext().color(R.color.purple_500))
                            chipBackgroundColor = ColorStateList.valueOf(requireContext().color(R.color.purple_50))
                            chipIcon = requireContext().drawable(R.drawable.round_share_24)
                            chipIconSize = 16.dpToPx()
                            chipIconTint = ColorStateList.valueOf(requireContext().color(R.color.purple_500))
                            iconStartPadding = 6.dpToPx()
                        }
                        "" -> {
                            chipBackgroundColor = ColorStateList.valueOf(requireContext().color(R.color.transparent_white_50))
                        }
                        else -> {
                            setTextColor(requireContext().color(R.color.title_color))
                            chipBackgroundColor = ColorStateList.valueOf(requireContext().color(R.color.black_50))
                        }
                    }
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    elevation = 0f
                    onSafeClick {
                    }
                }
                chipGroupLinkTextActions.addView(chip)
            }
    }

    private fun FragmentSearchBinding.doWhenSearchIsFocused() {
            clTabs.isVisible = false
            isSearchSuggestionSelected = false
            chipGroupLinkTextActions.isVisible = true
            etSearch.setSelection(0, etSearch.text.length)
            etSearch.setSelectAllOnFocus(true)
            btnWebsiteQuickActions.isVisible = false
            btnQrScan.isVisible = true
            btnVoiceSearch.isVisible = true
            ivWebappProfile.isVisible = false
            ivSearchEngine.isVisible = true
    }

    private fun FragmentSearchBinding.doWhenSearchIsNotFocused() {
            clTabs.isVisible = true
            chipGroupLinkTextActions.isVisible = false
            etSearch.clearFocus()
            btnWebsiteQuickActions.isVisible = true
            btnQrScan.isVisible = false
            btnVoiceSearch.isVisible = false
            ivWebappProfile.isVisible = true
            ivSearchEngine.isVisible = false
            rvSearchSuggestions.isVisible = false
    }

    private fun FragmentSearchBinding.showCloseAllTabsPopup() {
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

    // https://stackoverflow.com/questions/13445594/data-sharing-between-fragments-and-activity-in-android
    // https://stackoverflow.com/questions/34562117/how-do-i-change-the-color-of-icon-of-the-selected-tab-of-tablayout
    // https://stackoverflow.com/questions/50496593/show-popup-on-long-click-on-selected-tab-of-tablayout
    // https://stackoverflow.com/questions/37833495/add-iconstext-to-tablayout
    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentSearchBinding.addTab(topic: String) {
        topicTabsList.add(topic)
        val currentTabPosition = if (topicTabsList.isNotEmpty()) topicTabsList.lastIndex else 0
        val newTab = tabLayoutTabs.newTab().apply {
            text = topicTabsList[topicTabsList.lastIndex] // Actual setting happens here TabLayoutMediator
        }
        tabLayoutTabs.addTab(
            /* tab = */ newTab,
            /* position = */ currentTabPosition,
            /* setSelected = */ true
        )
        tabLayoutTabs.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
        tabLayoutTabs.getTabAt(currentTabPosition)?.view?.setOnLongClickListener {
            fun closeOtherTabs() {
                try {
                    // FIXME not working
                    for (position: Int in 0 until tabLayoutTabs.tabCount) {
                        if (position == currentTabPosition) continue
                        topicTabsList.removeAt(position)
                        tabLayoutTabs.removeTabAt(position)
                    }
                } catch (_: Exception) {
                }
            }

            fun closeTab() {
                if (topicTabsList.size == 1) {
                    requireActivity().supportFragmentManager.popBackStackImmediate()
                } else {
                    topicTabsList.removeAt(binding.tabLayoutTabs.selectedTabPosition)
                    binding.tabLayoutTabs.removeTabAt(binding.tabLayoutTabs.selectedTabPosition)
                    binding.viewpagerReminders.adapter?.notifyDataSetChanged()
                }
            }

            fun duplicateTab() {
                addTab(NewTabType.NEW_TAB.value)
            }

            val tabOptionsList = if (tabLayoutTabs.tabCount == 1) {
                listOf(
                    "Duplicate Tab",
                    "Close Tab"
                )
            } else {
                listOf(
                    "Close Tabs to the Left",
                    "Close Tabs to the Right",
                    "Close Other Tabs",
                    "New Tab to the Left",
                    "New Tab to the Right",
                    "Duplicate Tab",
                    "Close Tab",
                )
            }

            requireContext().showPopup(
                view = it,
                menuList = tabOptionsList
            ) { menuPosition: Int ->
                if (tabOptionsList.size == 2) {
                    when (tabOptionsList[menuPosition]) {
                        tabOptionsList[0] -> closeTab()
                        tabOptionsList[1] -> duplicateTab()
                    }
                } else {
                    when (tabOptionsList[menuPosition]) {
                        tabOptionsList[0] -> closeOtherTabs()
                        tabOptionsList[1] -> closeTab()
                        tabOptionsList[2] -> duplicateTab()
                    }
                }
            }
            true
        }
        tabLayoutTabs.getTabAt(currentTabPosition)?.view?.onSafeClick {
//            root.showSnackBar(
//                message = tabLayoutTabs.getTabAt(currentTabPosition)?.text.toString(),
//                anchorView = tabLayoutTabs,
//                isAnimated = false
//            )
        }
        viewpagerReminders.adapter?.notifyDataSetChanged()
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
            createChildView(R.drawable.round_close_24, QuickActionTabMenu.CLOSE_ALL_TABS.value, R.color.purple_50),
            createChildView(R.drawable.round_refresh_24, QuickActionTabMenu.REFRESH_WEBSITE.value, R.color.purple_50),
            createChildView(R.drawable.round_share_24, QuickActionTabMenu.SHARE_LINK.value, R.color.purple_50),
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

        val icon2dot5 = requireContext().drawable(R.drawable.outline_library_add_24)?.changeColor(requireContext(), R.color.purple_500)
        val action2dot5 = Action(id = QuickActionTabMenu.COLLECT_ALL_TABS.ordinal, icon = icon2dot5!!, title = QuickActionTabMenu.COLLECT_ALL_TABS.value)

//        val icon3 = requireContext().drawable(R.drawable.round_share_24)?.changeColor(requireContext(), R.color.purple_500)
//        val action3 = Action(id = QuickActionTabMenu.SHARE_LINK.ordinal, icon = icon3!!, title = QuickActionTabMenu.SHARE_LINK.value)

        val icon4 = requireContext().drawable(R.drawable.round_close_24)?.changeColor(requireContext(), R.color.purple_500)
        val action4 = Action(id = QuickActionTabMenu.CLOSE_ALL_TABS.ordinal, icon = icon4!!, title = QuickActionTabMenu.CLOSE_ALL_TABS.value)

        val icon5 = requireContext().drawable(R.drawable.round_refresh_24)?.changeColor(requireContext(), R.color.purple_500)
        val action5 = Action(id = QuickActionTabMenu.REFRESH_WEBSITE.ordinal, icon = icon5!!, title = QuickActionTabMenu.REFRESH_WEBSITE.value)

        val icon6 = requireContext().drawable(R.drawable.round_arrow_forward_24)?.changeColor(requireContext(), R.color.purple_500)
        val action6 = Action(id = QuickActionTabMenu.NAVIGATE_FORWARD.ordinal, icon = icon6!!, title = QuickActionTabMenu.NAVIGATE_FORWARD.value)

        val addFabQuickActionView = QuickActionView.make(requireContext()).apply {
            addAction(action1)
            addAction(action2)
            addAction(action2dot5)
//            addAction(action3)
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
                QuickActionTabMenu.CLOSE_ALL_TABS.ordinal -> {
                    binding.showCloseAllTabsPopup()
                }
                QuickActionTabMenu.REFRESH_WEBSITE.ordinal -> {}
//                QuickActionTabMenu.SHARE_LINK.ordinal -> {}
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

    private fun FragmentSearchBinding.showPopupMenu(
        view: View,
        @MenuRes menuRes: Int
    ) {
        PopupMenu(requireContext(), view).apply {
            this.menu.invokeSetMenuIconMethod()
            menuInflater.inflate(menuRes, this.menu)
            setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.menu_item_new_private_disappearing_tab -> {
                        etSearch.showKeyboard()
                        addTab(NewTabType.NEW_PRIVATE_DISAPPEARING_TAB.value)
                        etSearch.setSelection(0, etSearch.text.length)
                        etSearch.setSelectAllOnFocus(true)
                        false
                    }
                    R.id.menu_item_new_private_tab -> {
                        etSearch.showKeyboard()
                        addTab(NewTabType.NEW_PRIVATE_TAB.value)
                        etSearch.setSelection(0, etSearch.text.length)
                        etSearch.setSelectAllOnFocus(true)
                        false
                    }
                    R.id.menu_item_new_disappearing_tab -> {
                        etSearch.showKeyboard()
                        addTab(NewTabType.NEW_DISAPPEARING_TAB.value)
                        etSearch.setSelection(0, etSearch.text.length)
                        etSearch.setSelectAllOnFocus(true)
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

    fun getSearchInputField(): EditText = binding.etSearch

    fun getFaviconImageView(): ShapeableImageView = binding.ivWebappProfile

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
