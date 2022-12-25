package com.singularitycoder.connectme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = HomeFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentHomeBinding

    private val homeAdapter = HomeAdapter()
    private val newsList = mutableListOf<News>()

    private var topicParam: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicParam = arguments?.getString(ARG_PARAM_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentHomeBinding.setupUI() {
        rvQuestions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
        }
        homeAdapter.newsList = listOf(
            News(
                imageUrl = "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                title = "Two people stranded in an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.google.com",
                time = "9 hours ago",
                link = ""
            ),
            News(
                imageUrl = "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                title = "Two people stranded in an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.google.com",
                time = "9 hours ago",
                link = ""
            ),
            News(
                imageUrl = "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                title = "Two people stranded in an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.google.com",
                time = "9 hours ago",
                link = ""
            ),
            News(
                imageUrl = "https://images.pexels.com/photos/2850287/pexels-photo-2850287.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                title = "Two people stranded in an unknwon sea. People call it the scary Hahahah phenomenon.",
                source = "www.google.com",
                time = "9 hours ago",
                link = ""
            ),
        )
    }

    private fun FragmentHomeBinding.setupUserActionListeners() {
        homeAdapter.setOnNewsClickListener { it: News ->
        }
    }

    private fun observeForData() {

    }
}

private const val ARG_PARAM_SCREEN_TYPE = "ARG_PARAM_TOPIC"
