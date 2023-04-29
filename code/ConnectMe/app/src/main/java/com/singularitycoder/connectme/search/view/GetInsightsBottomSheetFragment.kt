package com.singularitycoder.connectme.search.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentGetInsightsBottomSheetBinding
import com.singularitycoder.connectme.databinding.ListItemIconTextRoundBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import com.singularitycoder.connectme.search.model.ApiResult
import com.singularitycoder.connectme.search.model.ApiState
import com.singularitycoder.connectme.search.model.Insight
import com.singularitycoder.connectme.search.model.InsightType
import com.singularitycoder.connectme.search.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GetInsightsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = GetInsightsBottomSheetFragment()
    }

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var binding: FragmentGetInsightsBottomSheetBinding

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val insightsAdapter = InsightsAdapter()
    private var isTextInsight: Boolean = true
    private var isAllInsightsAdded: Boolean = false

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
        /** https://stackoverflow.com/questions/48002290/show-entire-bottom-sheet-with-edittext-above-keyboard
         * This is for adjusting the input field properly when keyboard visible */
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setTransparentBackground()

        setBottomSheetBehaviour()

        rvInsights.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = insightsAdapter
        }

        layoutItem7.tvText.text = "Is website fishy?" // Trustworthy or not. Check terms/privacy policy to see if they are trying to exploit u, right to repair exists, etc.
        layoutItem8.tvText.text = "Similar sites"
        layoutItem5.tvText.text = "Past misdeeds" // Check if this website or company is involved in shady stuff
        layoutItem6.tvText.text = "Check mood" // U dont want to spoil ur day by reading bad stuff
        layoutItem3.tvText.text = "Summarize"
        layoutItem4.tvText.text = "Find Errors" // Find mistakes, Logical fallacies, Biases, etc
        layoutItem1.tvText.text = "Simplify"
        layoutItem2.tvText.text = "Give analogy"
        // When was the site created. Scammers generally create new sites.

        ibChatMode.setPadding(4.dpToPx().toInt(), 4.dpToPx().toInt(), 4.dpToPx().toInt(), 2.dpToPx().toInt())

        etImageQuantity.editText?.setText("2")
        val imageQuantityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, (1..10).map { it.toString() })
        (etImageQuantity.editText as? AutoCompleteTextView)?.setAdapter(imageQuantityAdapter)

        etImageSize.editText?.setText("1024x1024")
        val imageSizeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("256x256", "512x512", "1024x1024"))
        (etImageSize.editText as? AutoCompleteTextView)?.setAdapter(imageSizeAdapter)

        preferences.edit().putString(Preferences.KEY_OPEN_AI_MODEL, openAiModelsList[0]).apply()
    }

    private fun FragmentGetInsightsBottomSheetBinding.setupUserActionListeners() {
        root.setOnClickListener { }

//        root.setOnVisibilityChangedListener {
//            if (it.not()) return@setOnVisibilityChangedListener
//            doAfter(duration = 500) {
//                etAskAnything.showKeyboard()
//            }
//        }

        listOf(
            layoutItem1,
            layoutItem2,
            layoutItem3,
            layoutItem4,
            layoutItem5,
            layoutItem6,
            layoutItem7,
            layoutItem8,
        ).forEach { layout: ListItemIconTextRoundBinding ->
            layout.root.onSafeClick {
                llDefaultRequests.isVisible = false
                searchViewModel.getTextInsight("Is google trustworthy?")
                val insight = Insight(
                    userType = ChatPeople.USER.ordinal,
                    insight = "Is google trustworthy?",
                    created = timeNow
                )
                insightsAdapter.insightsList.add(insight)
                searchViewModel.addInsight(insight)
                insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
            }
        }

        ibDefaultRequests.onSafeClick {
            if (etAskAnything.text?.isBlank() == true) {
                llDefaultRequests.isVisible = llDefaultRequests.isVisible.not()
//                if (llDefaultRequests.isVisible.not()) {
//                    etAskAnything.showKeyboard()
//                }
                llImageGenerationOptions.isVisible = false
                setTextMode()
                isTextInsight = true
            } else {
                val insight = Insight(
                    userType = ChatPeople.USER.ordinal,
                    insight = etAskAnything.text.toString().trim(),
                    created = timeNow
                )
                insightsAdapter.insightsList.add(insight)
                searchViewModel.addInsight(insight)
                insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
                if (isTextInsight) {
                    searchViewModel.getTextInsight(etAskAnything.text.toString())
                } else {
                    searchViewModel.getImageInsight(
                        prompt = etAskAnything.text.toString(),
                        numOfImages = etImageQuantity.editText?.text.toString().trim().toIntOrNull() ?: 2,
                        imageSize = etImageSize.editText?.text.toString().trim()
                    )
                }
                etAskAnything.setText("")
            }
//            if (llDefaultRequests.isVisible.not()) {
//                rvInsights.minimumHeight = deviceHeight() / 2
//            } else {
//                rvInsights.minimumHeight = LayoutParams.WRAP_CONTENT
//            }
        }

        llImageGenerationOptions.setOnVisibilityChangedListener {
            if (it) llDefaultRequests.isVisible = false
            setChatMode()
        }

        cardAskAnything.onSafeClick {
            etAskAnything.showKeyboard()
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

        etImageSize.editText?.setOnFocusChangeListener { view, isFocused ->
            etImageQuantity.boxStrokeColor = requireContext().color(R.color.black_50)
        }

        ivEditApiKey.onSafeClick {
            AddApiKeyBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_ADD_API_KEY)
            dismiss()
        }

        ivAiSettings.onSafeClick {
            val selectedOpenAiModel = preferences.getString(Preferences.KEY_OPEN_AI_MODEL, "")
            PopupMenu(requireContext(), it.first).apply {
                openAiModelsList.forEach {
                    menu.add(
                        /* p0 = */ 0,
                        /* p1 = */ 1,
                        /* p2 = */ 1,
                        /* p3 = */ menuIconWithText(
                            icon = requireContext().drawable(R.drawable.round_check_24)?.changeColor(requireContext(), if (selectedOpenAiModel == it) R.color.purple_500 else android.R.color.transparent),
                            title = it
                        )
                    )
                }
                setOnMenuItemClickListener { it: MenuItem? ->
                    view?.setHapticFeedback()
                    when (it?.title?.toString()?.trim()) {
                        openAiModelsList[0] -> {
                            preferences.edit().putString(Preferences.KEY_OPEN_AI_MODEL, openAiModelsList[0]).apply()
                        }
                        openAiModelsList[1] -> {
                            preferences.edit().putString(Preferences.KEY_OPEN_AI_MODEL, openAiModelsList[1]).apply()
                        }
                    }
                    false
                }
                show()
            }
        }

        ibChatMode.onSafeClick {
            isTextInsight = isTextInsight.not()
            setChatMode()
        }

        insightsAdapter.setOnItemLongClickListener { insight: Insight?, view: View? ->
            PopupMenu(requireContext(), view).apply {
                val menuOptions = listOf("Copy", "Share", "Delete")
                menuOptions.forEach {
                    menu.add(it)
                }
                setOnMenuItemClickListener { it: MenuItem? ->
                    view?.setHapticFeedback()
                    when (it?.title?.toString()?.trim()) {
                        menuOptions[0] -> {
                            root.context.clipboard()?.text = insight?.insight
                            root.context.showToast("Copied!")
                        }
                        menuOptions[1] -> {
                            requireContext().shareText(text = insight?.insight)
                        }
                        menuOptions[2] -> {
                            searchViewModel.deleteInsight(insight)
                        }
                    }
                    false
                }
                show()
            }
        }
    }

    private fun FragmentGetInsightsBottomSheetBinding.setChatMode() {
        if (isTextInsight) {
            setTextMode()
        } else {
            setImageMode()
        }
    }

    private fun FragmentGetInsightsBottomSheetBinding.setImageMode() {
        ibChatMode.setImageResource(R.drawable.filter_vintage_black_24dp)
        ibChatMode.setPadding(5.dpToPx().toInt(), 5.dpToPx().toInt(), 5.dpToPx().toInt(), 5.dpToPx().toInt())
        etAskAnything.hint = "Ask for a painting"
        llImageGenerationOptions.isVisible = true
    }

    private fun FragmentGetInsightsBottomSheetBinding.setTextMode() {
        ibChatMode.setImageResource(R.drawable.title_black_24dp)
        ibChatMode.setPadding(4.dpToPx().toInt(), 4.dpToPx().toInt(), 4.dpToPx().toInt(), 2.dpToPx().toInt())
        etAskAnything.hint = "Ask about this website"
        llImageGenerationOptions.isVisible = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentGetInsightsBottomSheetBinding.observeForData() {
        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = searchViewModel.insightSharedFlow) { it: ApiResult ->
            fun removeLoadingItem() {
                insightsAdapter.insightsList.removeAt(insightsAdapter.insightsList.lastIndex)
                insightsAdapter.notifyItemRemoved(insightsAdapter.insightsList.size)
            }
            when (it.apiState) {
                ApiState.LOADING -> {
                    insightsAdapter.insightsList.add(
                        Insight(
                            userType = ChatPeople.AI.ordinal,
                            insight = if (it.insightType == InsightType.TEXT) "thinking..." else "painting..."
                        )
                    )
                    insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
                }
                ApiState.SUCCESS -> {
                    removeLoadingItem()
                    insightsAdapter.insightsList.add(it.insight)
                }
                ApiState.ERROR -> {
                    removeLoadingItem()
                    insightsAdapter.insightsList.add(Insight(insight = it.error?.error?.message))
                }
                else -> Unit
            }
            insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
            scrollViewConversation.scrollTo(scrollViewConversation.height, scrollViewConversation.height)
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(flow = searchViewModel.getAllInsights()) { it: List<Insight?> ->
            if (isAllInsightsAdded) return@collectLatestLifecycleFlow
            insightsAdapter.insightsList.addAll(it)
            insightsAdapter.notifyDataSetChanged()
            isAllInsightsAdded = true
        }
    }

    private fun setBottomSheetBehaviour() {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("bottom sheet state: ${behavior.state}")
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> Unit
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}