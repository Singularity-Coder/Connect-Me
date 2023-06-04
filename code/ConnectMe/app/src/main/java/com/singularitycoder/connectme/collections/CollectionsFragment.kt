package com.singularitycoder.connectme.collections

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.databinding.FragmentCollectionsBinding
import com.singularitycoder.connectme.helpers.collectLatestLifecycleFlow
import com.singularitycoder.connectme.helpers.hideKeyboard
import com.singularitycoder.connectme.helpers.onImeClick
import com.singularitycoder.connectme.helpers.onSafeClick
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

    private fun FragmentCollectionsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        collectionsAdapter.setOnNewsClickListener { it: LinksCollection? ->
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