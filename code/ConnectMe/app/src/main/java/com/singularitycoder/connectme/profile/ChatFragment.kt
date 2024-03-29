package com.singularitycoder.connectme.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentChatBinding
import com.singularitycoder.connectme.helpers.constants.DUMMY_FACE_URLS_2
import com.singularitycoder.connectme.helpers.constants.globalLayoutAnimation
import com.singularitycoder.connectme.helpers.layoutAnimationController
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val ARG_PARAM_CHAT_SCREEN_TYPE: String = "ARG_PARAM_CHAT_SCREEN_TYPE"

@AndroidEntryPoint
class ChatFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = ChatFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_CHAT_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentChatBinding

    private val followingAdapter = ChatAdapter()
    private val followingList = mutableListOf<UserFollowing>()

    private var profileScreenType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileScreenType = arguments?.getString(ARG_PARAM_CHAT_SCREEN_TYPE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentChatBinding.setupUI() {
        layoutSearch.btnMore.isVisible = false
        rvChat.apply {
            layoutAnimation = rvChat.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = followingAdapter
            followingAdapter.profileScreenType = profileScreenType
        }
        (0..30).forEach { it: Int ->
            followingList.add(
                UserFollowing(
                    imageUrl = DUMMY_FACE_URLS_2[Random().nextInt(DUMMY_FACE_URLS_2.size)],
                    title = "Cringe Lord $it",
                    source = "Cringe Lord $it",
                    time = "5 hours ago",
                    link = "",
                    posts = 17L + it
                )
            )
        }
        followingAdapter.followingList = followingList
    }

    private fun FragmentChatBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        followingAdapter.setOnClickListener { it: UserFollowing ->
        }
    }

    private fun observeForData() {

    }
}