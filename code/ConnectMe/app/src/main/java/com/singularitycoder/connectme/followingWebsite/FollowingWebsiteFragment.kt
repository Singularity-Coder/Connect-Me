package com.singularitycoder.connectme.followingWebsite

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentFollowingWebsiteBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.constants.globalLayoutAnimation
import com.singularitycoder.connectme.search.view.peek.PeekBottomSheetFragment
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
        layoutSearch.btnMore.isVisible = false
        rvFollowing.apply {
            layoutAnimation = rvFollowing.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = followingWebsiteAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentFollowingWebsiteBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        followingWebsiteAdapter.setOnClickListener { it: FollowingWebsite? ->
            PeekBottomSheetFragment.newInstance(
                peekUrl = it?.link
            ).show(parentFragmentManager, BottomSheetTag.TAG_PEEK)
        }

        followingWebsiteAdapter.setOnLongClickListener { followingWebsite: FollowingWebsite?, view: View? ->
            val optionsList = listOf(
                Pair("Open in new tab", R.drawable.round_add_circle_outline_24),
                Pair("Open in new private tab", R.drawable.outline_policy_24),
                Pair("Share", R.drawable.outline_share_24),
                Pair("Copy link", R.drawable.baseline_content_copy_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        requireActivity().openSearchScreen(isPrivate = false, linkList = listOf(followingWebsite?.link))
                    }

                    optionsList[1].first -> {
                        requireActivity().openSearchScreen(isPrivate = true, linkList = listOf(followingWebsite?.link))
                    }

                    optionsList[2].first -> {
                        requireContext().shareTextOrImage(text = followingWebsite?.title, title = followingWebsite?.link)
                    }

                    optionsList[3].first -> {
                        requireContext().clipboard()?.text = followingWebsite?.link
                        requireContext().showToast("Copied link")
                    }
                }
            }
        }

        followingWebsiteAdapter.setOnFollowClickListener { it: FollowingWebsite? ->
            followingWebsiteViewModel.deleteItem(it)
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                followingWebsiteAdapter.followingWebsiteList = followingWebsiteList
                followingWebsiteAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }

            followingWebsiteAdapter.followingWebsiteList = followingWebsiteList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            followingWebsiteAdapter.notifyDataSetChanged()
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = followingWebsiteViewModel.getAllFollowingWebsites()) { it: List<FollowingWebsite?> ->
            this.followingWebsiteList = it
            followingWebsiteAdapter.followingWebsiteList = it
            followingWebsiteAdapter.notifyDataSetChanged()
        }
    }
}