package com.singularitycoder.connectme.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.connectme.feed.Feed
import com.singularitycoder.connectme.feed.FeedDao
import com.singularitycoder.connectme.helpers.NetworkStatus
import com.singularitycoder.connectme.helpers.inputStreamToString
import com.singularitycoder.connectme.helpers.seconds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.eclipse.jetty.http.HttpMethod
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class WebsiteActionsViewModel @Inject constructor(
    private val networkStatus: NetworkStatus,
    private val feedDao: FeedDao,
) : ViewModel() {

    fun getRssFeedFrom(url: String?) = viewModelScope.launch(IO) {
        try {
            if (networkStatus.isOnline().not()) return@launch
            val connection: HttpURLConnection = suspendCoroutine { it: Continuation<HttpURLConnection> ->
                val httpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = HttpMethod.GET.name
                    setRequestProperty("Content-Type", "application/rss+xml; utf-8")
                    instanceFollowRedirects = true
                    readTimeout = 10.seconds().toInt()
                    connectTimeout = 10.seconds().toInt()
                    connect()
                }
                it.resume(httpURLConnection)
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    try {
                        val inputStream: InputStream = BufferedInputStream(connection.inputStream)
                        var xmlResponseString = inputStreamToString(connection, inputStream) // FIXME not working
                        while (xmlResponseString.substringAfter("<title>").substringBefore("</title>").isBlank().not()) {
                            val title = xmlResponseString.substringAfter("<title>").substringBefore("</title>")
                            val link = xmlResponseString.substringAfter("<link>").substringBefore("</link>")
                            val imageTag = xmlResponseString.substringAfter("<img").substringBefore(">")
                            val image = xmlResponseString.substringAfter("src=\"").substringBefore("\"")
                            val pubDate = xmlResponseString.substringAfter("<pubDate>").substringBefore("</pubDate>")
                            val date = if (pubDate.isBlank().not()) {
                                xmlResponseString.substringAfter("<published>").substringBefore("</published>")
                            } else pubDate
                            val feed = Feed(
                                image = image,
                                title = title,
                                time = date,
                                website = url,
                                link = link
                            )
                            feedDao.insert(feed)
                            xmlResponseString = xmlResponseString
                                .replace(oldValue = "<title>$title</title>", newValue = "")
                                .replace(oldValue = "<link>$link</link>", newValue = "")
                                .replace(oldValue = "<img$imageTag>", newValue = "")
                                .replace(oldValue = "<pubDate>$pubDate</pubDate>", newValue = "")
                                .replace(oldValue = "<published>$date</published>", newValue = "")
                        }
                    } catch (_: Exception) {
                    }
                }
                else -> {
                    try {
                        val errorStream: InputStream = connection.errorStream
                        val errorString = inputStreamToString(connection, errorStream)
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}
