package com.singularitycoder.connectme.feed

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentFeedBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.constants.FragmentsTag
import com.singularitycoder.connectme.helpers.constants.NewTabType
import com.singularitycoder.connectme.helpers.constants.WorkerTag
import com.singularitycoder.connectme.search.model.SearchTab
import com.singularitycoder.connectme.search.view.SearchFragment
import com.singularitycoder.connectme.search.view.peek.PeekBottomSheetFragment
import com.singularitycoder.connectme.search.viewmodel.WebsiteActionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = FeedFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentFeedBinding


    private var feedList = listOf<Feed?>()
    private var feedSavedList = listOf<Feed?>()
    private var topicParam: String? = null

    private val feedAdapter = FeedAdapter()

    private val websiteActionsViewModel by activityViewModels<WebsiteActionsViewModel>()

    private val rssFeedTypeList = listOf(
        Pair("All Feeds", R.drawable.round_check_24),
        Pair("Saved Feed", R.drawable.round_check_24)
    )
    private var selectedFeed: String? = rssFeedTypeList.first().first

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    override fun onResume() {
        super.onResume()
        if (feedList.isEmpty()) {
            binding.layoutShimmerLoader.root.isVisible = true
        }
//        CodeExecutor.executeCode(
//            javaClassName = "EelloWoruludu",
//            commandsList = listOf("System.out.println(\"Eeeelloololo Waraladuuuuuuuu\")")
//        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFeedBinding.setupUI() {
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.round_tune_24)
        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }
        parseRssFeedEveryHourFromWorker()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFeedBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        feedAdapter.setOnItemClickListener { it: Feed? ->
            PeekBottomSheetFragment.newInstance(
                peekUrl = it?.link
            ).show(parentFragmentManager, BottomSheetTag.TAG_PEEK)
        }

        feedAdapter.setOnItemLongClickListener { feed, view ->
            val optionsList = listOf(
                Pair("Open in new tab", R.drawable.round_add_circle_outline_24),
                Pair("Open in new private tab", R.drawable.outline_policy_24),
                Pair("Save", R.drawable.favorite_border_black_24dp),
                Pair("Share", R.drawable.outline_share_24),
                Pair("Copy link", R.drawable.baseline_content_copy_24),
                Pair("Delete", R.drawable.outline_delete_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItemText = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        openSearchScreen(isPrivate = false, feed = feed)
                    }
                    optionsList[1].first -> {
                        openSearchScreen(isPrivate = true, feed = feed)
                    }
                    optionsList[2].first -> {
                        websiteActionsViewModel.updatedFeedItemToSaved(feed?.copy(isSaved = true))
                    }
                    optionsList[3].first -> {
                        requireContext().shareTextOrImage(text = feed?.title, title = feed?.link)
                    }
                    optionsList[4].first -> {
                        requireContext().clipboard()?.text = feed?.link
                        requireContext().showToast("Copied link")
                    }
                    optionsList[5].first -> {
                        requireContext().showAlertDialog(
                            title = "Delete item",
                            message = feed?.title ?: "",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveAction = {
                                websiteActionsViewModel.deleteItem(feed)
                            }
                        )
                    }
                }
            }
        }

        layoutSearch.btnMore.onSafeClick { pair: Pair<View?, Boolean> ->
            requireContext().showSingleSelectionPopupMenu(
                view = pair.first,
                title = "Filter",
                selectedOption = selectedFeed,
                menuList = rssFeedTypeList,
            ) { menuItem: MenuItem? ->
                selectedFeed = menuItem?.title?.toString()?.trim() ?: ""
                when (menuItem?.title?.toString()?.trim()) {
                    rssFeedTypeList[0].first -> {
                        feedAdapter.feedList = feedList
                    }
                    rssFeedTypeList[1].first -> {
                        feedAdapter.feedList = feedSavedList
                    }
                }
                feedAdapter.notifyDataSetChanged()
            }
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                feedAdapter.feedList = feedList
                feedAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }

            feedAdapter.feedList = feedList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            feedAdapter.notifyDataSetChanged()
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }

        feedAdapter.getItemCountListener { count: Int ->
            if (count > 0) {
                binding.layoutShimmerLoader.root.isVisible = false
                parseRssFeedEveryHourFromWorker()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = websiteActionsViewModel.getAllItemsStateFlow()) { it: List<Feed?> ->
            this.feedList = it
            feedAdapter.feedList = it
            feedAdapter.notifyDataSetChanged()
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = websiteActionsViewModel.getAllSavedItemsStateFlow()) { it: List<Feed?> ->
            this.feedSavedList = it
        }
    }

    private fun openSearchScreen(
        isPrivate: Boolean,
        feed: Feed?
    ) {
        (requireActivity() as? MainActivity)?.showScreen(
            fragment = SearchFragment.newInstance(websiteList = listOf(feed).mapIndexed { index, feed ->
                SearchTab(
                    type = if (isPrivate) NewTabType.NEW_PRIVATE_TAB else NewTabType.NEW_TAB,
                    link = feed?.link,
                )
            }.toArrayList()),
            tag = FragmentsTag.SEARCH,
            isAdd = true
        )
    }

    private fun parseRssFeedEveryHourFromWorker() {
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = PeriodicWorkRequestBuilder<RssFeedWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setConstraints(workConstraints).build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            /* uniqueWorkName = */ WorkerTag.PERIODIC_RSS_FEED_PARSER,
            /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.KEEP,
            /* periodicWork = */ workRequest
        )
    }
}