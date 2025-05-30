package com.singularitycoder.connectme.collections

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.ThisApp
import com.singularitycoder.connectme.databinding.FragmentCollectionsBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.constants.CollectionScreenEvent
import com.singularitycoder.connectme.helpers.constants.DEFAULT_WEB_APPS
import com.singularitycoder.connectme.helpers.constants.globalLayoutAnimation
import com.singularitycoder.connectme.search.view.peek.PeekBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        lifecycleScope.launch {
            /** To load the default webapps when db starts I am forcing a dummy insert and adding some delay for all to load */
            if (collectionsViewModel.getCollectionsCount() == 0) {
                collectionsViewModel.addToCollections(DEFAULT_WEB_APPS.first())
                delay(1.seconds())
            }
            withContext(Main) {
                observeForData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity?.application as? ThisApp)?.isCollectionsScreenLoaded = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentCollectionsBinding.setupUI() {
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.ic_round_more_horiz_24)
        rvCollections.apply {
            layoutAnimation = rvCollections.context.layoutAnimationController(globalLayoutAnimation)
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
            ).show(parentFragmentManager, BottomSheetTag.TAG_COLLECTION_DETAIL)
        }

        collectionsAdapter.setOnWebAppClickListener { it: CollectionWebPage? ->
            PeekBottomSheetFragment.newInstance(
                peekUrl = it?.link
            ).show(parentFragmentManager, BottomSheetTag.TAG_PEEK)
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                setSearchList(collectionsList)
                return@doAfterTextChanged
            }

            collectionsAdapter.collectionsList = collectionsList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            collectionsAdapter.notifyDataSetChanged()
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }

        layoutSearch.btnMore.onSafeClick { btn: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Add", R.drawable.ic_round_add_24),
                Pair("Import", R.drawable.round_south_west_24),
                Pair("Export", R.drawable.round_north_east_24),
            )
            val subMenuOptionsList = listOf("html", "csv", "db")
            requireContext().showPopupMenuWithIcons(
                view = btn.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        CreateCollectionBottomSheetFragment.newInstance(
                            eventType = CollectionScreenEvent.CREATE_NEW_COLLECTION
                        ).show(parentFragmentManager, BottomSheetTag.TAG_CREATE_COLLECTION)
                    }
                    optionsList[1].first -> {
                        requireContext().showPopupMenu(
                            view = btn.first,
                            title = "Import",
                            menuList = subMenuOptionsList
                        ) { menuPosition: Int ->
                            when (subMenuOptionsList[menuPosition]) {
                                "html" -> {
                                }
                                "csv" -> {
                                    // TODO csv to json to insert all
                                }
                                "db" -> {
                                    // TODO extract compressed databases file in ext storage -> then copy/replace to databases dir
                                    val uri = requireContext().internalFilesDir(directory = "").toUri()
                                    activity.importDbFrom(uri = uri)
                                }
                            }
                        }
                    }
                    optionsList[2].first -> {
                        requireContext().showPopupMenu(
                            view = btn.first,
                            title = "Export",
                            menuList = subMenuOptionsList
                        ) { menuPosition: Int ->
                            when (subMenuOptionsList[menuPosition]) {
                                "html" -> {
                                }
                                "csv" -> {
                                    // TODO get json from collections, then convert json to csv
                                }
                                "db" -> {
                                    // TODO temp measure. store in ext db
                                    // TODO compress databases dir in internal storage -> then copy to specified ext storage
                                    val path = requireContext().internalFilesDir(directory = "COLLECTIONS_BACKUP").absolutePath
                                    requireContext().exportDbTo(path = path)
                                }
                            }
                        }
                    }
                }
            }
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = collectionsViewModel.getAllCollections()) { it: List<CollectionWebPage?> ->
            val collectionsMap = HashMap<String?, ArrayList<CollectionWebPage?>>()
            val linksCollectionsList = mutableListOf<LinksCollection?>()
            it.forEach {
                val collectionWebPageList = (collectionsMap.get(it?.collectionTitle) ?: ArrayList()).apply { add(it) }
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