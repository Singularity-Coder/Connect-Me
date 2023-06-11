package com.singularitycoder.connectme.collections

import android.annotation.SuppressLint
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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentCollectionsBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.search.view.createCollection.CreateCollectionBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class CollectionsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = CollectionsFragment()
    }

    private lateinit var binding: FragmentCollectionsBinding

    private var collectionsList = listOf<LinksCollection?>()

    private val collectionsAdapter = CollectionsAdapter()
    private val collectionsViewModel by viewModels<CollectionsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentCollectionsBinding.setupUI() {
        rvCollections.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectionsAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentCollectionsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        collectionsAdapter.setOnItemClickListener { it: LinksCollection? ->
            CollectionDetailBottomSheetFragment.newInstance(
                collectionTitle = it?.title
            ).show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_COLLECTION_DETAIL)
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                setSearchList(collectionsList)
                return@doAfterTextChanged
            }

            collectionsAdapter.collectionsList = collectionsList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            collectionsAdapter.notifyDataSetChanged()
        }

        etSearch.onImeClick {
            etSearch.hideKeyboard()
        }

        btnMore.onSafeClick {
            val popupMenu = PopupMenu(requireContext(), it.first)
            val optionsList = listOf(
                Pair("Add", R.drawable.ic_round_add_24),
                Pair("Import", R.drawable.round_south_west_24),
                Pair("Export", R.drawable.round_north_east_24),
            )
            optionsList.forEach { it: Pair<String, Int> ->
                when (it.first) {
                    "Add" -> {
                        popupMenu.menu.add(
                            0, 1, 1, menuIconWithText(
                                icon = requireContext().drawable(it.second)?.changeColor(requireContext(), R.color.purple_500),
                                title = it.first
                            )
                        )
                    }
                    "Import" -> {
                        popupMenu.menu.addSubMenu(
                            0, 1, 1, menuIconWithText(
                                icon = requireContext().drawable(it.second)?.changeColor(requireContext(), R.color.purple_500),
                                title = it.first
                            )
                        ).apply {
                            add("csv")
                            add("db")
                        }
                    }
                    "Export" -> {
                        popupMenu.menu.addSubMenu(
                            0, 1, 1, menuIconWithText(
                                icon = requireContext().drawable(it.second)?.changeColor(requireContext(), R.color.purple_500),
                                title = it.first
                            )
                        ).apply {
                            /** Notice the space next to the words. This is different from Import submenus */
                            add("csv ")
                            add("db ")
                        }
                    }
                    else -> Unit
                }
            }
            popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
                view?.setHapticFeedback()
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        CreateCollectionBottomSheetFragment.newInstance(
                            screen = this@CollectionsFragment::class.java.simpleName
                        ).show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_CREATE_COLLECTION)
                    }
                }
                when (it?.title) {
                    "csv" /* Import CSV */ -> {
                        requireContext().showToast(optionsList[1].first + " csv")
                    }
                    "db" /* Import DB */ -> {
                        requireContext().showToast(optionsList[1].first + " db")
                    }
                    "csv " /* Export CSV */ -> {
                        requireContext().showToast(optionsList[2].first + " csv")
                    }
                    "db " /* Export DB */ -> {
                        requireContext().showToast(optionsList[2].first + " db")
                    }
                }
                false
            }
            popupMenu.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = collectionsViewModel.getAllCollections()) { it: List<CollectionWebPage?> ->
            val collectionsMap = HashMap<String?, ArrayList<CollectionWebPage?>>()
            val linksCollectionsList = mutableListOf<LinksCollection?>()
            it.forEach {
                val collectionWebPageList = (collectionsMap.get(it?.collectionTitle) ?: ArrayList()).apply { add(it) }
                collectionsMap.put(it?.collectionTitle, collectionWebPageList)
            }
            collectionsMap.keys.forEach { it: String? ->
                val linksCollection = LinksCollection(
                    title = it,
                    count = collectionsMap.get(it)?.size ?: 0,
                    linkList = collectionsMap.get(it) ?: emptyList()
                )
                linksCollectionsList.add(linksCollection)
            }
            this.collectionsList = linksCollectionsList.toList()
            setSearchList(linksCollectionsList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSearchList(collectionsList: List<LinksCollection?>) {
        collectionsAdapter.collectionsList = collectionsList
        collectionsAdapter.notifyDataSetChanged()
    }
}