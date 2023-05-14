package com.singularitycoder.connectme.search.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import androidx.room.util.query
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentGetInsightsBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import com.singularitycoder.connectme.search.model.*
import com.singularitycoder.connectme.search.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.util.*
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
    private lateinit var textToSpeech: TextToSpeech

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val insightsAdapter = InsightsAdapter()
    private val promptsAdapter = PromptsAdapter()
    private var isTextInsight: Boolean = true
    private var isAllInsightsAdded: Boolean = false
    private var insightOnClickPosition: Int = 0
    private var insightOnClick: Insight? = null

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

    override fun onDestroyView() {
        if (this::textToSpeech.isInitialized) {
            if (textToSpeech.isSpeaking) textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroyView()
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

        initTextToSpeech()

        rvInsights.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = insightsAdapter
        }

        rvDefaultPrompts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = promptsAdapter
            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }

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

        promptsAdapter.setOnItemClickListener { prompt: Pair<String, String> ->
            rvDefaultPrompts.isVisible = false
            if (prompt.first.trim().contains(other = "Translate to ...", ignoreCase = true)) {
                etAskAnything.setText("Translate content to ")
                etAskAnything.showKeyboard()
            } else {
                searchViewModel.getTextInsight(
                    prompt = prompt.second,
                    screen = this@GetInsightsBottomSheetFragment.javaClass.simpleName
                )
                val insight = Insight(
                    userType = ChatRole.USER.ordinal,
                    insight = prompt.first,
                    created = timeNow,
                    website = getHostFrom(searchViewModel.getWebViewData().url)
                )
                insightsAdapter.insightsList.add(insight)
                searchViewModel.addInsight(insight)
                insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
            }
        }

        ibDefaultRequests.onSafeClick {
            if (etAskAnything.text?.isBlank() == true) {
                rvDefaultPrompts.isVisible = rvDefaultPrompts.isVisible.not()
//                if (llDefaultRequests.isVisible.not()) {
//                    etAskAnything.showKeyboard()
//                }
                llImageGenerationOptions.isVisible = false
                setTextMode()
                isTextInsight = true
            } else {
                val insight = Insight(
                    userType = ChatRole.USER.ordinal,
                    insight = etAskAnything.text.toString().trim(),
                    created = timeNow,
                    website = getHostFrom(searchViewModel.getWebViewData().url)
                )
                insightsAdapter.insightsList.add(insight)
                searchViewModel.addInsight(insight)
                insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
                if (isTextInsight) {
                    searchViewModel.getTextInsight(
                        prompt = etAskAnything.text.toString(),
                        screen = this@GetInsightsBottomSheetFragment.javaClass.simpleName
                    )
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
            if (it) rvDefaultPrompts.isVisible = false
            setChatMode()
        }

        cardAskAnything.onSafeClick {
            etAskAnything.showKeyboard()
        }

        etAskAnything.doAfterTextChanged { it: Editable? ->
            if (it.isNullOrBlank().not()) {
                rvDefaultPrompts.isVisible = false
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

        insightsAdapter.setOnItemLongClickListener { insight: Insight?, view: View?, position: Int ->
            PopupMenu(requireContext(), view).apply {
                val menuOptions = listOf("Ask again", "Copy", "Share", "Delete")
                menuOptions.forEach {
                    menu.add(
                        0, 1, 1, menuIconWithText(
                            icon = when (it.trim()) {
                                menuOptions[0] -> requireContext().drawable(R.drawable.round_refresh_24)?.changeColor(requireContext(), R.color.purple_500)
                                menuOptions[1] -> requireContext().drawable(R.drawable.baseline_content_copy_24)?.changeColor(requireContext(), R.color.purple_500)
                                menuOptions[2] -> requireContext().drawable(R.drawable.round_share_24)?.changeColor(requireContext(), R.color.purple_500)
                                menuOptions[3] -> requireContext().drawable(R.drawable.outline_delete_24)?.changeColor(requireContext(), R.color.purple_500)
                                else -> requireContext().drawable(R.drawable.round_check_24)?.changeColor(requireContext(), android.R.color.transparent)
                            },
                            title = it
                        )
                    )
                }
                setOnMenuItemClickListener { it: MenuItem? ->
                    view?.setHapticFeedback()
                    when (it?.title?.toString()?.trim()) {
                        menuOptions[0] -> {
                            etAskAnything.setText(insight?.insight)
                            etAskAnything.setSelection(etAskAnything.text?.length ?: 0)
                        }
                        menuOptions[1] -> {
                            root.context.clipboard()?.text = insight?.insight
                            root.context.showToast("Copied!")
                        }
                        menuOptions[2] -> {
                            requireContext().shareText(text = insight?.insight)
                        }
                        menuOptions[3] -> {
                            searchViewModel.deleteInsight(insight)
                            insightsAdapter.insightsList.removeAt(position)
                            insightsAdapter.notifyItemRemoved(position)
                        }
                    }
                    false
                }
                show()
            }
        }

        insightsAdapter.setOnFullScreenClickListener { insight: Insight?, currentImagePosition: Int ->
            ImageViewerBottomSheetFragment.newInstance(
                imageList = insight?.imageList?.toTypedArray() ?: emptyArray(),
                currentImagePosition = currentImagePosition
            ).show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_IMAGE_VIEWER)
        }

        insightsAdapter.setOnItemClickListener { insight: Insight?, position: Int ->
            insightOnClick = insight
            insightOnClickPosition = position
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
                insightsAdapter.removeTextViewSpan(
                    recyclerView = binding.rvInsights,
                    adapterPosition = insightOnClickPosition,
                    insight = insightOnClick
                )
            } else {
                startTextToSpeech(insight?.insight)
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
            if (it.apiState == ApiState.NONE) return@collectLatestLifecycleFlow
            if (it.screen != this@GetInsightsBottomSheetFragment.javaClass.simpleName) return@collectLatestLifecycleFlow

            fun removeLoadingItem() {
                if (insightsAdapter.insightsList.isEmpty()) return
                insightsAdapter.insightsList.removeAt(insightsAdapter.insightsList.lastIndex)
                insightsAdapter.notifyItemRemoved(insightsAdapter.insightsList.size)
            }

            when (it.apiState) {
                ApiState.LOADING -> {
                    insightsAdapter.insightsList.add(
                        Insight(
                            userType = ChatRole.ASSISTANT.ordinal,
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
                    val errorMessage = if (it.error?.error?.message.isNullOrBlank()) {
                        if (it.error?.error?.code?.contains("invalid_api_key", true) == true) {
                            "Incorrect API key provided. You can find your API key at https://platform.openai.com/account/api-keys."
                        } else {
                            "Something went wrong. Try again!"
                        }
                    } else it.error?.error?.message
                    insightsAdapter.insightsList.add(Insight(insight = errorMessage, imageList = dummyFaceUrls2))
                }
                else -> Unit
            }

            insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
//            scrollViewConversation.scrollTo(scrollViewConversation.height, scrollViewConversation.height)
            rvInsights.scrollToPosition(insightsAdapter.insightsList.lastIndex)
            searchViewModel.resetInsight()
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(
            flow = searchViewModel.getAllInsightsBy(website = getHostFrom(searchViewModel.getWebViewData().url))
        ) { it: List<Insight?> ->
            if (isAllInsightsAdded) return@collectLatestLifecycleFlow
            insightsAdapter.insightsList.addAll(it)
            insightsAdapter.notifyDataSetChanged()
            isAllInsightsAdded = true
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(
            flow = searchViewModel.getPromptBy(website = getHostFrom(searchViewModel.getWebViewData().url))
        ) { prompt: Prompt? ->
            try {
                // TODO fix this later
                val dynamicPromptMap = JSONObject(prompt?.promptsJson ?: "").toMap()

                val triplePromptKeysList = mutableListOf<Triple<String, String, String>>()

                var itemAddedPosition = 0

                val apiPromptKeysList = dynamicPromptMap.keys.toList()
                val localPromptKeysList = localTextPromptsMap.keys.toList()

                val apiQuotient = apiPromptKeysList.size / 3
                val localQuotient = localPromptKeysList.size / 3

                (0..apiQuotient).forEach {
                    triplePromptKeysList.add(
                        Triple(
                            first = apiPromptKeysList.getOrNull(itemAddedPosition) ?: "",
                            second = apiPromptKeysList.getOrNull(itemAddedPosition + 1) ?: "",
                            third = apiPromptKeysList.getOrNull(itemAddedPosition + 2) ?: ""
                        )
                    )
                    itemAddedPosition += 3
                }

                (0..localQuotient).forEach {
                    triplePromptKeysList.add(
                        Triple(
                            first = apiPromptKeysList.getOrNull(itemAddedPosition) ?: "",
                            second = apiPromptKeysList.getOrNull(itemAddedPosition + 1) ?: "",
                            third = apiPromptKeysList.getOrNull(itemAddedPosition + 2) ?: ""
                        )
                    )
                    itemAddedPosition += 3
                }

                triplePromptKeysList.forEach { key: Triple<String, String, String> ->
                    promptsAdapter.promptList.add(
                        Triple(
                            Pair(first = key.first, second = dynamicPromptMap.get(key.first).toString()),
                            Pair(first = key.second, second = dynamicPromptMap.get(key.second).toString()),
                            Pair(first = key.third, second = dynamicPromptMap.get(key.third).toString())
                        )
                    )
                }

                promptsAdapter.notifyDataSetChanged()
            } catch (_: Exception) {
                localTextPromptsMap.keys.forEach { key: String ->
                    promptsAdapter.promptList.add(
                        Triple(
                            Pair(first = key, second = localTextPromptsMap.get(key).toString()),
                            Pair(first = key, second = localTextPromptsMap.get(key).toString()),
                            Pair(first = key, second = localTextPromptsMap.get(key).toString())
                        )
                    )
                }
                promptsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val result: Int = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language not supported for Text-to-Speech!")
                }
            }
        }
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = Unit
            override fun onDone(utteranceId: String) {
                requireActivity().runOnUiThread {
                    insightsAdapter.removeTextViewSpan(
                        recyclerView = binding.rvInsights,
                        adapterPosition = insightOnClickPosition,
                        insight = insightOnClick
                    )
                }
            }

            @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
            override fun onError(utteranceId: String) = Unit

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                requireActivity().runOnUiThread {
                    insightsAdapter.setTtsTextHighlighting(
                        query = utteranceId?.substring(start, end) ?: "",
                        utteranceId = utteranceId,
                        recyclerView = binding.rvInsights,
                        adapterPosition = insightOnClickPosition,
                        insight = insightOnClick
                    )
                }
            }
        })
    }

    private fun startTextToSpeech(textToSpeak: String?) {
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, textToSpeak /* utteranceId = */) }
        textToSpeech.apply {
            speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, textToSpeak)
            playSilentUtterance(1000, TextToSpeech.QUEUE_ADD, textToSpeak) // Stay silent for 1000 ms
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