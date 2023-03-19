package com.singularitycoder.connectme.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.databinding.FragmentFeedBinding
import com.singularitycoder.connectme.helpers.dummyImageUrls
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class CollectionsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = CollectionsFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentFeedBinding

    private val feedAdapter = CollectionsAdapter()
    private val feedList = mutableListOf<Collection>()

    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentFeedBinding.setupUI() {
        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }
        feedAdapter.feedList = listOf(
            Collection(
                imageUrl = dummyImageUrls[1],
                title = "Party all night got 3 billion people in trouble. Mars police are investigating this on earth.",
                source = "www.news.com",
                time = "5 hours ago",
                link = ""
            ),
            Collection(
                imageUrl = dummyImageUrls[3],
                title = "Two people stranded on an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.google.com",
                time = "4 hours ago",
                link = ""
            ),
            Collection(
                imageUrl = dummyImageUrls[0],
                title = "Two people stranded in an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.newsplus.com",
                time = "2 hours ago",
                link = ""
            ),
            Collection(
                imageUrl = dummyImageUrls[2],
                title = "Two people stranded in an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.google.com",
                time = "9 hours ago",
                link = ""
            ),
        )
    }

    private fun FragmentFeedBinding.setupUserActionListeners() {
        feedAdapter.setOnNewsClickListener { it: Collection ->
        }
    }

    private fun observeForData() {

    }
}