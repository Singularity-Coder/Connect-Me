package com.singularitycoder.connectme.search.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.ChatRole
import com.singularitycoder.connectme.helpers.constants.HttpMethod
import com.singularitycoder.connectme.helpers.constants.Preferences
import com.singularitycoder.connectme.helpers.constants.SearchEngine
import com.singularitycoder.connectme.helpers.encryption.AesCbcWithIntegrity
import com.singularitycoder.connectme.helpers.searchSuggestions.BingSearchSuggestionProvider
import com.singularitycoder.connectme.helpers.searchSuggestions.DuckSearchSuggestionProvider
import com.singularitycoder.connectme.helpers.searchSuggestions.GoogleSearchSuggestionProvider
import com.singularitycoder.connectme.helpers.searchSuggestions.YahooSearchSuggestionProvider
import com.singularitycoder.connectme.search.dao.InsightDao
import com.singularitycoder.connectme.search.dao.PromptDao
import com.singularitycoder.connectme.search.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val preferences: SharedPreferences,
    private val networkStatus: NetworkStatus,
    private val insightDao: InsightDao,
    private val promptDao: PromptDao,
) : ViewModel() {

    private val _searchSuggestionResultsStateFlow = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestionResultsStateFlow = _searchSuggestionResultsStateFlow.asStateFlow()

    private val _insightSharedFlow = MutableSharedFlow<ApiResult>()
    val insightSharedFlow = _insightSharedFlow.asSharedFlow()

    private val _webViewDataStateFlow = MutableStateFlow<WebViewData>(WebViewData())
    val webViewDataStateFlow = _webViewDataStateFlow.asStateFlow()

    fun getSearchSuggestions(query: String) = viewModelScope.launch {
        if (query.isBlank()) {
            _searchSuggestionResultsStateFlow.value = emptyList()
            return@launch
        }
        val selectedSearchEngine = preferences.getString(Preferences.KEY_SEARCH_SUGGESTION_PROVIDER, SearchEngine.GOOGLE.name)
        val searchEngine = SearchEngine.valueOf(selectedSearchEngine ?: SearchEngine.GOOGLE.name)
        when (searchEngine) {
            SearchEngine.GOOGLE -> {
                _searchSuggestionResultsStateFlow.value = GoogleSearchSuggestionProvider().fetchSearchSuggestionResultsList(query)
            }
            SearchEngine.BING -> {
                _searchSuggestionResultsStateFlow.value = BingSearchSuggestionProvider().fetchSearchSuggestionResultsList(query)
            }
            SearchEngine.DUCK -> {
                _searchSuggestionResultsStateFlow.value = DuckSearchSuggestionProvider().fetchSearchSuggestionResultsList(query)
            }
            SearchEngine.YAHOO -> {
                _searchSuggestionResultsStateFlow.value = YahooSearchSuggestionProvider().fetchSearchSuggestionResultsList(query)
            }
            else -> Unit
        }
    }

    fun resetSearchSuggestions() {
        _searchSuggestionResultsStateFlow.value = emptyList()
    }

    fun getAllInsightItemsBy(website: String?) = insightDao.getAllByWebsiteStateFlow(website)

    suspend fun getAllInsightStringsBy(website: String?) = insightDao.getAllInsights(website)

    /** https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1
     * [distinctUntilChanged] means collect will not trigger until that specific row is updated.
     * So this wont spam collect for every change in the table. */
    fun getAllInsightsByWebsiteStateFlow(website: String?): Flow<Insight?> {
        return insightDao.getInsightByWebsiteStateFlow(website).distinctUntilChanged()
    }

    fun addInsight(insight: Insight?) = viewModelScope.launch {
        insightDao.insert(insight ?: return@launch)
    }

    fun deleteInsight(insight: Insight?) = viewModelScope.launch {
        insightDao.delete(insight ?: return@launch)
    }

    fun setWebViewData(webViewData: WebViewData) {
        _webViewDataStateFlow.value = webViewData
    }

    fun getWebViewData(): WebViewData = _webViewDataStateFlow.value

    fun getPromptBy(website: String?) = promptDao.getByWebsiteStateFlow(website)

    fun addPrompt(prompt: Prompt?) = viewModelScope.launch {
        promptDao.insert(prompt ?: return@launch)
    }

    fun resetInsight() = viewModelScope.launch(IO) {
        _insightSharedFlow.emit(ApiResult(apiState = ApiState.NONE))
    }

    fun hasPromptList(
        website: String?,
        callback: () -> Unit
    ) = viewModelScope.launch(IO) {
        val hasPromptList = promptDao.getInsightByWebsite(website)?.promptsJson.isNullOrBlank().not()
        withContext(Main) {
            if (hasPromptList) return@withContext
            callback.invoke()
        }
    }

    /** https://learn.deeplearning.ai/chatgpt-prompt-eng/
     * role = "system" is for setting the context/persona. Initial info you dont want the user to see. Used to prep the bot. Make this call before starting the conv.
     * Ex: You are a pro anime otaku and u have lived and breathed the otaku life. Respond to the user with this in mind. */
    fun getTextInsight(
        prompt: String?,
        role: String = ChatRole.USER.name.toLowCase(),
        isSaveToDb: Boolean = true,
        isSendResponse: Boolean = true,
        screen: String? = null
    ) = viewModelScope.launch(IO) {
        try {
            if (networkStatus.isOnline().not()) return@launch
            if (isSendResponse) {
                _insightSharedFlow.emit(ApiResult(apiState = ApiState.LOADING, insightType = InsightType.TEXT, screen = screen))
            }
            val selectedOpenAiModel = preferences.getString(Preferences.KEY_OPEN_AI_MODEL, "")
            val jsonObjectContent = JSONObject().apply {
                put("role", role)
                put("content", prompt)
            }
            val jsonArrayContent = JSONArray().apply {
                put(jsonObjectContent)
            }
            val jsonObjectRequest = JSONObject().apply {
                put("model", selectedOpenAiModel)
                put("messages", jsonArrayContent.toString())
            }
            makeOpenAiPostRequest(
                url = "https://api.openai.com/v1/chat/completions",
                jsonObjectRequest = jsonObjectRequest,
                onSuccess = { jsonObject: JsonObject? ->
                    if (isSendResponse.not()) return@makeOpenAiPostRequest
                    val insightResponse = ConnectMeUtils.gson.fromJson(jsonObject, InsightObject.Root::class.java)
                    val insight = Insight(
                        created = insightResponse.created,
                        insight = insightResponse.choices.firstOrNull()?.message?.content,
                        website = getHostFrom(url = _webViewDataStateFlow.value.url)
                    )
                    _insightSharedFlow.emit(
                        ApiResult(
                            insight = insight,
                            apiState = ApiState.SUCCESS,
                            insightType = InsightType.TEXT,
                            screen = screen
                        )
                    )
                    if (isSaveToDb) insightDao.insert(insight)
                },
                onFailure = { errorJsonObject: JsonObject? ->
                    if (isSendResponse.not()) return@makeOpenAiPostRequest
                    val errorObject = ConnectMeUtils.gson.fromJson(errorJsonObject, InsightObject.ErrorObject::class.java)
                    _insightSharedFlow.emit(
                        ApiResult(
                            error = errorObject,
                            apiState = ApiState.ERROR,
                            insightType = InsightType.TEXT,
                            screen = screen
                        )
                    )
                }
            )
        } catch (_: Exception) {
            if (isSendResponse.not()) return@launch
            _insightSharedFlow.emit(
                ApiResult(
                    error = InsightObject.ErrorObject(InsightObject.Error("Something went wrong. Try again!")),
                    apiState = ApiState.ERROR,
                    insightType = InsightType.TEXT,
                    screen = screen
                )
            )
        }
    }

    fun getImageInsight(
        prompt: String?,
        numOfImages: Int,
        imageSize: String,
        screen: String? = null
    ) = viewModelScope.launch(IO) {
        try {
            if (networkStatus.isOnline().not()) return@launch
            _insightSharedFlow.emit(ApiResult(apiState = ApiState.LOADING, insightType = InsightType.IMAGE, screen = screen))
            val jsonObjectRequest = JSONObject().apply {
                put("prompt", prompt)
                put("n", numOfImages)
                put("size", imageSize)
            }
            makeOpenAiPostRequest(
                url = "https://api.openai.com/v1/images/generations",
                jsonObjectRequest = jsonObjectRequest,
                onSuccess = { jsonObject: JsonObject? ->
                    val insightResponse = ConnectMeUtils.gson.fromJson(jsonObject, ImageInsightObject.Root::class.java)
                    val insight = Insight(
                        created = insightResponse.created,
                        imageList = insightResponse.data.map { it.url },
                        website = getHostFrom(url = _webViewDataStateFlow.value.url)
                    )
                    _insightSharedFlow.emit(
                        ApiResult(
                            insight = insight,
                            apiState = ApiState.SUCCESS,
                            insightType = InsightType.IMAGE,
                            screen = screen
                        )
                    )
                    insightDao.insert(insight)
                },
                onFailure = { errorJsonObject: JsonObject? ->
                    val errorObject = ConnectMeUtils.gson.fromJson(errorJsonObject, InsightObject.ErrorObject::class.java)
                    _insightSharedFlow.emit(
                        ApiResult(
                            error = errorObject,
                            apiState = ApiState.ERROR,
                            insightType = InsightType.IMAGE,
                            screen = screen
                        )
                    )
                }
            )
        } catch (_: Exception) {
            _insightSharedFlow.emit(
                ApiResult(
                    error = InsightObject.ErrorObject(InsightObject.Error("Something went wrong. Try again!")),
                    apiState = ApiState.ERROR,
                    insightType = InsightType.IMAGE,
                    screen = screen
                )
            )
        }
    }

    // https://www.baeldung.com/httpurlconnection-post
    // https://hmkcode.com/android-send-json-data-to-server/
    private suspend fun makeOpenAiPostRequest(
        url: String,
        jsonObjectRequest: JSONObject,
        onSuccess: suspend (jsonObject: JsonObject?) -> Unit,
        onFailure: suspend (errorJsonObject: JsonObject?) -> Unit
    ) {
        val encryptedApiKey = preferences.getString(Preferences.KEY_OPEN_AI_API_KEY, "") ?: ""
        val decryptedApiKey = AesCbcWithIntegrity.decryptString(encryptedApiKey, AesCbcWithIntegrity.AES_SECRET_KEY)

        fun HttpURLConnection.setPostRequestContent() = try {
            val input: ByteArray = jsonObjectRequest.toString().toByteArray(Charsets.UTF_8)
            this.outputStream.apply {
                write(input, 0, input.size)
                close()
            }
        } catch (_: Exception) {
        }

        // https://stackoverflow.com/questions/71025062/what-is-suspendcoroutine
        val connection: HttpURLConnection = suspendCoroutine { it: Continuation<HttpURLConnection> ->
            val httpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = HttpMethod.POST.name
                setRequestProperty("Content-Type", "application/json; utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $decryptedApiKey")
                doOutput = true
                instanceFollowRedirects = true
                readTimeout = 10.seconds().toInt()
                connectTimeout = 10.seconds().toInt()
                setPostRequestContent()
                connect()
            }
            it.resume(httpURLConnection)
        }

        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                var jsonObject: JsonObject? = null
                try {
                    val inputStream: InputStream = BufferedInputStream(connection.inputStream)
                    val jsonString = inputStreamToString(connection, inputStream)
                    jsonObject = JsonParser.parseString(jsonString).asJsonObject
                } catch (_: Exception) {
                }
                onSuccess.invoke(jsonObject)
            }
            else -> {
                var errorJsonObject: JsonObject? = null
                try {
                    val errorStream: InputStream = connection.errorStream
                    val errorJsonString = inputStreamToString(connection, errorStream)
                    errorJsonObject = JsonParser.parseString(errorJsonString).asJsonObject
                } catch (_: Exception) {
                }
                onFailure.invoke(errorJsonObject)
            }
        }
    }
}
