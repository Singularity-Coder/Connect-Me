package com.singularitycoder.connectme.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.ThisApp
import com.singularitycoder.connectme.databinding.FragmentExploreBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.BottomSheetTag
import com.singularitycoder.connectme.helpers.constants.globalLayoutAnimation
import com.singularitycoder.connectme.helpers.constants.typefaceList
import com.singularitycoder.connectme.search.view.peek.PeekBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class ExploreFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = ExploreFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentExploreBinding

    private val exploreAdapter = ExploreAdapter()
    private val exploreList = mutableListOf<Explore>()

    private var topicParam: String? = null
    private var isInitComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        /** Will not init until collections are loaded. Done for speed */
        if ((activity?.application as? ThisApp)?.isCollectionsScreenLoaded == true && isInitComplete.not()) {
            binding.setupUI()
            binding.setupUserActionListeners()
            binding.observeForData()
            isInitComplete = true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentExploreBinding.setupUI() {
        rvExplore.apply {
            layoutAnimation = rvExplore.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = exploreAdapter
        }
        lifecycleScope.launch(Default) {
            repeat((0..30).count()) {
                exploreList.add(
                    Explore(
                        title = "Party all night got 3 billion people in trouble. Mars police are investigating this on earth.",
                        source = "www.news.com",
                        time = "5 hours ago",
                        link = ""
                    )
                )
            }
            withContext(Main) {
                exploreAdapter.setTypefacePosition(Random.nextInt(typefaceList.size))
                exploreAdapter.exploreList = exploreList
                exploreAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun FragmentExploreBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        exploreAdapter.setOnItemClickListener { it: Explore? ->
            PeekBottomSheetFragment.newInstance(
                peekUrl = it?.link
            ).show(parentFragmentManager, BottomSheetTag.TAG_PEEK)
        }

        exploreAdapter.setOnItemLongClickListener { explore: Explore?, view: View? ->
            val optionsList = listOf(
                Pair("Open in new tab", R.drawable.round_add_circle_outline_24),
                Pair("Open in new private tab", R.drawable.outline_policy_24),
                Pair("Share", R.drawable.outline_share_24),
                Pair("Copy link", R.drawable.baseline_content_copy_24),
                Pair("Delete", R.drawable.outline_delete_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItemText = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        requireActivity().openSearchScreen(isPrivate = false, linkList = listOf(explore?.link))
                    }

                    optionsList[1].first -> {
                        requireActivity().openSearchScreen(isPrivate = true, linkList = listOf(explore?.link))
                    }

                    optionsList[2].first -> {
                        requireContext().shareTextOrImage(text = explore?.title, title = explore?.link)
                    }

                    optionsList[3].first -> {
                        requireContext().clipboard()?.text = explore?.link
                        requireContext().showToast("Copied link")
                    }

                    optionsList[4].first -> {
                        requireContext().showAlertDialog(
                            title = "Delete item",
                            message = explore?.title ?: "",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveBtnColor = R.color.md_red_700,
                            positiveAction = {
                            }
                        )
                    }
                }
            }
        }
    }

    private fun FragmentExploreBinding.observeForData() {
    }
}