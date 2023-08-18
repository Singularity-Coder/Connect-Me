package com.singularitycoder.connectme.search.view.speedDial

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.collections.CollectionWebPage
import com.singularitycoder.connectme.collections.CollectionsViewModel
import com.singularitycoder.connectme.databinding.FragmentSpeedDialBottomSheetBinding
import com.singularitycoder.connectme.downloads.Download
import com.singularitycoder.connectme.downloads.DownloadsViewModel
import com.singularitycoder.connectme.followingWebsite.FollowingWebsite
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteViewModel
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import com.singularitycoder.connectme.history.History
import com.singularitycoder.connectme.history.HistoryViewModel
import com.singularitycoder.connectme.search.view.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

const val ARG_PARAM_SPEED_DIAL_TITLE = "ARG_PARAM_SPEED_DIAL_TITLE"
const val ARG_PARAM_SPEED_DIAL_SUBTITLE = "ARG_PARAM_SPEED_DIAL_SUBTITLE"
const val ARG_PARAM_SPEED_DIAL_HOST = "ARG_PARAM_SPEED_DIAL_HOST"

@AndroidEntryPoint
class SpeedDialBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            title: String?,
            subtitle: String? = null,
            host: String? = null
        ) = SpeedDialBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM_SPEED_DIAL_TITLE, title)
                putString(ARG_PARAM_SPEED_DIAL_SUBTITLE, subtitle)
                putString(ARG_PARAM_SPEED_DIAL_HOST, host)
            }
        }
    }

    private lateinit var binding: FragmentSpeedDialBottomSheetBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var speedDialTitle: String? = null
    private var speedDialSubtitle: String? = null
    private var speedDialHost: String? = null
    private var speedDialList = listOf<SpeedDial?>()

    private val collectionsViewModel by viewModels<CollectionsViewModel>()
    private val historyViewModel by viewModels<HistoryViewModel>()
    private val followingWebsiteViewModel by viewModels<FollowingWebsiteViewModel>()
    private val downloadsViewModel by viewModels<DownloadsViewModel>()
    private val speedDialAdapter: SpeedDialAdapter by lazy { SpeedDialAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speedDialTitle = arguments?.getString(ARG_PARAM_SPEED_DIAL_TITLE)
        speedDialSubtitle = arguments?.getString(ARG_PARAM_SPEED_DIAL_SUBTITLE)
        speedDialHost = arguments?.getString(ARG_PARAM_SPEED_DIAL_HOST)
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
            val searchFragment = parentFragmentManager.fragments.firstOrNull {
                it.javaClass.simpleName == SearchFragment.newInstance("").javaClass.simpleName
            } as? SearchFragment
            searchFragment?.loadWebPage(
                link = it?.link,
                favicon = it?.favicon
            )
            dismiss()
        }

        if (speedDialTitle == SpeedDialFeatures.DOWNLOADS.value) {
            speedDialAdapter.setOnItemLongClickListener { it: SpeedDial? ->
                requireActivity().openFile(selectedItem = File(it?.path ?: ""))
            }
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
                this@SpeedDialBottomSheetFragment.speedDialList = if (speedDialHost != null) {
                    mappedSpeedDialList.filter { getHostFrom(url = it.link).contains(other = speedDialHost!!, ignoreCase = true) }
                } else {
                    mappedSpeedDialList
                }
                prepareHistoryList(this@SpeedDialBottomSheetFragment.speedDialList)
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

        if (speedDialTitle == SpeedDialFeatures.DOWNLOADS.value) {
            (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = downloadsViewModel.getAllDownloadsFlow()) { downloadsList: List<Download?> ->
                fun getBitmap(@DrawableRes drawableRes: Int): Bitmap? {
                    return requireContext().drawable(drawableRes)
                        ?.changeColor(requireContext(), R.color.purple_500)
                        ?.toBitmapOrNull(width = 32.dpToPx().toInt(), height = 32.dpToPx().toInt())
                }
                speedDialList = downloadsList.map { download: Download? ->
                    val fileExtension = download?.extension?.toLowCase()?.trim()
                    val bitmap = when (fileExtension) {
                        in ImageFormat.values().map { it.value.toLowCase().trim() } -> {
                            getBitmap(drawableRes = R.drawable.outline_image_24)
                        }
                        in VideoFormat.values().map { it.value.toLowCase().trim() } -> {
                            getBitmap(drawableRes = R.drawable.outline_movie_24)
                        }
                        in AudioFormat.values().map { it.value.toLowCase().trim() } -> {
                            getBitmap(drawableRes = R.drawable.outline_audiotrack_24)
                        }
                        in DocumentFormat.values().map { it.value.toLowCase().trim() } -> {
                            getBitmap(drawableRes = R.drawable.outline_article_24)
                        }
                        in ArchiveFormat.values().map { it.value.toLowCase().trim() } -> {
                            getBitmap(drawableRes = R.drawable.outline_folder_zip_24)
                        }
                        in AndroidFormat.values().map { it.value.toLowCase().trim() } -> {
                            getBitmap(drawableRes = R.drawable.outline_android_24)
                        }
                        else -> {
                            getBitmap(drawableRes = R.drawable.outline_insert_drive_file_24)
                        }
                    }
                    SpeedDial(
                        type = speedDialTitle,
                        bitmap = bitmap,
                        title = download?.title,
                        time = download?.time,
                        link = download?.link ?: "",
                        path = download?.path ?: ""
                    )
                }
                speedDialAdapter.speedDialList = speedDialList
                speedDialAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openIfFileElseShowFilesListIfDirectory(currentDirectory: File) {
        if (currentDirectory.isFile) return requireActivity().openFile(currentDirectory)

        speedDialList = getFilesListFrom(getDownloadDirectory()).map { file: File ->
            if (file.isDirectory.not()) {
                SpeedDial(
                    imageUrl = file.absolutePath,
                    title = file.name,
                    timeInString = "${file.getAppropriateSize()}  •  ${file.lastModified().toIntuitiveDateTime()}",
                )
            } else null
        }
        speedDialAdapter.speedDialList = speedDialList.mapNotNull { it }
        speedDialAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun prepareHistoryList(historyList: List<SpeedDial?>) {
        val sortedHistoryList = ArrayList<SpeedDial?>()
        val historyMap = HashMap<Long?, ArrayList<SpeedDial?>>()
        historyList.sortedBy { it?.time }.forEach { it: SpeedDial? ->
            val key = convertDateToLong(date = it?.time?.toTimeOfType(type = DateType.dd_MMM_yyyy) ?: "", dateType = DateType.dd_MMM_yyyy.value)
            val historyArrayList = historyMap.get(key) ?: ArrayList()
            historyArrayList.add(it)
            historyMap.put(key, historyArrayList)
        }
        historyMap.keys.sortedBy { it }.forEach { date: Long? ->
            val preparedList = historyMap.get(date)?.mapIndexed { index, history ->
                history.apply {
                    if (index == historyMap.get(date)?.lastIndex) this?.isDateShown = true
                }
            } ?: emptyList()
            sortedHistoryList.addAll(preparedList)
        }
        speedDialAdapter.speedDialList = sortedHistoryList
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