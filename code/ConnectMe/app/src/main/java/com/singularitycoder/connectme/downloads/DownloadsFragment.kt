package com.singularitycoder.connectme.downloads

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentDownloadsBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.DUMMY_FACE_URLS_2
import com.singularitycoder.connectme.helpers.constants.Preferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
private const val ARG_PARAM_IS_SELF_PROFILE = "ARG_PARAM_IS_SELF_PROFILE"

@AndroidEntryPoint
class DownloadsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String, isSelfProfile: Boolean) = DownloadsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM_SCREEN_TYPE, screenType)
                putBoolean(ARG_PARAM_IS_SELF_PROFILE, isSelfProfile)
            }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentDownloadsBinding

    private val feedAdapter = DownloadsAdapter()
    private val feedList = mutableListOf<Download>()
    private val sortByOptionsList = listOf(
        "Most recent",
        "File name (A to Z)",
        "File name (Z to A)",
        "Modified (newest first)",
        "Modified (oldest first)",
        "Type (A to Z)",
        "Type (Z to A)",
        "Size (largest first)",
        "Size (smallest first)",
    )

    private var topicParam: String? = null
    private var isSelfProfile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
        isSelfProfile = arguments?.getBoolean(ARG_PARAM_IS_SELF_PROFILE) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentDownloadsBinding.setupUI() {
        layoutSearch.btnMore.icon = requireContext().drawable(R.drawable.ic_round_more_horiz_24)
        layoutSearch.cardSearch.isVisible = isSelfProfile
        layoutSearch.btnMore.isVisible = isSelfProfile
        rvDownloads.apply {
            layoutManager = GridLayoutManager(/* context = */ context, /* spanCount = */ 2)
            adapter = feedAdapter
        }
        lifecycleScope.launch(Default) {
            (0..30).forEach { it: Int ->
                feedList.add(
                    Download(
                        imageUrl = DUMMY_FACE_URLS_2[Random().nextInt(DUMMY_FACE_URLS_2.size)],
                        title = "Cringe Lord lords it over and gives it back to others $it",
                        source = "Cringe Lord lords it over and gives it back to others $it",
                        time = if (isSelfProfile) "58 Mb • 5 hr ago" else "58 Mb",
                        link = "",
                    )
                )
            }
            withContext(Main) {
                feedAdapter.feedList = feedList
                feedAdapter.notifyDataSetChanged()
            }
        }
        preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[0]).apply()
    }

    private fun FragmentDownloadsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        layoutSearch.btnMore.onSafeClick { pair: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Filter", R.drawable.round_filter_list_24),
                Pair("Sort by", R.drawable.round_sort_24),
                Pair("Add New Folder", R.drawable.outline_create_new_folder_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        // Filter by image, video, audio, file - dynamic filter based on the file types provided
                    }
                    optionsList[1].first -> {
                        setupSortOptionsMenu(view = pair.first)
                    }
                    optionsList[2].first -> {
                    }
                }
            }
        }

        feedAdapter.setOnItemClickListener { it: Download ->
        }

        feedAdapter.setOnItemLongClickListener { it: Download ->
            // Open with...
            // Copy link
            // Share
            // Hide
            // Delete
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }
    }

    private fun setupSortOptionsMenu(view: View?) {
        val selectedOpenAiModel = preferences.getString(Preferences.KEY_DOWNLOAD_SORT_BY, "")
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menu.add(Menu.NONE, -1, 0, "Sort by").apply {
            isEnabled = false
        }
        sortByOptionsList.forEach {
            popupMenu.menu.add(
                0, 1, 1, menuIconWithText(
                    icon = requireContext().drawable(R.drawable.round_check_24)?.changeColor(
                        context = requireContext(),
                        color = if (selectedOpenAiModel == it) R.color.purple_500 else android.R.color.transparent
                    ),
                    title = it
                )
            )
        }
        popupMenu.setOnMenuItemClickListener { aiModelMenuItem: MenuItem? ->
            view?.setHapticFeedback()
            when (aiModelMenuItem?.title?.toString()?.trim()) {
                sortByOptionsList[0] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[0]).apply()
                }
                sortByOptionsList[1] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[1]).apply()
                }
                sortByOptionsList[2] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[2]).apply()
                }
                sortByOptionsList[3] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[3]).apply()
                }
                sortByOptionsList[4] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[4]).apply()
                }
                sortByOptionsList[5] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[5]).apply()
                }
                sortByOptionsList[6] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[6]).apply()
                }
                sortByOptionsList[7] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[7]).apply()
                }
                sortByOptionsList[8] -> {
                    preferences.edit().putString(Preferences.KEY_DOWNLOAD_SORT_BY, sortByOptionsList[8]).apply()
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun observeForData() {

    }
}