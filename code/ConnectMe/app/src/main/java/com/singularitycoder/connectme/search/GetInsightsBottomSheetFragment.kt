package com.singularitycoder.connectme.search

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentGetInsightsBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetInsightsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = GetInsightsBottomSheetFragment()
    }

    private lateinit var binding: FragmentGetInsightsBottomSheetBinding

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val insightsAdapter = InsightsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGetInsightsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    // https://stackoverflow.com/questions/15543186/how-do-i-create-colorstatelist-programmatically
    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentGetInsightsBottomSheetBinding.setupUI() {
        setTransparentBackground()
        setBottomSheetBehaviour()
        root.layoutParams.height = deviceHeight() - (deviceHeight() / 10)
        rvInsights.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = insightsAdapter
        }
        layoutItem7.tvText.text = "Is website fishy?" // Trustworthy or not. Check terms/privacy policy to see if they are trying to exploit u, right to repair exists, etc.
        layoutItem8.tvText.text = "Similar sites"
        layoutItem5.tvText.text = "Past misdeeds" // Check if this website or company is involved in shady businesses
        layoutItem6.tvText.text = "Check mood" // U dont want to spoil ur day by reading bad stuff
        layoutItem3.tvText.text = "Summarize"
        layoutItem4.tvText.text = "Translate"
        layoutItem1.tvText.text = "Simplify"
        layoutItem2.tvText.text = "Give analogy"
    }

    private fun FragmentGetInsightsBottomSheetBinding.setupUserActionListeners() {
        root.setOnClickListener { }

        layoutItem1.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
            insightsAdapter.insightsList.add(
                InsightObject.Insight(
                    userType = 0,
                    insight = "Is google trustworthy?"
                )
            )
            insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
        }

        layoutItem2.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        layoutItem3.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        layoutItem4.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        layoutItem5.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        layoutItem6.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        layoutItem7.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        layoutItem8.root.onSafeClick {
            searchViewModel.getInsight("Is google trustworthy?")
        }

        ibDefaultRequests.onSafeClick {
            if (etAskAnything.text?.isBlank() == true) {
                llDefaultRequests.isVisible = llDefaultRequests.isVisible.not()
            } else {
                insightsAdapter.insightsList.add(
                    InsightObject.Insight(
                        userType = 0,
                        insight = etAskAnything.text.toString()
                    )
                )
                insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
                searchViewModel.getInsight(etAskAnything.text.toString())
                etAskAnything.setText("")
            }
            if (llDefaultRequests.isVisible.not()) {
                rvInsights.minimumHeight = deviceHeight() / 3
                etAskAnything.showKeyboard()
            } else {
                rvInsights.minimumHeight = LayoutParams.WRAP_CONTENT
            }
        }

        etAskAnything.doAfterTextChanged { it: Editable? ->
            if (it.isNullOrBlank().not()) {
                llDefaultRequests.isVisible = false
                rvInsights.minimumHeight = deviceHeight() / 3
            }
            ibDefaultRequests.setImageDrawable(
                if (it.isNullOrBlank()) {
                    requireContext().drawable(R.drawable.baseline_auto_fix_high_24)
                } else {
                    requireContext().drawable(R.drawable.round_arrow_upward_24)
                }
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentGetInsightsBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = searchViewModel.insightSharedFlow) { it: Pair<InsightObject.Insight?, InsightObject.ErrorObject?> ->
            if (it.first == null && it.second == null) return@collectLatestLifecycleFlow
            if (it.first == null) {
                insightsAdapter.insightsList.add(InsightObject.Insight(insight = it.second?.error?.message))
            } else {
                insightsAdapter.insightsList.add(InsightObject.Insight(insight = it.first?.choices?.firstOrNull()?.message?.content))
            }
            insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
        }
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheet.layoutParams.height = deviceHeight()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        var oldState = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        oldState = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> {
                        if (oldState == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}