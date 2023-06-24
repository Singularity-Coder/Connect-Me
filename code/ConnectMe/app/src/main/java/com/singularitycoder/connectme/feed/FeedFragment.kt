package com.singularitycoder.connectme.feed

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentFeedBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.DUMMY_IMAGE_URLS
import com.singularitycoder.connectme.helpers.constants.RSS_FEED_TYPE_LIST
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = FeedFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentFeedBinding

    private val feedAdapter = FeedAdapter()
    private val feedList = mutableListOf<Feed>()

    private var topicParam: String? = null
    private var selectedFeed: String? = "All Feeds"

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
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.round_tune_24)
        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }
        lifecycleScope.launch(Default) {
            (0..30).forEach { it: Int ->
                feedList.add(
                    Feed(
                        imageUrl = DUMMY_IMAGE_URLS[Random().nextInt(DUMMY_IMAGE_URLS.size)],
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

        feedAdapter.setOnItemClickListener { it: Feed? ->
        }

        feedAdapter.setOnItemLongClickListener { feed, view ->
            val optionsList = listOf(
                Pair("Open in new tab", R.drawable.round_add_circle_outline_24),
                Pair("Open in new private tab", R.drawable.outline_policy_24),
                Pair("Save", R.drawable.favorite_border_black_24dp),
                Pair("Share", R.drawable.outline_share_24),
                Pair("Copy link", R.drawable.baseline_content_copy_24),
                Pair("Delete", R.drawable.outline_delete_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {}
                    optionsList[1].first -> {}
                    optionsList[2].first -> {}
                    optionsList[3].first -> {
                        requireContext().shareTextOrImage(text = feed?.title, title = feed?.link)
                    }
                    optionsList[4].first -> {
                        requireContext().clipboard()?.text = feed?.link
                        requireContext().showToast("Copied link")
                    }
                    optionsList[5].first -> {
                        requireContext().showAlertDialog(
                            title = "Delete item",
                            message = feed?.title ?: "",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveAction = {
//                                feedViewModel.deleteItem(feed)
                            }
                        )
                    }
                }
            }
        }

        layoutSearch.btnMore.onSafeClick {
            val popupMenu = PopupMenu(requireContext(), it.first)
            RSS_FEED_TYPE_LIST.forEach {
                popupMenu.menu.add(
                    0, 1, 1, menuIconWithText(
                        icon = requireContext().drawable(R.drawable.round_check_24)?.changeColor(
                            context = requireContext(),
                            color = if (selectedFeed == it) R.color.purple_500 else android.R.color.transparent
                        ),
                        title = it
                    )
                )
            }
            popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
                view?.setHapticFeedback()
                when (it?.title?.toString()?.trim()) {
                    RSS_FEED_TYPE_LIST[0] -> {
                        selectedFeed = RSS_FEED_TYPE_LIST[0]
                    }
                    RSS_FEED_TYPE_LIST[1] -> {
                        selectedFeed = RSS_FEED_TYPE_LIST[1]
                    }
                }
                false
            }
            popupMenu.show()
        }
    }

    private fun observeForData() {

    }
}