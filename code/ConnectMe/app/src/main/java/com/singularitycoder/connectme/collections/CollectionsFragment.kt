package com.singularitycoder.connectme.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.databinding.FragmentCollectionsBinding
import com.singularitycoder.connectme.helpers.constants.dummyFaviconUrls
import com.singularitycoder.connectme.search.WebApp
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class CollectionsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = CollectionsFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentCollectionsBinding

    private val feedAdapter = CollectionsAdapter()
    private val feedList = mutableListOf<Collection>()

    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentCollectionsBinding.setupUI() {
        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }
        (0..30).forEach { it: Int ->
            feedList.add(
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
        feedAdapter.feedList = feedList
    }

    private fun FragmentCollectionsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        feedAdapter.setOnNewsClickListener { it: Collection ->
        }
    }

    private fun observeForData() {

    }
}