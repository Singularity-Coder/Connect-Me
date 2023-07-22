package com.singularitycoder.connectme.search.view

import android.annotation.SuppressLint
import android.app.ActionBar
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
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
import com.singularitycoder.connectme.collections.CollectionWebPage
import com.singularitycoder.connectme.collections.CollectionsViewModel
import com.singularitycoder.connectme.collections.CreateCollectionBottomSheetFragment
import com.singularitycoder.connectme.collections.LinksCollection
import com.singularitycoder.connectme.databinding.FragmentSearchBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import com.singularitycoder.connectme.history.History
import com.singularitycoder.connectme.history.HistoryViewModel
import com.singularitycoder.connectme.search.model.*
import com.singularitycoder.connectme.search.view.addApiKey.AddApiKeyBottomSheetFragment
import com.singularitycoder.connectme.search.view.getInsights.GetInsightsBottomSheetFragment
import com.singularitycoder.connectme.search.view.websiteActions.WebsiteActionsBottomSheetFragment
import com.singularitycoder.connectme.search.viewmodel.SearchViewModel
import com.singularitycoder.flowlauncher.helper.pinterestView.CircleImageView
import com.singularitycoder.flowlauncher.helper.pinterestView.PinterestView
import com.singularitycoder.flowlauncher.helper.quickActionView.Action
import com.singularitycoder.flowlauncher.helper.quickActionView.QuickActionView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO drag and drop the tabs outside the tab row to close the tabs
private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
private const val ARG_PARAM_WEBSITE_LIST = "ARG_PARAM_WEBSITE_LIST"

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            screenType: String = "",
            websiteList: ArrayList<SearchTab?> = ArrayList()
        ) = SearchFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM_SCREEN_TYPE, screenType)
                putParcelableArrayList(ARG_PARAM_WEBSITE_LIST, websiteList.toArrayList())
            }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentSearchBinding

    private val webSearchTabsList = mutableListOf<SearchTab?>()
    private val linksCollectionsList = mutableListOf<LinksCollection?>()
    private val collectionsTitlesList = mutableListOf<String?>()

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val collectionsViewModel by viewModels<CollectionsViewModel>()
    private val historyViewModel by viewModels<HistoryViewModel>()
    private val iconTextActionAdapter by lazy { IconTextActionAdapter() }

    private var searchQuery: String = ""
    private var topicParam: String? = null
    private var websiteList: List<SearchTab?> = emptyList()
    private var isSearchSuggestionSelected: Boolean = false
    private var isWebsiteTitleSet: Boolean = false

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) = Unit
        override fun onPageSelected(position: Int) = Unit
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            binding.doOnTabClick()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
        websiteList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList(ARG_PARAM_WEBSITE_LIST, SearchTab::class.java) ?: emptyList()
        } else {
            arguments?.getParcelableArrayList(ARG_PARAM_WEBSITE_LIST) ?: emptyList()
        }
        websiteList.forEach { webSearchTabsList.add(it) }
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
        if (websiteList.isEmpty()) binding.ibAddTab.performClick()
    }

    override fun onResume() {
        super.onResume()
        if (webSearchTabsList.size == 1 && binding.etSearch.text.isBlank()) {
            binding.etSearch.showKeyboard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ConnectMeUtils.webpageFragmentIdList.clear()
    }

    private fun FragmentSearchBinding.setupUI() {
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
            viewDummyTitle.isVisible = true
        }
        layoutFollowing.tvTitle.text = "Following"
        layoutHistory.tvTitle.text = "History"
        layoutDownloads.tvTitle.text = "Downloads"
        layoutFollowing.apply {
            layoutFollowingApp1.ivAppIcon.load(DUMMY_FAVICON_URLS[0])
            layoutFollowingApp1.tvAppName.text = "Doodle"
            layoutFollowingApp2.ivAppIcon.load(DUMMY_FAVICON_URLS[1])
            layoutFollowingApp2.tvAppName.text = "Stupify"
            layoutFollowingApp3.ivAppIcon.load(DUMMY_FAVICON_URLS[2])
            layoutFollowingApp3.tvAppName.text = "Hitgub"
            layoutFollowingApp4.ivAppIcon.load(DUMMY_FAVICON_URLS[3])
            layoutFollowingApp4.tvAppName.text = "Coldstar"
        }
        lifecycleScope.launch {
            if (collectionsTitlesList.isEmpty()) {
                collectionsTitlesList.addAll(collectionsViewModel.getAllUniqueCollectionTitles())
            }
            val top4CollectionItemsList = collectionsViewModel.getTop4CollectionsBy(collectionTitle = collectionsTitlesList.firstOrNull())
            withContext(Main) {
                layoutCollections.tvTitle.text = collectionsTitlesList.firstOrNull()
                updateTop4WebPageViewsOfCollections(top4CollectionItemsList)
            }
        }
        tabLayoutTabs.addOnTabSelectedListener(tabSelectedListener)
        setupSearchSuggestionsRecyclerView()
        setUpViewPager()
        setupLinkEditingFeatures()
    }

    private fun FragmentSearchBinding.setUpUserActionListeners() {
        root.setOnClickListener { }

        layoutCollections.apply {
            viewDummyForDropdown.onSafeClick {
                lifecycleScope.launch {
                    if (collectionsTitlesList.isEmpty()) {
                        collectionsTitlesList.addAll(collectionsViewModel.getAllUniqueCollectionTitles())
                    }
                    withContext(Main) {
                        val adapter = ArrayAdapter(
                            /* context = */ requireContext(),
                            /* resource = */ android.R.layout.simple_list_item_1,
                            /* objects = */ collectionsTitlesList
                        )
                        ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle).apply {
                            anchorView = it.first
                            setAdapter(adapter)
                            setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                                layoutCollections.tvTitle.text = collectionsTitlesList[position]
                                lifecycleScope.launch {
                                    val top4CollectionItemsList = collectionsViewModel.getTop4CollectionsBy(collectionTitle = collectionsTitlesList[position])
                                    withContext(Main) {
                                        updateTop4WebPageViewsOfCollections(top4CollectionItemsList)
                                    }
                                }
                                this.dismiss()
                            }
                            show()
                        }
                    }
                }
            }
            tvTitle.setOnClickListener {
                layoutCollections.viewDummyForDropdown.performClick()
            }
            viewDummyTitle.setOnClickListener {
                tvTitle.performClick()
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

        viewSearchSuggestionsScrim.onSafeClick {
            cardSearchSuggestions.isVisible = false
            viewSearchSuggestionsScrim.isVisible = false
        }

        cardSearchSuggestions.setOnVisibilityChangedListener { it: Boolean ->
            viewSearchSuggestionsScrim.isVisible = it
        }

        ivSearchEngine.onSafeClick {
            val popupMenu = android.widget.PopupMenu(requireContext(), it.first)
            SearchEngine.values().forEach { it: SearchEngine ->
                popupMenu.menu.add(
                    0, 1, 1, menuIconWithText(
                        icon = requireContext().drawable(it.icon),
                        title = it.value,
                        iconWidth = 24.dpToPx().toInt(),
                        iconHeight = 24.dpToPx().toInt(),
                        defaultSpace = "      "
                    )
                )
            }
            popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
                view?.setHapticFeedback()
                ivSearchEngine.setImageResource(SearchEngine.getEngineBy(it?.title?.toString()).icon)
                preferences.edit().putString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.getEngineBy(it?.title?.toString()).name).apply()
                searchViewModel.getSearchSuggestions(searchQuery)
                popupMenu.dismiss()
                false
            }
            popupMenu.show()
        }

        cardAddTab.onSafeClick {
            ibAddTab.performClick()
        }

        cardAddTab.onCustomLongClick {
            ibAddTab.performLongClick()
        }

        ibAddTab.onCustomLongClick {
            showAddTabPopupMenu(view = it, menuRes = R.menu.new_tab_popup_menu)
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

        /** The webpage loads in [SearchTabFragment] */
        etSearch.onImeClick(imeAction = EditorInfo.IME_ACTION_SEARCH) {
            if (etSearch.text.isNullOrBlank()) return@onImeClick
            val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList[tabLayoutTabs.selectedTabPosition]) as? SearchTabFragment
            selectedWebpage?.loadUrl(url = etSearch.text.toString().trim())
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
//            llTabActionButtons.isVisible = query.isBlank()
            if (isSearchSuggestionSelected) {
                isSearchSuggestionSelected = false
                cardSearchSuggestions.isVisible = false
                etSearch.clearFocus()
                doWhenSearchIsNotFocused()
                etSearch.hideKeyboard()
                return@doAfterTextChanged
            }
            if (query.isBlank()) {
                cardSearchSuggestions.isVisible = false
                return@doAfterTextChanged
            }
            if (isWebsiteTitleSet) {
                isWebsiteTitleSet = false
                cardSearchSuggestions.isVisible = false
                return@doAfterTextChanged
            }
            searchViewModel.getSearchSuggestions(query)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                if (selectedWebpage?.getWebView()?.canGoBack() == true) {
                    selectedWebpage.getWebView().goBack()
                } else {
                    showCloseAllTabsPopup()
                }
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
            etSearch.setText(iconTextAction.title)
            etSearch.clearFocus()
            val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList[tabLayoutTabs.selectedTabPosition]) as? SearchTabFragment
            selectedWebpage?.loadUrl(url = iconTextAction.title)
        }
    }

    private fun FragmentSearchBinding.observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = searchViewModel.searchSuggestionResultsStateFlow) { searchSuggestionsList: List<String> ->
            if (searchSuggestionsList.isEmpty() || searchQuery.isBlank()) {
                cardSearchSuggestions.isVisible = false
                return@collectLatestLifecycleFlow
            }
            try {
                cardSearchSuggestions.isVisible = etSearch.isKeyboardVisible()
            } catch (_: Exception) {
            }
            val rect = Rect() // rect will be populated with the coordinates of your view that area still visible.
            root.getWindowVisibleDisplayFrame(rect)
            val heightDiff: Int = root.rootView.height - (rect.bottom - rect.top)
            if (searchSuggestionsList.size > 5) {
                rvSearchSuggestions.layoutParams.height = heightDiff - 32.dpToPx().toInt()
            } else {
                rvSearchSuggestions.layoutParams.height = ActionBar.LayoutParams.WRAP_CONTENT
            }
            val selectedSearchEngine = preferences.getString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.GOOGLE.name)
            val searchEngine = SearchEngine.valueOf(selectedSearchEngine ?: SearchEngine.GOOGLE.name)
            iconTextActionAdapter.setQuery(query = searchQuery)
            iconTextActionAdapter.setList(searchSuggestionsList.map { searchSuggestion: String ->
                IconTextAction(
                    title = searchSuggestion,
                    icon = searchEngine.icon,
                    actionIcon = R.drawable.round_south_west_24
                )
            })
        }

        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = searchViewModel.insightSharedFlow) { it: ApiResult ->
            if (it.apiState == ApiState.NONE) return@collectLatestLifecycleFlow
            if (it.screen != this@SearchFragment.javaClass.simpleName) return@collectLatestLifecycleFlow

            when (it.apiState) {
                ApiState.SUCCESS -> {
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList[tabLayoutTabs.selectedTabPosition]) as? SearchTabFragment
                    val promptsJson = "{" + it.insight?.insight?.substringAfter("{")?.substringBefore("}")?.trim() + "}"
                    searchViewModel.addPrompt(
                        Prompt(
                            website = getHostFrom(url = selectedWebpage?.getWebView()?.url),
                            promptsJson = promptsJson
                        )
                    )
                }
                else -> Unit
            }

            searchViewModel.resetInsight()
        }

        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = collectionsViewModel.getAllCollections()) { it: List<CollectionWebPage?> ->
            val collectionsMap = HashMap<String?, java.util.ArrayList<CollectionWebPage?>>()
            it.forEach {
                val collectionWebPageList = (collectionsMap.get(it?.collectionTitle) ?: java.util.ArrayList()).apply { add(it) }
                collectionsMap.put(it?.collectionTitle, collectionWebPageList)
            }
            collectionsMap.keys.forEach { key: String? ->
                val linksCollection = LinksCollection(
                    title = key,
                    count = collectionsMap[key]?.size ?: 0,
                    linkList = collectionsMap[key] ?: emptyList()
                )
                linksCollectionsList.add(linksCollection)
            }
        }
    }

    private fun FragmentSearchBinding.setupSearchSuggestionsRecyclerView() {
        rvSearchSuggestions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = iconTextActionAdapter
        }
    }

    private fun FragmentSearchBinding.setUpViewPager() {
        viewpagerTabs.apply {
            isUserInputEnabled = false
            adapter = SearchViewPagerAdapter(
                fragmentManager = requireActivity().supportFragmentManager,
                lifecycle = lifecycle
            )
            registerOnPageChangeCallback(viewPager2PageChangeListener)
        }
        TabLayoutMediator(tabLayoutTabs, viewpagerTabs) { tab, position ->
            tab.text = getDomainFrom(host = getHostFrom(url = webSearchTabsList[position]?.link)) // FIXME update webSearchTabsList when webpage loads for the previous tabs
            tab.icon = when (webSearchTabsList[position]?.type) {
                NewTabType.NEW_PRIVATE_DISAPPEARING_TAB -> requireContext().drawable(R.drawable.outline_policy_24)
                NewTabType.NEW_PRIVATE_TAB -> requireContext().drawable(R.drawable.outline_policy_24)
                NewTabType.NEW_DISAPPEARING_TAB -> requireContext().drawable(R.drawable.outline_timer_24)
                else -> null
            }
            tab.view.onSafeClick {
                doOnTabClick()
            }
            tab.view.onCustomLongClick {
                doOnTabLongClick(currentTabPosition = tabLayoutTabs.selectedTabPosition, tabView = it)
            }
        }.attach()
    }

    private fun FragmentSearchBinding.setupLinkEditingFeatures() {
        listOf("Copy", "Share", "   /   ", "   .   ", ".com", "  .in  ", "www.", "https://", "http://", "")
            .forEach { action: String ->
                val chip = Chip(requireContext()).apply {
                    text = action
                    isCheckable = false
                    isClickable = false
                    when (action) {
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
                            chipIcon = requireContext().drawable(R.drawable.outline_share_24)
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
                        when (action.trim()) {
                            "Copy" -> {
                                if (etSearch.text.isNullOrBlank().not()) {
                                    requireContext().clipboard()?.text = etSearch.text
                                    binding.root.showSnackBar("Copied link: ${requireContext().clipboard()?.text}")
                                }
                            }
                            "Share" -> {
                                val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList[tabLayoutTabs.selectedTabPosition]) as? SearchTabFragment
                                // Delay a bit to allow popup menu hide animation to play
                                doAfter(300) {
                                    requireContext().shareUrl(
                                        url = etSearch.text.toString(),
                                        webView = selectedWebpage?.getWebView()
                                    )
                                }
                            }
                            "/" -> {
                                etSearch.text.insert(etSearch.selectionStart, "/")
                            }
                            "." -> {
                                etSearch.text.insert(etSearch.selectionStart, ".")
                            }
                            ".com" -> {
                                etSearch.text.insert(etSearch.selectionStart, ".com")
                            }
                            ".in" -> {
                                etSearch.text.insert(etSearch.selectionStart, ".in")
                            }
                            "www." -> {
                                etSearch.text.insert(etSearch.selectionStart, "www.")
                            }
                            "https://" -> {
                                etSearch.text.insert(etSearch.selectionStart, "https://")
                            }
                            "http://" -> {
                                etSearch.text.insert(etSearch.selectionStart, "http://")
                            }
                        }
                    }
                }
                chipGroupLinkTextActions.addView(chip)
            }
    }

    private fun FragmentSearchBinding.doWhenSearchIsFocused() {
        clWebsiteProfile.isVisible = true
        clTabs.isVisible = false
        isSearchSuggestionSelected = false
        chipGroupLinkTextActions.isVisible = true
        btnWebsiteQuickActions.isVisible = false
        btnQrScan.isVisible = true
        btnVoiceSearch.isVisible = true
        ivWebappProfile.isVisible = false
        ivSearchEngine.isVisible = true
        try {
            val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
            etSearch.setText(selectedWebpage?.getWebView()?.url)
        } catch (_: Exception) {
        }
        etSearch.setSelection(0, etSearch.text.length)
        etSearch.setSelectAllOnFocus(true)
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
        llTabActionButtons.isVisible = true
        try {
            val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
            clWebsiteProfile.isVisible = selectedWebpage?.getWebView()?.url.isNullOrBlank().not()
            etSearch.setText(selectedWebpage?.getWebView()?.title)
            etSearch.setSelection(0) // Adjusts the url back to the start if it is a long url
            isWebsiteTitleSet = true
            scrollViewNewTabOptions.isVisible = selectedWebpage?.isWebpageLoadedAtLeastOnce?.not() == true
        } catch (_: Exception) {
        }
        cardSearchSuggestions.isVisible = false
    }

    fun doOnWebPageLoaded() {
        val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
        binding.scrollViewNewTabOptions.isVisible = false
        websiteStuffToLoad(selectedWebpage)
        val tabText = if (selectedWebpage?.getWebView()?.title.isNullOrBlank().not()) {
            selectedWebpage?.getWebView()?.title
        } else NewTabType.NEW_TAB.value
        binding.tabLayoutTabs.getTabAt(binding.tabLayoutTabs.selectedTabPosition)?.text = if ((tabText?.length ?: 0) > 10) {
            tabText?.substring(0, 10) + "..."
        } else {
            if ((tabText?.length ?: 0) < 5) "$tabText     " else tabText
        }
        binding.etSearch.setSelection(binding.etSearch.selectionStart)
        binding.scrollViewNewTabOptions.isVisible = selectedWebpage?.isWebpageLoadedAtLeastOnce?.not() == true
        binding.viewSearchSuggestionsScrim.isVisible = false
        binding.cardSearchSuggestions.isVisible = false
        searchViewModel.hasPromptList(website = getHostFrom(url = selectedWebpage?.getWebView()?.url)) {
            searchViewModel.getTextInsight(
                prompt = """
                    Give me 10 keywords about ${getHostFrom(url = selectedWebpage?.getWebView()?.url)} and 
                    prepare a json string with the keyword you found as the key and a sensational question prompt 
                    about the keyword as the value. Add an appropriate emoji before the key. 
                    Do not explain anything. Just give me the json string as your answer.
                """.trimIndentsAndNewLines(),
                isSaveToDb = false,
                screen = this@SearchFragment.javaClass.simpleName
            )
        }
        searchViewModel.getTextInsight(
            prompt = """Answer questions that are within the scope of this website 
                    ${getHostFrom(url = selectedWebpage?.getWebView()?.url)} only. The scope can include topics and
                     content related to this website. 
                """.trimIndentsAndNewLines(),
            role = ChatRole.SYSTEM.name.toLowCase(),
            isSaveToDb = false,
            isSendResponse = false,
            screen = this@SearchFragment.javaClass.simpleName
        )
    }

    private fun websiteStuffToLoad(selectedWebpage: SearchTabFragment?) {
//        val simplifiedUrl = selectedWebpage?.getWebView()?.url?.simplifyUrl() ?: ""
        binding.etSearch.setText(selectedWebpage?.getWebView()?.title)
        binding.clWebsiteProfile.isVisible = selectedWebpage?.getWebView()?.url.isNullOrBlank().not()
//        val query = "/${simplifiedUrl.substringAfter("/")}"
//        val styleSpan = TextAppearanceSpan(requireContext(), R.style.url_highlight)
//        binding.etSearch.highlightQueriedText(query = query, result = simplifiedUrl, styleSpan = styleSpan)
    }

    private fun FragmentSearchBinding.showCloseAllTabsPopup() {
        requireContext().showAlertDialog(
            title = "Close all tabs?",
            message = """
                Press "Close" to permanently close all tabs.
                
                Press "SAVE" to retain them in collections.
                 
            """.trimIndent(),
            positiveBtnText = "Close",
            negativeBtnText = "SAVE",
            neutralBtnText = "Cancel",
            positiveAction = {
                etSearch.clearFocus()
                requireActivity().supportFragmentManager.popBackStackImmediate()
            },
            negativeAction = {
                // TODO get all tabs from frag ids
                // TODO construct collectionWebpage obj
                // TODO save obj to db
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
        /** Actual customization happens in TabLayoutMediator */
        etSearch.setText("")
        webSearchTabsList.add(SearchTab(title = topic))
        val currentTabPosition = if (webSearchTabsList.isNotEmpty()) webSearchTabsList.lastIndex else 0
        tabLayoutTabs.addTab(
            /* tab = */ tabLayoutTabs.newTab(),
            /* position = */ tabLayoutTabs.tabCount,
            /* setSelected = */ true
        )
        tabLayoutTabs.tabIndicatorAnimationMode = TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC

        tabLayoutTabs.getTabAt(currentTabPosition)?.view?.onSafeClick {
            doOnTabClick()
        }

        tabLayoutTabs.getTabAt(currentTabPosition)?.view?.onCustomLongClick {
            doOnTabLongClick(currentTabPosition = tabLayoutTabs.selectedTabPosition, tabView = it)
        }

        viewpagerTabs.adapter?.notifyDataSetChanged()

//        tabLayoutTabs.getTabAt(tabLayoutTabs.selectedTabPosition)?.text = webSearchTabsList[webSearchTabsList.lastIndex]?.title
    }

    private fun FragmentSearchBinding.doOnTabClick() {
        val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
        ivWebappProfile.setImageBitmap(selectedWebpage?.getFavicon())
        setWebViewData()
        websiteStuffToLoad(selectedWebpage)
        val tabText = if (selectedWebpage?.getWebView()?.title.isNullOrBlank().not()) {
            selectedWebpage?.getWebView()?.title
        } else NewTabType.NEW_TAB.value
        tabLayoutTabs.getTabAt(tabLayoutTabs.selectedTabPosition)?.text = if ((tabText?.length ?: 0) > 10) {
            tabText?.substring(0, 10) + "..."
        } else tabText
        scrollViewNewTabOptions.isVisible = selectedWebpage?.isWebpageLoadedAtLeastOnce?.not() == true
        //            if (
        //                tabText?.contains(NewTabType.NEW_TAB.value, ignoreCase = true)?.not() == true &&
        //                tabText.contains(NewTabType.NEW_PRIVATE_TAB.value, ignoreCase = true).not() &&
        //                tabText.contains(NewTabType.NEW_DISAPPEARING_TAB.value, ignoreCase = true).not()
        //            ) {
        //                root.showSnackBar(
        //                    message = tabText,
        //                    anchorView = tabLayoutTabs,
        //                    isAnimated = false
        //                )
        //            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentSearchBinding.doOnTabLongClick(currentTabPosition: Int, tabView: View?) {
        fun closeTabsToTheLeft() {
            val tabsToCloseCount = (tabLayoutTabs.tabCount - 1) - tabLayoutTabs.selectedTabPosition
            try {
                for (position: Int in 0 until tabsToCloseCount) {
                    webSearchTabsList.removeAt(position)
                    ConnectMeUtils.webpageFragmentIdList.removeAt(position)
                    tabLayoutTabs.removeTabAt(position)
                    viewpagerTabs.adapter?.notifyDataSetChanged()
                }
            } catch (_: Exception) {
            }
        }

        fun closeTabsToTheRight() {
            val tabsToCloseCount = (tabLayoutTabs.tabCount - 1) - tabLayoutTabs.selectedTabPosition
            try {
                for (position: Int in 0 until tabsToCloseCount) {
                    webSearchTabsList.removeAt(position)
                    ConnectMeUtils.webpageFragmentIdList.removeAt(position)
                    tabLayoutTabs.removeTabAt(position)
                    viewpagerTabs.adapter?.notifyDataSetChanged()
                }
            } catch (_: Exception) {
            }
        }

        fun closeOtherTabs() {
            try {
                for (position: Int in 0 until tabLayoutTabs.tabCount) {
                    if (position == currentTabPosition) continue
                    try {
                        webSearchTabsList.removeAt(position)
                    } catch (_: Exception) {
                    }
                    ConnectMeUtils.webpageFragmentIdList.removeAt(position)
                    tabLayoutTabs.removeTabAt(position)
//                    viewpagerTabs.adapter?.notifyItemRemoved(position)
                    viewpagerTabs.adapter?.notifyDataSetChanged()
                }
            } catch (_: Exception) {
            }
        }

        fun newTabToTheLeft() {

        }

        fun newTabToTheRight() {

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

        requireContext().showPopupMenu(
            view = tabView,
            menuList = tabOptionsList
        ) { menuPosition: Int ->
            if (tabOptionsList.size == 2) {
                when (tabOptionsList[menuPosition]) {
                    tabOptionsList[0] -> duplicateTab()
                    tabOptionsList[1] -> closeTab()
                }
            } else {
                when (tabOptionsList[menuPosition]) {
                    tabOptionsList[0] -> closeTabsToTheLeft()
                    tabOptionsList[1] -> closeTabsToTheRight()
                    tabOptionsList[2] -> closeOtherTabs()
                    tabOptionsList[3] -> newTabToTheLeft()
                    tabOptionsList[4] -> newTabToTheRight()
                    tabOptionsList[5] -> duplicateTab()
                    tabOptionsList[6] -> closeTab()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun closeTab() {
        if (webSearchTabsList.size == 1) {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        } else {
            webSearchTabsList.removeAt(binding.tabLayoutTabs.selectedTabPosition)
            ConnectMeUtils.webpageFragmentIdList.removeAt(binding.tabLayoutTabs.selectedTabPosition)
            binding.tabLayoutTabs.removeTabAt(binding.tabLayoutTabs.selectedTabPosition) // FIXME this alone is not sufficient. Why? Same with recyclerview
//            binding.viewpagerTabs.removeViewAt(binding.tabLayoutTabs.selectedTabPosition)
//            binding.viewpagerTabs.adapter?.notifyItemRemoved(binding.tabLayoutTabs.selectedTabPosition)
            binding.viewpagerTabs.adapter?.notifyDataSetChanged()
//            binding.viewpagerTabs.currentItem = if (binding.tabLayoutTabs.tabCount > 1) {
//                binding.tabLayoutTabs.selectedTabPosition - 1
//            } else 0
        }
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
            createChildView(R.drawable.outline_share_24, QuickActionTabMenu.GET_INSIGHT.value, R.color.purple_50),
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

        val icon3 = requireContext().drawable(R.drawable.baseline_auto_fix_high_24)?.changeColor(requireContext(), R.color.purple_500)
        val action3 = Action(id = QuickActionTabMenu.GET_INSIGHT.ordinal, icon = icon3!!, title = QuickActionTabMenu.GET_INSIGHT.value)

        val icon4 = requireContext().drawable(R.drawable.round_close_24)?.changeColor(requireContext(), R.color.purple_500)
        val action4 = Action(id = QuickActionTabMenu.CLOSE_ALL_TABS.ordinal, icon = icon4!!, title = QuickActionTabMenu.CLOSE_ALL_TABS.value)

        val icon5 = requireContext().drawable(R.drawable.round_refresh_24)?.changeColor(requireContext(), R.color.purple_500)
        val action5 = Action(id = QuickActionTabMenu.REFRESH_WEBSITE.ordinal, icon = icon5!!, title = QuickActionTabMenu.REFRESH_WEBSITE.value)

        val icon5dot5 = requireContext().drawable(R.drawable.ic_round_more_horiz_24)?.changeColor(requireContext(), R.color.purple_500)
        val action5dot5 = Action(id = QuickActionTabMenu.MORE_OPTIONS.ordinal, icon = icon5dot5!!, title = QuickActionTabMenu.MORE_OPTIONS.value)

        val icon6 = requireContext().drawable(R.drawable.round_arrow_forward_24)?.changeColor(requireContext(), R.color.purple_500)
        val action6 = Action(id = QuickActionTabMenu.NAVIGATE_FORWARD.ordinal, icon = icon6!!, title = QuickActionTabMenu.NAVIGATE_FORWARD.value)

        val addFabQuickActionView = QuickActionView.make(requireContext()).apply {
            addAction(action1)
            addAction(action2)
            addAction(action2dot5)
            addAction(action3)
            addAction(action4)
            addAction(action5)
            addAction(action5dot5)
            addAction(action6)
            register(binding.btnWebsiteQuickActions)
            setBackgroundColor(requireContext().color(R.color.purple_50))
            setIndicatorDrawable(createGradientDrawable(width = deviceWidth() / 11, height = deviceWidth() / 11))
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
                QuickActionTabMenu.NAVIGATE_BACK.ordinal -> {
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                    if (selectedWebpage?.getWebView()?.canGoBack() == true) {
                        selectedWebpage.getWebView().goBack()
                    }
                }
                QuickActionTabMenu.NAVIGATE_FORWARD.ordinal -> {
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                    if (selectedWebpage?.getWebView()?.canGoForward() == true) {
                        selectedWebpage.getWebView().goForward()
                    } else {
                        closeTab()
                    }
                }
                QuickActionTabMenu.HOME.ordinal -> {
                    val selectedSearchEngine = preferences.getString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.GOOGLE.name)
                    val searchEngine = SearchEngine.valueOf(selectedSearchEngine ?: SearchEngine.GOOGLE.name)
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                    selectedWebpage?.loadUrl(url = "https://" + searchEngine.url.substringAfter("https://").substringBefore("/"))
                }
                QuickActionTabMenu.CLOSE_ALL_TABS.ordinal -> {
                    binding.showCloseAllTabsPopup()
                }
                QuickActionTabMenu.REFRESH_WEBSITE.ordinal -> {
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                    selectedWebpage?.getWebView()?.reload()
                }
                QuickActionTabMenu.GET_INSIGHT.ordinal -> {
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                    if (selectedWebpage?.getWebView()?.url.isNullOrBlank() || selectedWebpage?.getWebView()?.url?.isValidURL()?.not() == true) {
                        binding.root.showSnackBar("Not a valid website!")
                        return@setOnActionSelectedListener
                    }
                    val encryptedApiSecret = preferences.getString(Preferences.KEY_OPEN_AI_API_KEY, "")
                    if (encryptedApiSecret.isNullOrBlank().not()) {
                        GetInsightsBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_GET_INSIGHTS)
                    } else {
                        AddApiKeyBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_ADD_API_KEY)
                    }
                }
                QuickActionTabMenu.MORE_OPTIONS.ordinal -> {
                    val popupMenu = android.widget.PopupMenu(requireContext(), binding.btnWebsiteQuickActions)
                    QuickActionTabMenuMoreOptions.values().forEach {
                        popupMenu.menu.add(
                            0, 1, 1, menuIconWithText(
                                icon = requireContext().drawable(it.icon)?.changeColor(requireContext(), R.color.purple_500),
                                title = it.title
                            )
                        )
                    }
                    popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
                        view?.setHapticFeedback()
                        when (it?.title?.toString()?.trim()) {
                            QuickActionTabMenuMoreOptions.FIND_IN_PAGE.title -> {}
                            QuickActionTabMenuMoreOptions.ADD_SHORTCUT.title -> {
                                val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                                requireContext().addShortcut(webView = selectedWebpage?.getWebView(), favicon = selectedWebpage?.getFavicon())
                            }
                            QuickActionTabMenuMoreOptions.PRINT.title -> {}
                            QuickActionTabMenuMoreOptions.TRANSLATE.title -> {}
                            QuickActionTabMenuMoreOptions.DOWNLOAD.title -> {}
                            QuickActionTabMenuMoreOptions.ADD_TO_COLLECTIONS.title -> addToCollections()
                        }
                        false
                    }
                    popupMenu.show()
                }
            }
        }
    }

    private fun addToCollections() {
        lifecycleScope.launch {
            val collectionTitlesList = collectionsViewModel.getAllUniqueCollectionTitles().toArrayList().apply {
                add("Create new")
            }
            withContext(Main) {
                requireContext().showPopupMenu(
                    view = binding.btnWebsiteQuickActions,
                    title = "Add to Collections",
                    menuList = collectionTitlesList
                ) { menuPosition: Int ->
                    val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
                    val collectionWebPage = CollectionWebPage(
                        collectionTitle = collectionTitlesList[menuPosition] ?: "",
                        favicon = encodeBitmapToBase64String(bitmap = selectedWebpage?.getFavicon()),
                        title = selectedWebpage?.getWebView()?.title,
                        time = timeNow,
                        link = selectedWebpage?.getWebView()?.url ?: ""
                    )
                    if (collectionTitlesList[menuPosition]?.contains("Create new") == true) {
                        CreateCollectionBottomSheetFragment.newInstance(
                            collectionWebPage = collectionWebPage,
                            screen = this@SearchFragment::class.java.simpleName
                        ).show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_CREATE_COLLECTION)
                    } else {
                        collectionsViewModel.addToCollections(collectionWebPage)
                        requireContext().showToast("Added to ${collectionTitlesList[menuPosition]}")
                    }
                }
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

    private fun FragmentSearchBinding.showAddTabPopupMenu(
        view: View?,
        @MenuRes menuRes: Int
    ) {
        view ?: return
        val popupMenu = PopupMenu(requireContext(), view).apply {
            this.menu.invokeSetMenuIconMethod()
            menuInflater.inflate(menuRes, this.menu)
        }
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
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
        popupMenu.setOnDismissListener { it: PopupMenu? ->
        }
        popupMenu.menu.setMarginBtwMenuIconAndText(
            context = requireContext(),
            iconMarginDp = 10
        )
        popupMenu.menu.forEach { it: MenuItem ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.iconTintList = ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
            }
        }
        popupMenu.show()
    }

    fun getFaviconImageView(): ShapeableImageView = binding.ivWebappProfile

    fun getTabsTabLayout(): TabLayout = binding.tabLayoutTabs

    fun setWebsiteProfileLayoutVisibility(isVisible: Boolean) {
        binding.clWebsiteProfile.isVisible = isVisible
    }

    fun setWebViewData() {
        val selectedWebpage = activity?.supportFragmentManager?.findFragmentByTag(ConnectMeUtils.webpageFragmentIdList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)) as? SearchTabFragment
        val webViewData = WebViewData(
            url = selectedWebpage?.getWebView()?.url,
            title = selectedWebpage?.getWebView()?.title,
            favIcon = selectedWebpage?.getFavicon(),
            certificate = selectedWebpage?.getWebView()?.certificate
        )
        searchViewModel.setWebViewData(webViewData)
        val history = History(
            favicon = encodeBitmapToBase64String(selectedWebpage?.getFavicon()),
            title = selectedWebpage?.getWebView()?.title,
            time = timeNow,
            website = getHostFrom(url = selectedWebpage?.getWebView()?.url),
            link = selectedWebpage?.getWebView()?.url ?: ""
        )
        if (selectedWebpage?.getWebView()?.url.isNullOrBlank().not()) {
            historyViewModel.addToHistory(history)
        }
//        webSearchTabsList[binding.tabLayoutTabs.selectedTabPosition] = webSearchTabsList.getOrNull(binding.tabLayoutTabs.selectedTabPosition)?.copy(title = selectedWebpage?.getWebView()?.title)
    }

    // https://stackoverflow.com/questions/19765938/show-and-hide-a-view-with-a-slide-up-down-animation
    // https://developer.android.com/develop/ui/views/animations/reveal-or-hide-view
    fun showUrlSearchBar(isVisible: Boolean) {
//        val bottomTransition: Transition = Slide(Gravity.BOTTOM).apply {
//            duration = 300
//            addTarget(binding.clUrlSearchHeader)
//        }
//        TransitionManager.beginDelayedTransition(binding.clUrlSearchHeader.parent as ViewGroup, bottomTransition)


//        var cof = -1
//        var vis = if (isVisible) View.VISIBLE else View.GONE
//        var alpha = 0F
//        if (binding.clUrlSearchHeader.visibility == View.GONE) {
//            cof = 0
//            vis = View.VISIBLE
//            alpha = 1F
//        }
//        binding.clUrlSearchHeader.animate()
//            .translationY(binding.clUrlSearchHeader.height.toFloat() * cof)
//            .alpha(alpha)
//            .withStartAction {//in case showing the recyclerview show it at the beginning.
//                if (vis == View.VISIBLE)
//                    binding.clUrlSearchHeader.visibility = View.VISIBLE
//            }
//            .withEndAction {//in case hiding the recyclerview hide it at the end.
//                if (vis == View.GONE)
//                    binding.clUrlSearchHeader.visibility = View.GONE
//            }
//            .start()


//        if (isVisible) {
//            binding.clUrlSearchHeader.slideAnimation(SlideDirection.UP, SlideType.SHOW)//to make it reappear from bottom of the screen
//        } else {
//            binding.clUrlSearchHeader.slideAnimation(SlideDirection.DOWN, SlideType.HIDE)//to make it disappear through bottom of the screen
//        }

        binding.clUrlSearchHeader.isVisible = isVisible
    }

    private fun FragmentSearchBinding.updateTop4WebPageViewsOfCollections(top4CollectionItemsList: List<CollectionWebPage?>) {
        layoutCollections.layoutFollowingApp1.apply {
            ivAppIcon.load(decodeBase64StringToBitmap(top4CollectionItemsList.getOrNull(0)?.favicon))
            tvAppName.text = top4CollectionItemsList.getOrNull(0)?.title
        }
        layoutCollections.layoutFollowingApp2.apply {
            ivAppIcon.load(decodeBase64StringToBitmap(top4CollectionItemsList.getOrNull(1)?.favicon))
            tvAppName.text = top4CollectionItemsList.getOrNull(1)?.title
        }
        layoutCollections.layoutFollowingApp3.apply {
            ivAppIcon.load(decodeBase64StringToBitmap(top4CollectionItemsList.getOrNull(2)?.favicon))
            tvAppName.text = top4CollectionItemsList.getOrNull(2)?.title
        }
        layoutCollections.layoutFollowingApp4.apply {
            ivAppIcon.load(decodeBase64StringToBitmap(top4CollectionItemsList.getOrNull(3)?.favicon))
            tvAppName.text = top4CollectionItemsList.getOrNull(3)?.title
        }
    }

    inner class SearchViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = webSearchTabsList.size
        override fun createFragment(position: Int): Fragment {
            return SearchTabFragment.newInstance(tabUrl = websiteList.getOrNull(position)?.link)
        }
    }
}