package com.singularitycoder.connectme.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.databinding.FragmentFeedBinding
import com.singularitycoder.connectme.helpers.CodeExecutor
import com.singularitycoder.connectme.helpers.constants.dummyImageUrls
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = FeedFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentFeedBinding

    private val feedAdapter = FeedAdapter()
    private val feedList = mutableListOf<Feed>()

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

    override fun onResume() {
        super.onResume()
//        CodeExecutor.executeCode(
//            javaClassName = "EelloWoruludu",
//            commandsList = listOf("System.out.println(\"Eeeelloololo Waraladuuuuuuuu\")")
//        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFeedBinding.setupUI() {
        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }
        lifecycleScope.launch(Default) {
            (0..30).forEach { it: Int ->
                feedList.add(
                    Feed(
                        imageUrl = dummyImageUrls[Random().nextInt(dummyImageUrls.size)],
                        title = "Party all night got 3 billion people in trouble. Mars police are investigating this on earth.",
                        source = "www.news.com",
                        time = "5 hours ago",
                        link = ""
                    )
                )
            }
            withContext(Main) {
                feedAdapter.feedList = feedList
                feedAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun FragmentFeedBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        feedAdapter.setOnNewsClickListener { it: Feed ->
        }
    }

    private fun observeForData() {

    }
}