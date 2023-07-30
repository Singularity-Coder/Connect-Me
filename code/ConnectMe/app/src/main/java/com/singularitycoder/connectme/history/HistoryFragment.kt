package com.singularitycoder.connectme.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentHistoryBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.constants.FragmentsTag
import com.singularitycoder.connectme.helpers.constants.NewTabType
import com.singularitycoder.connectme.search.model.SearchTab
import com.singularitycoder.connectme.search.view.SearchFragment
import com.singularitycoder.connectme.search.view.peek.PeekBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = HistoryFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private val historyAdapter = HistoryAdapter()
    private val historyViewModel by viewModels<HistoryViewModel>()

    private var historyList = listOf<History?>()
    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentHistoryBinding.setupUI() {
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.outline_delete_24)
        linearLayoutManager = LinearLayoutManager(
            /* context = */ context,
            /* orientation = */ RecyclerView.VERTICAL,
            /* reverseLayout = */ true
        )
        rvHistory.apply {
            layoutManager = linearLayoutManager
            adapter = historyAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentHistoryBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            println("scrollY: $scrollY oldScrollY: $oldScrollY".trimIndent())
//            if (scrollY - oldScrollY > 20) {
//                etSearch.hideKeyboard()
//            }

//            if (historyAdapter.historyList.isNotEmpty()) {
//                tvDate.isVisible = historyAdapter.historyList.isNotEmpty()
//                tvDate.text = historyAdapter.historyList.get(linearLayoutManager.findLastVisibleItemPosition())?.time?.toShortDate()
//            }
        })

        layoutSearch.btnMore.onSafeClick {
            val menuOptionsList = listOf(
                "Last hour",
                "Last 24 hours",
                "Last 7 days",
                "Last 4 weeks",
                "Last year",
                "All time"
            )
            requireContext().showPopupMenu(
                view = layoutSearch.btnMore,
                title = "Delete History",
                menuList = menuOptionsList
            ) { menuPosition: Int ->
                when (menuOptionsList[menuPosition]) {
                    menuOptionsList[0] -> {
                        val time1Hour = TimeUnit.HOURS.toMillis(1)
                        historyViewModel.deleteAllHistoryByTIme(elapsedTime = timeNow - time1Hour)
                    }
                    menuOptionsList[1] -> {
                        val time24Hours = TimeUnit.HOURS.toMillis(24)
                        historyViewModel.deleteAllHistoryByTIme(elapsedTime = timeNow - time24Hours)
                    }
                    menuOptionsList[2] -> {
                        val time7Days = TimeUnit.DAYS.toMillis(7)
                        historyViewModel.deleteAllHistoryByTIme(elapsedTime = timeNow - time7Days)
                    }
                    menuOptionsList[3] -> {
                        val time4Weeks = TimeUnit.DAYS.toMillis(28)
                        historyViewModel.deleteAllHistoryByTIme(elapsedTime = timeNow - time4Weeks)
                    }
                    menuOptionsList[4] -> {
                        val time1Year = TimeUnit.DAYS.toMillis(28 * 12)
                        historyViewModel.deleteAllHistoryByTIme(elapsedTime = timeNow - time1Year)
                    }
                    menuOptionsList[5] -> {
                        requireContext().showAlertDialog(
                            title = "Delete all history",
                            message = "Careful! You cannot undo this.",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveAction = {
                                historyViewModel.deleteAllHistory()
                            }
                        )
                    }
                }
            }
        }

        historyAdapter.setOnClickListener { it: History? ->
            PeekBottomSheetFragment.newInstance(
                peekUrl = it?.link
            ).show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_PEEK)
        }

        historyAdapter.setOnLongClickListener { history: History?, view: View? ->
            val optionsList = listOf(
                Pair("Open in new tab", R.drawable.round_add_circle_outline_24),
                Pair("Open in new private tab", R.drawable.outline_policy_24),
                Pair("Share", R.drawable.outline_share_24),
                Pair("Copy link", R.drawable.baseline_content_copy_24),
                Pair("Delete", R.drawable.outline_delete_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        openSearchScreen(isPrivate = false, history = history)
                    }
                    optionsList[1].first -> {
                        openSearchScreen(isPrivate = true, history = history)
                    }
                    optionsList[2].first -> {
                        requireContext().shareTextOrImage(text = history?.title, title = history?.link)
                    }
                    optionsList[3].first -> {
                        requireContext().clipboard()?.text = history?.link
                        requireContext().showToast("Copied link")
                    }
                    optionsList[4].first -> {
                        historyViewModel.deleteItem(history)
                    }
                }
            }
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                prepareHistoryList(historyList)
                return@doAfterTextChanged
            }

            historyAdapter.historyList = historyList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            historyAdapter.notifyDataSetChanged()
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }
    }

    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = historyViewModel.getAllHistoryFlow()) { historyList: List<History?> ->
            this.historyList = historyList
            prepareHistoryList(historyList)
        }
    }

    private fun openSearchScreen(
        isPrivate: Boolean,
        history: History?
    ) {
        (requireActivity() as? MainActivity)?.showScreen(
            fragment = SearchFragment.newInstance(websiteList = listOf(history).mapIndexed { index, history ->
                SearchTab(
                    type = if (isPrivate) NewTabType.NEW_PRIVATE_TAB else NewTabType.NEW_TAB,
                    link = history?.link,
                )
            }.toArrayList()),
            tag = FragmentsTag.SEARCH,
            isAdd = true
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun prepareHistoryList(historyList: List<History?>) {
        val sortedHistoryList = ArrayList<History?>()
        val historyMap = HashMap<String?, ArrayList<History?>>()
        historyList.sortedBy { it?.time }.forEach { it: History? ->
            val historyArrayList = historyMap.get(it?.time?.toShortDate()) ?: ArrayList()
            historyArrayList.add(it)
            historyMap.put(it?.time?.toShortDate(), historyArrayList)
        }
        historyMap.keys.forEach { date: String? ->
            val preparedList = historyMap.get(date)?.mapIndexed { index, history ->
                history.apply {
                    if (index == historyMap.get(date)?.lastIndex) this?.isDateShown = true
                }
            } ?: emptyList()
            sortedHistoryList.addAll(preparedList.reversed())
        }
        historyAdapter.historyList = sortedHistoryList.reversed()
        historyAdapter.notifyDataSetChanged()
    }
}