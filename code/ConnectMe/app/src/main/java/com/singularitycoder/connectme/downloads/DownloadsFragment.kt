package com.singularitycoder.connectme.downloads

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentDownloadsBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.DUMMY_FACE_URLS_2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

    private lateinit var binding: FragmentDownloadsBinding

    private val feedAdapter = DownloadsAdapter()
    private val feedList = mutableListOf<Download>()

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
    }

    private fun FragmentDownloadsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        layoutSearch.btnMore.onSafeClick {
            val optionsList = listOf(
                Pair("Filter", R.drawable.round_filter_list_24),
                Pair("Sort by", R.drawable.round_sort_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = it.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                    }
                    optionsList[1].first -> {
                    }
                }
            }
        }

        feedAdapter.setOnNewsClickListener { it: Download ->
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }
    }

    private fun observeForData() {

    }
}