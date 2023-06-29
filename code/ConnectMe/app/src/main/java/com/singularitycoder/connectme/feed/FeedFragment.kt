package com.singularitycoder.connectme.feed

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
import com.singularitycoder.connectme.helpers.constants.WorkerTag
import com.singularitycoder.connectme.search.viewmodel.WebsiteActionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
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

    private val feedAdapter = FeedAdapter()
    private val rssFeedTypeList = listOf("All Feeds", "Saved Feed")

    private var feedList = listOf<Feed?>()
    private var feedSavedList = listOf<Feed?>()
    private var topicParam: String? = null
    private var selectedFeed: String? = rssFeedTypeList.first()

    private val websiteActionsViewModel by activityViewModels<WebsiteActionsViewModel>()

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
        if (feedList.isEmpty()) parseRssFeedFromWorker()
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFeedBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        feedAdapter.setOnItemClickListener { it: Feed? ->
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
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {}
                    optionsList[1].first -> {}
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

        layoutSearch.btnMore.onSafeClick {
            val popupMenu = PopupMenu(requireContext(), it.first)
            rssFeedTypeList.forEach {
                popupMenu.menu.add(
                    0, 1, 1, menuIconWithText(
                        icon = requireContext().drawable(R.drawable.round_check_24)?.changeColor(
                            context = requireContext(),
                            color = if (selectedFeed == it) R.color.purple_500 else android.R.color.transparent
                        ),
                        title = it
                    )
                )
            }
            popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
                view?.setHapticFeedback()
                when (it?.title?.toString()?.trim()) {
                    rssFeedTypeList[0] -> {
                        selectedFeed = rssFeedTypeList[0]
                        feedAdapter.feedList = feedList
                        feedAdapter.notifyDataSetChanged()
                    }
                    rssFeedTypeList[1] -> {
                        selectedFeed = rssFeedTypeList[1]
                        feedAdapter.feedList = feedSavedList
                        feedAdapter.notifyDataSetChanged()
                    }
                }
                false
            }
            popupMenu.show()
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

    private fun parseRssFeedFromWorker() {
        val workConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<RssFeedWorker>().setConstraints(workConstraints).build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(WorkerTag.RSS_FEED_PARSER, ExistingWorkPolicy.KEEP, workRequest)
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(workRequest.id).observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> println("RUNNING: show Progress")
                WorkInfo.State.ENQUEUED -> println("ENQUEUED: show Progress")
                WorkInfo.State.SUCCEEDED -> {
                    println("SUCCEEDED: stop Progress")
                    // TODO stop progress
                }
                WorkInfo.State.FAILED -> println("FAILED: stop showing Progress")
                WorkInfo.State.BLOCKED -> println("BLOCKED: show Progress")
                WorkInfo.State.CANCELLED -> println("CANCELLED: stop showing Progress")
                else -> Unit
            }
        }
    }
}