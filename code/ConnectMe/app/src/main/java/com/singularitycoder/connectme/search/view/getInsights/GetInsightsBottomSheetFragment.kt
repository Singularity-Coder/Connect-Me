package com.singularitycoder.connectme.search.view.getInsights

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.databinding.FragmentGetInsightsBottomSheetBinding
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.*
import com.singularitycoder.connectme.search.model.*
import com.singularitycoder.connectme.search.view.addApiKey.AddApiKeyBottomSheetFragment
import com.singularitycoder.connectme.search.view.imagePreview.ImageViewerBottomSheetFragment
import com.singularitycoder.connectme.search.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @Inject
    lateinit var networkStatus: NetworkStatus

    private lateinit var binding: FragmentGetInsightsBottomSheetBinding
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var animationDrawable: AnimationDrawable

    private val searchViewModel by activityViewModels<SearchViewModel>()
    private val insightsAdapter = InsightsAdapter()
    private val chatSearchAdapter = ChatSearchAdapter()
    private val promptsAdapter = PromptsAdapter()
    private var isTextInsight: Boolean = true
    private var isAllInsightsAdded: Boolean = false
    private var insightOnClickPosition: Int = 0
    private var insightOnClick: Insight? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isVoiceSearchQuery: Boolean = false
    private var apiResult: ApiResult? = null
    private var insightStringsList: List<String?> = emptyList()

    private val recordAudioPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult

        // Permission permanently denied
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.RECORD_AUDIO)) {
            requireContext().showPermissionSettings()
            return@registerForActivityResult
        }

        // This can be called twice. A user can deny the permission twice and then is directed to rationale
        if (isPermissionGranted.not()) {
            return@registerForActivityResult
        }

        if (requireContext().isRecordAudioPermissionGranted().not()) {
            return@registerForActivityResult
        }

        startVoiceSearch()
    }

    // https://github.com/realm/realm-android-adapters/issues/122
//    private val recyclerViewDataChangedObserver = object : RecyclerView.AdapterDataObserver() {
//        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//            super.onItemRangeInserted(positionStart, itemCount)
//            if (isVoiceSearchQuery &&
//                binding.rvInsights.isComputingLayout.not() &&
//                (apiResult?.apiState == ApiState.SUCCESS || apiResult?.apiState == ApiState.ERROR)
//            ) {
//                insightsAdapter.readResponse(recyclerView = binding.rvInsights, position = positionStart)
//                isVoiceSearchQuery = false
//                apiResult = null
//            }
//        }
//    }

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
        stopVoiceSearch()
//        binding.rvInsights.adapter?.unregisterAdapterDataObserver(recyclerViewDataChangedObserver)
        super.onDestroyView()
    }

    // https://stackoverflow.com/questions/15543186/how-do-i-create-colorstatelist-programmatically
    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentGetInsightsBottomSheetBinding.setupUI() {
        enableSoftInput()

        setTransparentBackground()

        requireActivity().setNavigationBarColor(R.color.white)

        setBottomSheetBehaviour()

        initTextToSpeech()

        initVoiceSearch()

        rvInsights.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = insightsAdapter
        }

        rvChatSearch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatSearchAdapter
        }

        rvDefaultPrompts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = promptsAdapter
            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }

        ivChatMode.setPadding(4.dpToPx().toInt(), 4.dpToPx().toInt(), 4.dpToPx().toInt(), 2.dpToPx().toInt())

        etImageQuantity.editText?.setText("2")
        val imageQuantityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, (1..10).map { it.toString() })
        (etImageQuantity.editText as? AutoCompleteTextView)?.setAdapter(imageQuantityAdapter)

        etImageSize.editText?.setText("1024x1024")
        val imageSizeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("256x256", "512x512", "1024x1024"))
        (etImageSize.editText as? AutoCompleteTextView)?.setAdapter(imageSizeAdapter)

        preferences.edit().putString(Preferences.KEY_OPEN_AI_MODEL, OPEN_AI_MODELS_LIST[0]).apply()

        lifecycleScope.launch {
            getInsightStringsList()
            withContext(Main) {
                setSearchList(insightStringsList)
            }
        }
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
                etAskAnything.setSelection(etAskAnything.text?.length ?: 0)
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
                        imageSize = etImageSize.editText?.text.toString().trim(),
                        screen = this@GetInsightsBottomSheetFragment.javaClass.simpleName
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
            cardVoiceSearch.isVisible = it.isNullOrBlank()
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

        ivSearch.onSafeClick {
            lifecycleScope.launch {
                getInsightStringsList()
                withContext(Main) {
                    setSearchList(insightStringsList)
                }
            }
            etSearch.setText("")
            clChatSearch.isVisible = clChatSearch.isVisible.not()
            scrollViewConversation.isVisible = clChatSearch.isVisible.not()
            llAskAnything.isVisible = clChatSearch.isVisible.not()
            if (clChatSearch.isVisible) {
                doAfter(1.seconds()) {
                    etSearch.showKeyboard()
                }
            } else {
                etSearch.hideKeyboard()
            }
        }

        ivMore.onSafeClick { pair: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Add API key", R.drawable.outline_key_24),
                Pair("Select AI Model", R.drawable.robot_black_24dp)
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        AddApiKeyBottomSheetFragment.newInstance().show(requireActivity().supportFragmentManager, BottomSheetTag.TAG_ADD_API_KEY)
                        dismiss()
                    }
                    optionsList[1].first -> {
                        setupAiModelMenu(view = pair.first)
                    }
                }
            }
        }

        ivChatMode.onSafeClick {
            isTextInsight = isTextInsight.not()
            setChatMode()
        }

        insightsAdapter.setOnItemLongClickListener { insight: Insight?, view: View?, position: Int ->
            val popupMenu = PopupMenu(requireContext(), view)
            val menuOptions = listOf("Ask again", "Copy", "Share", "Delete")
            menuOptions.forEach {
                popupMenu.menu.add(
                    0, 1, 1, menuIconWithText(
                        icon = when (it.trim()) {
                            menuOptions[0] -> requireContext().drawable(R.drawable.round_refresh_24)?.changeColor(requireContext(), R.color.purple_500)
                            menuOptions[1] -> requireContext().drawable(R.drawable.baseline_content_copy_24)?.changeColor(requireContext(), R.color.purple_500)
                            menuOptions[2] -> requireContext().drawable(R.drawable.outline_share_24)?.changeColor(requireContext(), R.color.purple_500)
                            menuOptions[3] -> requireContext().drawable(R.drawable.outline_delete_24)?.changeColor(requireContext(), R.color.purple_500)
                            else -> requireContext().drawable(R.drawable.round_check_24)?.changeColor(requireContext(), android.R.color.transparent)
                        },
                        title = it
                    )
                )
            }
            popupMenu.setOnMenuItemClickListener { it: MenuItem? ->
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
                        requireContext().shareTextOrImage(text = insight?.insight)
                    }
                    menuOptions[3] -> {
                        searchViewModel.deleteInsight(insight)
                        insightsAdapter.insightsList.removeAt(position)
                        insightsAdapter.notifyItemRemoved(position)
                    }
                }
                false
            }
            popupMenu.show()
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

        ivVoiceSearch.onSafeClick {
            checkPermissionAndStartSpeechToText()
        }

        clVoiceSearchLayout.onSafeClick {
            stop()
        }

//        rvInsights.adapter?.registerAdapterDataObserver(recyclerViewDataChangedObserver)

        chatSearchAdapter.setOnItemClickListener { it: String? ->
            requireContext().clipboard()?.text = it
            requireContext().showToast("Copied text!")
        }

        etSearch.doAfterTextChanged { query: Editable? ->
            ibClearSearch.isVisible = query.isNullOrBlank().not()

            if (query.isNullOrBlank()) {
                setSearchList(insightStringsList)
                chatSearchAdapter.query = ""
                return@doAfterTextChanged
            }

            val filteredList = insightStringsList.filter { it?.contains(other = query.toString(), ignoreCase = true) == true }
            setSearchList(filteredList)

            chatSearchAdapter.query = query.toString().toLowCase()
        }

        ibClearSearch.onSafeClick {
            etSearch.setText("")
        }

        etSearch.onImeClick {
            etSearch.hideKeyboard()
        }
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

//            fun readResponseIfRecyclerViewLayoutComputed() {
//                if (rvInsights.isComputingLayout.not()) {
//                    insightsAdapter.readResponse(recyclerView = binding.rvInsights)
//                    isVoiceSearchQuery = false
//                } else readResponseIfRecyclerViewLayoutComputed()
//            }

            when (it.apiState) {
                ApiState.LOADING -> {
                    insightsAdapter.insightsList.add(
                        Insight(
                            userType = ChatRole.ASSISTANT.ordinal,
                            insight = if (it.insightType == InsightType.TEXT) "thinking..." else "painting..."
                        )
                    )
                    insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
                    return@collectLatestLifecycleFlow
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
                    insightsAdapter.insightsList.add(Insight(insight = errorMessage/*, imageList = dummyFaceUrls2*/))
                }
                else -> Unit
            }

            this@GetInsightsBottomSheetFragment.apiResult = it
            insightsAdapter.notifyItemInserted(insightsAdapter.insightsList.size)
//            scrollViewConversation.scrollTo(scrollViewConversation.height, scrollViewConversation.height)
            rvInsights.scrollToPosition(insightsAdapter.insightsList.lastIndex)
            searchViewModel.resetInsight()
//            if (isVoiceSearchQuery) readResponseIfRecyclerViewLayoutComputed()
            if (isVoiceSearchQuery) {
                doAfter(1.seconds()) {
                    isVoiceSearchQuery = false
                    insightsAdapter.readResponse(recyclerView = binding.rvInsights)
                }
            }
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(
            flow = searchViewModel.getAllInsightItemsBy(website = getHostFrom(searchViewModel.getWebViewData().url))
        ) { it: List<Insight?> ->
            if (isAllInsightsAdded) return@collectLatestLifecycleFlow
            insightsAdapter.insightsList.addAll(it)
            insightsAdapter.notifyDataSetChanged()
            isAllInsightsAdded = true
        }

        (requireActivity() as MainActivity).collectLatestLifecycleFlow(
            flow = searchViewModel.getPromptBy(website = getHostFrom(searchViewModel.getWebViewData().url))
        ) { prompt: Prompt? ->
            val dynamicPromptMap = try {
                JSONObject(prompt?.promptsJson ?: "").toMap()
            } catch (_: Exception) {
                emptyMap<String, String>()
            }

            val tripleDynamicPromptKeysList = mutableListOf<Triple<String, String, String>>()
            val tripleLocalPromptKeysList = mutableListOf<Triple<String, String, String>>()

            var dynamicItemAddedPosition = 0
            var localItemAddedPosition = 0

            val apiPromptKeysList = dynamicPromptMap.keys.toList()
            val localPromptKeysList = LOCAL_TEXT_PROMPTS_MAP.keys.toList()

            val apiQuotient = if (dynamicPromptMap.isEmpty().not()) apiPromptKeysList.size / 3 else 0
            val localQuotient = localPromptKeysList.size / 3

            if (dynamicPromptMap.isEmpty().not()) {
                (0..apiQuotient).forEach {
                    tripleDynamicPromptKeysList.add(
                        Triple(
                            first = apiPromptKeysList.getOrNull(dynamicItemAddedPosition) ?: "",
                            second = apiPromptKeysList.getOrNull(dynamicItemAddedPosition + 1) ?: "",
                            third = apiPromptKeysList.getOrNull(dynamicItemAddedPosition + 2) ?: ""
                        )
                    )
                    dynamicItemAddedPosition += 3
                }
            }

            (0..localQuotient).forEach {
                tripleLocalPromptKeysList.add(
                    Triple(
                        first = localPromptKeysList.getOrNull(localItemAddedPosition) ?: "",
                        second = localPromptKeysList.getOrNull(localItemAddedPosition + 1) ?: "",
                        third = localPromptKeysList.getOrNull(localItemAddedPosition + 2) ?: ""
                    )
                )
                localItemAddedPosition += 3
            }

            tripleDynamicPromptKeysList.forEach { key: Triple<String, String, String> ->
                promptsAdapter.promptList.add(
                    Triple(
                        Pair(first = key.first, second = dynamicPromptMap.get(key.first).toString()),
                        Pair(first = key.second, second = dynamicPromptMap.get(key.second).toString()),
                        Pair(first = key.third, second = dynamicPromptMap.get(key.third).toString())
                    )
                )
            }

            tripleLocalPromptKeysList.forEach { key: Triple<String, String, String> ->
                promptsAdapter.promptList.add(
                    Triple(
                        Pair(first = key.first, second = LOCAL_TEXT_PROMPTS_MAP.get(key.first).toString()),
                        Pair(first = key.second, second = LOCAL_TEXT_PROMPTS_MAP.get(key.second).toString()),
                        Pair(first = key.third, second = LOCAL_TEXT_PROMPTS_MAP.get(key.third).toString())
                    )
                )
            }

            promptsAdapter.notifyDataSetChanged()
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
        ivChatMode.setImageResource(R.drawable.filter_vintage_black_24dp)
        ivChatMode.setPadding(5.dpToPx().toInt(), 5.dpToPx().toInt(), 5.dpToPx().toInt(), 5.dpToPx().toInt())
        etAskAnything.hint = "Ask for a painting"
        llImageGenerationOptions.isVisible = true
    }

    private fun FragmentGetInsightsBottomSheetBinding.setTextMode() {
        ivChatMode.setImageResource(R.drawable.title_black_24dp)
        ivChatMode.setPadding(4.dpToPx().toInt(), 4.dpToPx().toInt(), 4.dpToPx().toInt(), 2.dpToPx().toInt())
        val website = getHostFrom(url = searchViewModel.getWebViewData().url)
        etAskAnything.hint = "Ask about ${getDomainFrom(host = website)}"
        llImageGenerationOptions.isVisible = false
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
                        start = start,
                        end = end,
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

    // https://gist.github.com/magdamiu/77389efb66ae9e693dcf1d5680fdf531
    private fun setAnimatedGradientBackgroundForVoiceSearch() {
        binding.cardVoiceSearchLayout.setBackgroundResource(R.drawable.animated_voice_search_gradient)
        animationDrawable = binding.cardVoiceSearchLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(500)
        animationDrawable.setExitFadeDuration(500)
    }

    private fun FragmentGetInsightsBottomSheetBinding.initVoiceSearch() {
        setAnimatedGradientBackgroundForVoiceSearch()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                tvSpokenText.hint = ""
            }

            override fun onBeginningOfSpeech() {
                tvSpokenText.hint = "Listening..."
            }

            override fun onRmsChanged(p0: Float) = Unit
            override fun onBufferReceived(p0: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onError(p0: Int) = Unit

            override fun onResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                tvSpokenText.text = data?.firstOrNull()
                etAskAnything.setText(data?.firstOrNull() ?: "")
                stop()
            }

            override fun onPartialResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                tvSpokenText.text = data?.firstOrNull()
            }

            override fun onEvent(p0: Int, p1: Bundle?) = Unit
        })
    }

    private fun startVoiceSearch() {
        if (networkStatus.isOnline().not()) {
            requireContext().showToast("No Internet")
            return
        }
        isVoiceSearchQuery = true
        binding.llAskAnything.isVisible = false
        binding.clVoiceSearchLayout.isVisible = true
        if (animationDrawable.isRunning.not()) animationDrawable.start()
        binding.tvSpokenText.text = "Speak now"
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    private fun stop() {
        if (animationDrawable.isRunning) animationDrawable.stop()
        binding.llAskAnything.isVisible = true
        binding.clVoiceSearchLayout.isVisible = false
        speechRecognizer?.stopListening()
    }

    private fun stopVoiceSearch() {
        stop()
        speechRecognizer?.destroy()
    }

    private fun checkPermissionAndStartSpeechToText() {
        recordAudioPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
    }

    private suspend fun getInsightStringsList() {
        val insightStringsList = searchViewModel.getAllInsightStringsBy(website = getHostFrom(searchViewModel.getWebViewData().url))
        this@GetInsightsBottomSheetFragment.insightStringsList = insightStringsList
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSearchList(insightStringsList: List<String?>) {
        chatSearchAdapter.chatSearchList = insightStringsList
        chatSearchAdapter.notifyDataSetChanged()
    }

    private fun setupAiModelMenu(view: View?) {
        val selectedOpenAiModel = preferences.getString(Preferences.KEY_OPEN_AI_MODEL, "")
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menu.add(Menu.NONE, -1, 0, "Select AI Model").apply {
            isEnabled = false
        }
        OPEN_AI_MODELS_LIST.forEach {
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
                OPEN_AI_MODELS_LIST[0] -> {
                    preferences.edit().putString(Preferences.KEY_OPEN_AI_MODEL, OPEN_AI_MODELS_LIST[0]).apply()
                }
                OPEN_AI_MODELS_LIST[1] -> {
                    preferences.edit().putString(Preferences.KEY_OPEN_AI_MODEL, OPEN_AI_MODELS_LIST[1]).apply()
                }
            }
            false
        }
        popupMenu.show()
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
                    BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                    BottomSheetBehavior.STATE_SETTLING -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }
}