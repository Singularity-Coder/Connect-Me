package com.singularitycoder.connectme.search.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.Preferences
import com.singularitycoder.connectme.helpers.constants.SearchEngine
import com.singularitycoder.connectme.helpers.searchSuggestions.BingSearchSuggestionProvider
import com.singularitycoder.connectme.helpers.searchSuggestions.DuckSearchSuggestionProvider
import com.singularitycoder.connectme.helpers.searchSuggestions.GoogleSearchSuggestionProvider
import com.singularitycoder.connectme.helpers.searchSuggestions.YahooSearchSuggestionProvider
import com.singularitycoder.connectme.search.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.jetty.http.HttpMethod
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val preferences: SharedPreferences,
    private val networkStatus: NetworkStatus
) : ViewModel() {

    private val _searchSuggestionResultsStateFlow = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestionResultsStateFlow = _searchSuggestionResultsStateFlow.asStateFlow()

    private val _insightSharedFlow = MutableSharedFlow<ApiResult>()
    val insightSharedFlow = _insightSharedFlow.asSharedFlow()

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
        }
    }

    fun resetSearchSuggestions() {
        _searchSuggestionResultsStateFlow.value = emptyList()
    }

    fun getTextInsight(content: String) = viewModelScope.launch(IO) {
        try {
            if (networkStatus.isOnline().not()) return@launch
            getResponseFromChatGPT(content)
        } catch (_: Exception) {
        }
    }

    fun getImageInsight(
        prompt: String,
        numOfImages: Int,
        imageSize: String
    ) = viewModelScope.launch(IO) {
        try {
            if (networkStatus.isOnline().not()) return@launch
            getImageResponseFromChatGPT(prompt = prompt, numOfImages = numOfImages, imageSize = imageSize)
        } catch (_: Exception) {
        }
    }

    // https://www.baeldung.com/httpurlconnection-post
    // https://hmkcode.com/android-send-json-data-to-server/
    @Throws(Exception::class)
    private suspend fun getResponseFromChatGPT(content: String) {
        _insightSharedFlow.emit(ApiResult(apiState = ApiState.LOADING, insightType = InsightType.TEXT))
        val selectedOpenAiModel = preferences.getString(Preferences.KEY_OPEN_AI_MODEL, "")
        val jsonObjectContent = JSONObject().apply {
            put("role", "user")
            put("content", content)
        }
        val jsonArrayContent = JSONArray().apply {
            put(jsonObjectContent)
        }
        val jsonObjectRequest = JSONObject().apply {
            put("model", selectedOpenAiModel)
            put("messages", jsonArrayContent.toString())
        }
        makeOpenAiPostRequest(
            url = URL("https://api.openai.com/v1/chat/completions"),
            jsonObjectRequest = jsonObjectRequest,
            onSuccess = { jsonObject: JsonObject? ->
                val insightResponse = ConnectMeUtils.gson.fromJson(jsonObject, InsightObject.Root::class.java)
                val insight = Insight(
                    created = insightResponse.created,
                    insight = insightResponse.choices.firstOrNull()?.message?.content
                )
                _insightSharedFlow.emit(
                    ApiResult(
                        insight = insight,
                        apiState = ApiState.SUCCESS,
                        insightType = InsightType.TEXT
                    )
                )
            },
            onFailure = { errorJsonObject: JsonObject? ->
                val errorObject = ConnectMeUtils.gson.fromJson(errorJsonObject, InsightObject.ErrorObject::class.java)
                _insightSharedFlow.emit(
                    ApiResult(
                        error = errorObject,
                        apiState = ApiState.ERROR,
                        insightType = InsightType.TEXT
                    )
                )
            }
        )
    }

    @Throws(Exception::class)
    private suspend fun getImageResponseFromChatGPT(
        prompt: String,
        numOfImages: Int,
        imageSize: String
    ) {
        _insightSharedFlow.emit(ApiResult(apiState = ApiState.LOADING, insightType = InsightType.IMAGE))
        val jsonObjectRequest = JSONObject().apply {
            put("prompt", prompt)
            put("n", numOfImages)
            put("size", imageSize)
        }
        makeOpenAiPostRequest(
            url = URL("https://api.openai.com/v1/images/generations"),
            jsonObjectRequest = jsonObjectRequest,
            onSuccess = { jsonObject: JsonObject? ->
                val insightResponse = ConnectMeUtils.gson.fromJson(jsonObject, ImageInsightObject.Root::class.java)
                val insight = Insight(
                    created = insightResponse.created,
                    imageList = insightResponse.data
                )
                _insightSharedFlow.emit(
                    ApiResult(
                        insight = insight,
                        apiState = ApiState.SUCCESS,
                        insightType = InsightType.IMAGE
                    )
                )
            },
            onFailure = { errorJsonObject: JsonObject? ->
                val errorObject = ConnectMeUtils.gson.fromJson(errorJsonObject, InsightObject.ErrorObject::class.java)
                _insightSharedFlow.emit(
                    ApiResult(
                        error = errorObject,
                        apiState = ApiState.ERROR,
                        insightType = InsightType.IMAGE
                    )
                )
            }
        )
    }

    private suspend fun makeOpenAiPostRequest(
        url: URL,
        jsonObjectRequest: JSONObject,
        onSuccess: suspend (jsonObject: JsonObject?) -> Unit,
        onFailure: suspend (errorJsonObject: JsonObject?) -> Unit
    ) {
        val encryptedApiSecret = preferences.getString(Preferences.KEY_OPEN_AI_API_SECRET, "")
        val decryptedApiSecret = AesEncryption.decrypt(encryptedApiSecret)

        fun HttpURLConnection.setPostRequestContent() = try {
            val input: ByteArray = jsonObjectRequest.toString().toByteArray(Charsets.UTF_8)
            this.outputStream.apply {
                write(input, 0, input.size)
                close()
            }
        } catch (_: Exception) {
        }

        val connection: HttpURLConnection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = HttpMethod.POST.name
            setRequestProperty("Content-Type", "application/json; utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $decryptedApiSecret")
            doOutput = true
            instanceFollowRedirects = true
            readTimeout = 10.seconds().toInt()
            connectTimeout = 10.seconds().toInt()
            setPostRequestContent()
            connect()
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
