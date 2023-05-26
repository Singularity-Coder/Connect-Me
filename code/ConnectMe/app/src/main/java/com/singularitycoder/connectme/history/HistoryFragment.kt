package com.singularitycoder.connectme.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentHistoryBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

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
            if (scrollY - oldScrollY > 20) {
                etSearch.hideKeyboard()
            }

//            if (historyAdapter.historyList.isNotEmpty()) {
//                tvDate.isVisible = historyAdapter.historyList.isNotEmpty()
//                tvDate.text = historyAdapter.historyList.get(linearLayoutManager.findLastVisibleItemPosition())?.time?.toShortDate()
//            }
        })

        btnDeleteAllHistory.onSafeClick {
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

        historyAdapter.setOnClickListener { it: History ->

        }

        historyAdapter.setOnLongClickListener { history: History, view: View? ->
            val popupMenu = PopupMenu(requireContext(), view)
            val historyOptionsList = listOf("Share", "Delete")
            historyOptionsList.forEach {
                popupMenu.menu.add(
                    0, 1, 1, menuIconWithText(
                        icon = requireContext().drawable(
                            if (it == "Share") {
                                R.drawable.outline_share_24
                            } else {
                                R.drawable.outline_delete_24
                            }
                        )?.changeColor(requireContext(), R.color.purple_500),
                        title = it
                    )
                )
            }
            popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
                view?.setHapticFeedback()
                when (it?.title?.toString()?.trim()) {
                    historyOptionsList[0] -> {
                        requireContext().shareTextOrImage(text = history.title, title = history.link)
                    }
                    historyOptionsList[1] -> {
                        historyViewModel.deleteItem(history)
                    }
                }
                false
            }
            popupMenu.show()
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                prepareHistoryList(historyList)
                return@doAfterTextChanged
            }

            historyAdapter.historyList = historyList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            historyAdapter.notifyDataSetChanged()
        }
    }

    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = historyViewModel.getAllHistory()) { historyList: List<History?> ->
            this.historyList = historyList
            prepareHistoryList(historyList)
        }
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
            }?.reversed() ?: emptyList()
            sortedHistoryList.addAll(preparedList)
        }
        historyAdapter.historyList = sortedHistoryList.reversed()
        historyAdapter.notifyDataSetChanged()
    }
}