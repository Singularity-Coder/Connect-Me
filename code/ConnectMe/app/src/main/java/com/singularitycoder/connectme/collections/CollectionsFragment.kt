package com.singularitycoder.connectme.collections

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.databinding.FragmentCollectionsBinding
import com.singularitycoder.connectme.followingWebsite.FollowingWebsite
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteViewModel
import com.singularitycoder.connectme.helpers.collectLatestLifecycleFlow
import com.singularitycoder.connectme.helpers.constants.dummyFaviconUrls
import com.singularitycoder.connectme.search.model.WebApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

//private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class CollectionsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = CollectionsFragment().apply {
//            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentCollectionsBinding

    private var collectionsList = mutableListOf<Collection?>()

    private val collectionsAdapter = CollectionsAdapter()
    private val collectionsViewModel by viewModels<CollectionsViewModel>()

//    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

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
        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectionsAdapter
        }
        lifecycleScope.launch(Default) {
            (0..30).forEach { it: Int ->
                collectionsList.add(
                    Collection(
                        title = "Collection $it",
                        websitesList = (0..4).map {
                            WebApp(
                                imageUrl = dummyFaviconUrls[Random().nextInt(dummyFaviconUrls.size)],
                                title = "The Random Publications",
                                source = "Randomness is random.",
                                time = "5 hours ago",
                                link = "https://www.randompub.com",
                            )
                        }
                    )
                )
            }
            withContext(Main) {
                collectionsAdapter.collectionsList = collectionsList
                collectionsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun FragmentCollectionsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        collectionsAdapter.setOnNewsClickListener { it: Collection? ->
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = collectionsViewModel.getAllCollections()) { it: List<Collection?> ->
//            this.collectionsList = it
//            collectionsAdapter.collectionsList = it
//            collectionsAdapter.notifyDataSetChanged()
        }
    }
}