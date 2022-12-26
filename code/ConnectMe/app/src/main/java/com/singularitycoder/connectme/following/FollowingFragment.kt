package com.singularitycoder.connectme.following

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.databinding.FragmentFollowingBinding
import com.singularitycoder.connectme.feed.Feed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowingFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = FollowingFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentFollowingBinding

    private val followingAdapter = FollowingAdapter()
    private val followingList = mutableListOf<Following>()

    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentFollowingBinding.setupUI() {
        rvFollowing.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = followingAdapter
        }
        (0..30).forEach { it: Int ->
            followingList.add(
                Following(
                    imageUrl = "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                    title = "The Random Publications",
                    source = "random.com",
                    time = "5 hours ago",
                    link = "",
                    posts = 17L + it
                )
            )
        }
        followingAdapter.followingList = followingList
    }

    private fun FragmentFollowingBinding.setupUserActionListeners() {
        followingAdapter.setOnClickListener { it: Following ->
        }
    }

    private fun observeForData() {

    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
