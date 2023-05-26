package com.singularitycoder.connectme.followingWebsite

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
import com.singularitycoder.connectme.databinding.FragmentFollowingWebsiteBinding
import com.singularitycoder.connectme.helpers.collectLatestLifecycleFlow
import com.singularitycoder.connectme.helpers.onSafeClick
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class FollowingWebsiteFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = FollowingWebsiteFragment().apply {
//            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentFollowingWebsiteBinding

    private val followingWebsiteAdapter = FollowingWebsiteAdapter()
    private val followingWebsiteViewModel by viewModels<FollowingWebsiteViewModel>()

    private var followingWebsiteList = listOf<FollowingWebsite?>()
//    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFollowingWebsiteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFollowingWebsiteBinding.setupUI() {
        rvFollowing.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = followingWebsiteAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFollowingWebsiteBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        followingWebsiteAdapter.setOnClickListener { it: FollowingWebsite? ->

        }

        followingWebsiteAdapter.setOnFollowClickListener { it: FollowingWebsite? ->
            followingWebsiteViewModel.deleteItem(it)
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                followingWebsiteAdapter.followingWebsiteList = followingWebsiteList
                followingWebsiteAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }

            followingWebsiteAdapter.followingWebsiteList = followingWebsiteList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            followingWebsiteAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = followingWebsiteViewModel.getAllFollowingWebsites()) { it: List<FollowingWebsite?> ->
            this.followingWebsiteList = it
            followingWebsiteAdapter.followingWebsiteList = it
            followingWebsiteAdapter.notifyDataSetChanged()
        }
    }
}