package com.singularitycoder.connectme.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.databinding.FragmentFollowingWebsiteBinding
import com.singularitycoder.connectme.helpers.constants.FragmentsTag
import com.singularitycoder.connectme.helpers.constants.UserProfile
import com.singularitycoder.connectme.helpers.constants.DUMMY_FACE_URLS_2
import com.singularitycoder.connectme.helpers.hideKeyboard
import com.singularitycoder.connectme.helpers.onImeClick
import com.singularitycoder.connectme.helpers.showScreen
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val ARG_PARAM_USER_PROFILE_SCREEN_TYPE = "ARG_PARAM_USER_PROFILE_SCREEN_TYPE"

@AndroidEntryPoint
class UserFollowingFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(screenType: String) = UserFollowingFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_USER_PROFILE_SCREEN_TYPE, screenType) }
        }
    }

    private lateinit var binding: FragmentFollowingWebsiteBinding

    private val followingAdapter = UserFollowingAdapter()
    private val followingList = mutableListOf<UserFollowing>()

    private var profileScreenType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileScreenType = arguments?.getString(ARG_PARAM_USER_PROFILE_SCREEN_TYPE)
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

    private fun FragmentFollowingWebsiteBinding.setupUI() {
        when (profileScreenType) {
            UserProfile.FOLLOW.value -> {
                layoutSearch.root.isVisible = false
            }
            UserProfile.FOLLOWING.value -> {
            }
            UserProfile.FOLLOWERS.value -> {}
            UserProfile.FOLLOW_REQUESTS.value -> {
                layoutSearch.root.isVisible = false
            }
        }
        rvFollowing.apply {
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

    private fun FragmentFollowingWebsiteBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        followingAdapter.setOnClickListener { it: UserFollowing ->
            (requireActivity() as MainActivity).showScreen(UserProfileFragment.newInstance(isSelfProfile = false), FragmentsTag.USER_PROFILE, isAdd = true)
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }
    }

    private fun observeForData() {

    }
}