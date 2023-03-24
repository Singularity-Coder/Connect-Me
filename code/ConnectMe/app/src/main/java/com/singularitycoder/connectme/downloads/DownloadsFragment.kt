package com.singularitycoder.connectme.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.singularitycoder.connectme.databinding.FragmentDownloadsBinding
import com.singularitycoder.connectme.helpers.dummyFaceUrls2
import com.singularitycoder.connectme.helpers.onSafeClick
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"

@AndroidEntryPoint
class DownloadsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = DownloadsFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentDownloadsBinding

    private val feedAdapter = DownloadsAdapter()
    private val feedList = mutableListOf<Download>()

    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
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

    private fun FragmentDownloadsBinding.setupUI() {
        rvDownloads.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = feedAdapter
        }
        (0..30).forEach { it: Int ->
            feedList.add(
                Download(
                    imageUrl = dummyFaceUrls2[Random().nextInt(dummyFaceUrls2.size)],
                    title = "Cringe Lord lords it over and gives it back to others $it",
                    source = "Cringe Lord lords it over and gives it back to others $it",
                    time = "58 Mb * 5 hr ago",
                    link = "",
                )
            )
        }
        feedAdapter.feedList = feedList
    }

    private fun FragmentDownloadsBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        btnDeleteAllHistory.onSafeClick {
        }

        feedAdapter.setOnNewsClickListener { it: Download ->
        }
    }

    private fun observeForData() {

    }
}