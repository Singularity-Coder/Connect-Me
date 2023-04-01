package com.singularitycoder.connectme.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.databinding.FragmentHistoryBinding
import com.singularitycoder.connectme.helpers.constants.dummyFaviconUrls
import com.singularitycoder.connectme.helpers.onSafeClick
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = HistoryFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentHistoryBinding

    private val historyAdapter = HistoryAdapter()
    private val historyList = mutableListOf<History>()

    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentHistoryBinding.setupUI() {
        rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
        (0..30).forEach { it: Int ->
            historyList.add(
                History(
                    imageUrl = dummyFaviconUrls[Random().nextInt(dummyFaviconUrls.size)],
                    title = "The Random Publications",
                    source = "Randomness is random.",
                    time = "5 hours ago",
                    link = "https://www.randompub.com",
                    posts = 17L + it
                )
            )
        }
        historyAdapter.historyList = historyList
    }

    private fun FragmentHistoryBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        btnDeleteAllHistory.onSafeClick {  }

        historyAdapter.setOnClickListener { it: History ->
        }
    }

    private fun observeForData() {

    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
