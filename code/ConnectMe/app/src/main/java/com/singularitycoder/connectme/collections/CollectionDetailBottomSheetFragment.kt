package com.singularitycoder.connectme.collections

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentCollectionDetailBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.history.History
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_COLLECTION_TITLE = "ARG_PARAM_COLLECTION_TITLE"

@AndroidEntryPoint
class CollectionDetailBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(collectionTitle: String?) = CollectionDetailBottomSheetFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_COLLECTION_TITLE, collectionTitle) }
        }
    }

    private lateinit var binding: FragmentCollectionDetailBottomSheetBinding

    private var collectionTitle: String? = null
    private var webPageList = listOf<CollectionWebPage?>()

    private val collectionsViewModel by viewModels<CollectionsViewModel>()
    private val collectionDetailsAdapter: CollectionDetailsAdapter by lazy { CollectionDetailsAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectionTitle = arguments?.getString(ARG_PARAM_COLLECTION_TITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCollectionDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    private fun FragmentCollectionDetailBottomSheetBinding.setupUI() {
        enableSoftInput()

        setTransparentBackground()

        setBottomSheetBehaviour()

        tvHeader.text = collectionTitle

        rvCollections.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectionDetailsAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentCollectionDetailBottomSheetBinding.setupUserActionListeners() {
        collectionDetailsAdapter.setOnItemClickListener { it: CollectionWebPage? ->

        }

        collectionDetailsAdapter.setOnLongClickListener { collectionWebPage: CollectionWebPage?, view: View? ->
            val optionsList = listOf(
                Pair("Open in new tab", R.drawable.round_add_circle_outline_24),
                Pair("Open in new private tab", R.drawable.outline_policy_24),
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
                        requireContext().shareTextOrImage(text = collectionWebPage?.title, title = collectionWebPage?.link)
                    }
                    optionsList[3].first -> {
                        requireContext().clipboard()?.text = collectionWebPage?.link
                        requireContext().showToast("Copied link")
                    }
                    optionsList[4].first -> {
                        requireContext().showAlertDialog(
                            title = "Delete item",
                            message = collectionWebPage?.title ?: "",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveAction = {
                                collectionsViewModel.deleteItem(collectionWebPage)
                                if (webPageList.size == 1) {
                                    this@CollectionDetailBottomSheetFragment.dismiss()
                                }
                            }
                        )
                    }
                }
            }
        }

        btnOpenAll.onSafeClick {
            val optionsList = listOf(
                Pair("In new tab", R.drawable.round_add_circle_outline_24),
                Pair("In new private tab", R.drawable.outline_policy_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = it.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                    }
                    optionsList[1].first -> {
                    }
                }
            }
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                collectionDetailsAdapter.webPageList = webPageList
                collectionDetailsAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }

            collectionDetailsAdapter.webPageList = webPageList.filter {
                it?.link?.contains(other = query, ignoreCase = true) == true || it?.title?.contains(other = query, ignoreCase = true) == true
            }
            collectionDetailsAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentCollectionDetailBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(
            flow = collectionsViewModel.getCollectionsByCollectionTitles(collectionTitle)
        ) { it: List<CollectionWebPage?> ->
            this@CollectionDetailBottomSheetFragment.webPageList = it
            collectionDetailsAdapter.webPageList = it
            collectionDetailsAdapter.notifyDataSetChanged()
        }
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
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
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