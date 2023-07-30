package com.singularitycoder.connectme.search.view.speedDial

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.collections.CollectionWebPage
import com.singularitycoder.connectme.collections.CollectionsViewModel
import com.singularitycoder.connectme.databinding.FragmentSpeedDialBottomSheetBinding
import com.singularitycoder.connectme.followingWebsite.FollowingWebsite
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteViewModel
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.SpeedDialFeatures
import com.singularitycoder.connectme.history.History
import com.singularitycoder.connectme.history.HistoryViewModel
import com.singularitycoder.connectme.search.view.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

const val ARG_PARAM_SPEED_DIAL_TITLE = "ARG_PARAM_SPEED_DIAL_TITLE"
const val ARG_PARAM_SPEED_DIAL_SUBTITLE = "ARG_PARAM_SPEED_DIAL_SUBTITLE"

@AndroidEntryPoint
class SpeedDialBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(title: String?, subtitle: String? = null) = SpeedDialBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM_SPEED_DIAL_TITLE, title)
                putString(ARG_PARAM_SPEED_DIAL_SUBTITLE, subtitle)
            }
        }
    }

    private lateinit var binding: FragmentSpeedDialBottomSheetBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var speedDialTitle: String? = null
    private var speedDialSubtitle: String? = null
    private var speedDialList = listOf<SpeedDial?>()

    private val collectionsViewModel by viewModels<CollectionsViewModel>()
    private val historyViewModel by viewModels<HistoryViewModel>()
    private val followingWebsiteViewModel by viewModels<FollowingWebsiteViewModel>()
    private val speedDialAdapter: SpeedDialAdapter by lazy { SpeedDialAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speedDialTitle = arguments?.getString(ARG_PARAM_SPEED_DIAL_TITLE)
        speedDialSubtitle = arguments?.getString(ARG_PARAM_SPEED_DIAL_SUBTITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSpeedDialBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    private fun FragmentSpeedDialBottomSheetBinding.setupUI() {
        enableSoftInput()

        setTransparentBackground()

        setBottomSheetBehaviour()

        tvHeader.text = speedDialSubtitle ?: speedDialTitle

        linearLayoutManager = if (speedDialTitle == SpeedDialFeatures.HISTORY.value) {
            LinearLayoutManager(
                /* context = */ context,
                /* orientation = */ RecyclerView.VERTICAL,
                /* reverseLayout = */ true
            )
        } else {
            LinearLayoutManager(context)
        }
        rvSpeedDial.apply {
            layoutManager = linearLayoutManager
            adapter = speedDialAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentSpeedDialBottomSheetBinding.setupUserActionListeners() {
        speedDialAdapter.setOnItemClickListener { it: SpeedDial? ->
            val searchFragment = requireActivity().supportFragmentManager.fragments.firstOrNull {
                it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
            } as? SearchFragment
            searchFragment?.loadWebPage(
                link = it?.link,
                favicon = it?.favicon
            )
            dismiss()
        }

        ivSearch.onSafeClick {
            etSearch.setText("")
            clSearch.isVisible = clSearch.isVisible.not()
            if (clSearch.isVisible) {
                etSearch.showKeyboard()
            } else {
                etSearch.hideKeyboard()
            }
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                if (speedDialTitle == SpeedDialFeatures.HISTORY.value) {
                    prepareHistoryList(speedDialList)
                } else {
                    speedDialAdapter.speedDialList = speedDialList
                    speedDialAdapter.notifyDataSetChanged()
                }
                return@doAfterTextChanged
            }

            speedDialAdapter.speedDialList = speedDialList.filter {
                it?.link?.contains(other = query, ignoreCase = true) == true || it?.title?.contains(other = query, ignoreCase = true) == true
            }
            speedDialAdapter.notifyDataSetChanged()
        }

//        if (speedDialTitle == SpeedDialFeatures.HISTORY.value) {
//            rvSpeedDial.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    if (speedDialAdapter.speedDialList.isNotEmpty()) {
//                        tvDate.isVisible = speedDialAdapter.speedDialList.isNotEmpty()
//                        tvDate.text = speedDialAdapter.speedDialList.get(linearLayoutManager.findLastVisibleItemPosition())?.time.toShortDate()
//                    }
//                }
//            })
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentSpeedDialBottomSheetBinding.observeForData() {
        if (speedDialTitle == SpeedDialFeatures.COLLECTIONS.value) {
            (activity as? MainActivity)?.collectLatestLifecycleFlow(
                flow = collectionsViewModel.getCollectionsByCollectionTitle(speedDialSubtitle)
            ) { it: List<CollectionWebPage?> ->
                speedDialList = it.map { collectionWebPage: CollectionWebPage? ->
                    SpeedDial(
                        favicon = collectionWebPage?.favicon,
                        title = collectionWebPage?.title,
                        time = collectionWebPage?.time,
                        link = collectionWebPage?.link ?: "",
                    )
                }
                speedDialAdapter.speedDialList = speedDialList
                speedDialAdapter.notifyDataSetChanged()
            }
        }

        if (speedDialTitle == SpeedDialFeatures.HISTORY.value) {
            (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = historyViewModel.getAllHistoryFlow()) { historyList: List<History?> ->
                val mappedSpeedDialList = historyList.map { history: History? ->
                    SpeedDial(
                        favicon = history?.favicon,
                        title = history?.title,
                        time = history?.time,
                        link = history?.link ?: "",
                        isDateShown = history?.isDateShown ?: false
                    )
                }
                this@SpeedDialBottomSheetFragment.speedDialList = mappedSpeedDialList
                prepareHistoryList(mappedSpeedDialList)
            }
        }

        if (speedDialTitle == SpeedDialFeatures.FOLLOWING_WEBSITES.value) {
            (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = followingWebsiteViewModel.getAllFollowingWebsites()) { followingList: List<FollowingWebsite?> ->
                speedDialList = followingList.map { followingWebsite: FollowingWebsite? ->
                    SpeedDial(
                        favicon = followingWebsite?.favicon,
                        title = followingWebsite?.title,
                        time = followingWebsite?.time,
                        link = followingWebsite?.link ?: "",
                    )
                }
                speedDialAdapter.speedDialList = speedDialList
                speedDialAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun prepareHistoryList(historyList: List<SpeedDial?>) {
        val sortedHistoryList = ArrayList<SpeedDial?>()
        val historyMap = HashMap<String?, ArrayList<SpeedDial?>>()
        historyList.sortedBy { it?.time }.forEach { it: SpeedDial? ->
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
        speedDialAdapter.speedDialList = sortedHistoryList.reversed()
        speedDialAdapter.notifyDataSetChanged()
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheet.layoutParams.height = deviceHeight()
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        var oldState = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        oldState = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                    BottomSheetBehavior.STATE_SETTLING -> {
                        if (oldState == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}