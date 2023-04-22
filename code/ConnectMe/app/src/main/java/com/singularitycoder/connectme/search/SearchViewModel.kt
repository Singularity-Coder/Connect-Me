package com.singularitycoder.connectme.search

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

    private val _insightSharedFlow = MutableSharedFlow<Pair<InsightObject.Insight?, InsightObject.ErrorObject?>>()
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

    fun getInsight(content: String) = viewModelScope.launch(IO) {
        try {
            if (networkStatus.isOnline().not()) return@launch
            getResponseFromChatGPT(content)
        } catch (_: Exception) {
        }
    }

    // https://www.baeldung.com/httpurlconnection-post
    // https://hmkcode.com/android-send-json-data-to-server/
    @Throws(Exception::class)
    private suspend fun getResponseFromChatGPT(content: String) {
        val jsonObjectContent = JSONObject().apply {
            put("role", "user")
            put("content", content)
        }
        val jsonArrayContent = JSONArray().apply {
            put(jsonObjectContent)
        }
        val jsonObjectRequest = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", jsonArrayContent.toString())
        }
        val encryptedApiSecret = preferences.getString(Preferences.KEY_OPEN_AI_API_SECRET, "")
        val decryptedApiSecret = AesEncryption.decrypt(encryptedApiSecret)
        val url = URL("https://api.openai.com/v1/chat/completions")

        fun HttpURLConnection.setPostRequestContent() = try {
            println("jsonObjectRequest: $jsonObjectRequest")
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
            instanceFollowRedirects = false
            readTimeout = 20.seconds().toInt()
            connectTimeout = 30.seconds().toInt()
            setPostRequestContent()
            connect()
        }
        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                try {
                    val inputStream: InputStream = BufferedInputStream(connection.inputStream)
                    val jsonString = inputStreamToString(connection, inputStream)
                    val jsonObject: JsonObject = JsonParser.parseString(jsonString).asJsonObject
                    val insightResponse: InsightObject.Insight = ConnectMeUtils.gson.fromJson(jsonObject, InsightObject.Insight::class.java)
                    _insightSharedFlow.emit(Pair(insightResponse, null))
                } catch (_: Exception) {
                }
            }
            else -> {
                try {
                    val errorStream: InputStream = connection.errorStream
                    val errorJsonString = inputStreamToString(connection, errorStream)
                    val errorJsonObject: JsonObject = JsonParser.parseString(errorJsonString).asJsonObject
                    val errorModel: InsightObject.ErrorObject = ConnectMeUtils.gson.fromJson(errorJsonObject, InsightObject.ErrorObject::class.java)
                    _insightSharedFlow.emit(Pair(null, errorModel))
                } catch (_: Exception) {
                }
            }
        }
    }
}
