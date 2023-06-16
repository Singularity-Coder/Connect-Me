package com.singularitycoder.connectme.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.helpers.constants.FILE_PROVIDER
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Credit: Arunkumar
 * https://github.com/arunkumar9t2/lynket-browser
 * */

private const val ACCEPT = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"

// We will spoof as an iPad so that websites properly expose their shortcut icon. Even Google.com
// does not provide bigger icons when we go as Android.
private const val USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25"


@WorkerThread
suspend fun htmlString(url: String): String? {
    return try {
        val urlConnection: HttpURLConnection = createUrlConnection(url, 10_000)
        val result = suspendCoroutine<String?> { continuation: Continuation<String?> ->
            urlConnection.instanceFollowRedirects = true
            val encoding = urlConnection.contentEncoding
            val inputStream: InputStream = if (encoding != null && encoding.equals("gzip", ignoreCase = true)) {
                GZIPInputStream(urlConnection.inputStream)
            } else if (encoding != null && encoding.equals("deflate", ignoreCase = true)) {
                InflaterInputStream(urlConnection.inputStream, Inflater(true))
            } else {
                urlConnection.inputStream
            }
            val enc: String = Converter.extractEncoding(urlConnection.contentType)
            val result = Converter(url).grabStringFromInputStream(inputStream, enc)
            urlConnection.disconnect()
            println("htmlStringhtmlString: $result")
            continuation.resume(result)
        }
        result
    } catch (e: Exception) {
        println("exccccc: $e")
        null
    }
}

/**
 * Provides a [HttpURLConnection] instance for the given url and timeout
 *
 * @param urlAsStr Url to create a connection for.
 * @param timeout  Timeout
 * @return [HttpURLConnection] instance.
 * @throws IOException
 */
@Throws(IOException::class)
private suspend fun createUrlConnection(urlAsStr: String, timeout: Int): HttpURLConnection {
    val urlConnection = suspendCoroutine<HttpURLConnection> { continuation: Continuation<HttpURLConnection> ->
        val url = URL(urlAsStr)
        //using proxy may increase latency
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("User-Agent", USER_AGENT)
        urlConnection.setRequestProperty("Accept", ACCEPT)
        // suggest respond to be gzipped or deflated (which is just another compression)
        // http://stackoverflow.com/q/3932117
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate")
        urlConnection.connectTimeout = timeout
        urlConnection.readTimeout = timeout
        continuation.resume(urlConnection)
    }
    return urlConnection
}

private class Converter {
    private var maxBytes = 1000000 / 2
    private var encoding: String? = null
    private var url: String? = null

    internal constructor(urlOnlyHint: String?) {
        url = urlOnlyHint
    }

    internal constructor() {}

    fun setMaxBytes(maxBytes: Int): Converter {
        this.maxBytes = maxBytes
        return this
    }

    fun getEncoding(): String {
        return if (encoding == null) "" else encoding!!.lowercase(Locale.getDefault())
    }

    fun grabStringFromInputStream(`is`: InputStream?): String {
        return grabStringFromInputStream(`is`, maxBytes, encoding)
    }

    fun grabStringFromInputStream(`is`: InputStream?, encoding: String?): String {
        return grabStringFromInputStream(`is`, maxBytes, encoding)
    }

    /**
     * reads bytes off the string and returns a string
     *
     * @param is
     * @param maxBytes The max bytes that we want to read from the input stream
     * @return String
     */
    fun grabStringFromInputStream(`is`: InputStream?, maxBytes: Int, encoding: String?): String {
        this.encoding = encoding
        // Http 1.1. standard is iso-8859-1 not utf8 :(
        // but we force utf-8 as youtube assumes it ;)
        if (this.encoding == null || this.encoding!!.isEmpty()) this.encoding = UTF8
        try {
            BufferedInputStream(`is`, K2).use { `in` ->
                val output = ByteArrayOutputStream()

                // detect encoding with the help of meta tag
                try {
                    `in`.mark(K2 * 2)
                    var tmpEncoding = detectCharset("charset=", output, `in`, this.encoding)
                    if (tmpEncoding != null) this.encoding = tmpEncoding else {
                        println("no charset found in first stage")
                        // detect with the help of xml beginning ala encoding="charset"
                        tmpEncoding = detectCharset("encoding=", output, `in`, this.encoding)
                        if (tmpEncoding != null) this.encoding = tmpEncoding else println("no charset found in second stage")
                    }
                    if (!Charset.isSupported(this.encoding)) throw UnsupportedEncodingException(this.encoding)
                } catch (e: UnsupportedEncodingException) {
                    println("Using default encoding:$encoding encoding $url")
                    this.encoding = UTF8
                }

                // SocketException: Connection reset
                // IOException: missing CR    => problem on server (probably some xml character thing?)
                // IOException: Premature EOF => socket unexpectly closed from server
                var bytesRead = output.size()
                val arr = ByteArray(K2)
                while (true) {
                    if (bytesRead >= maxBytes) {
                        println("Maxbyte of $maxBytes exceeded! Maybe html is now broken but try it nevertheless. Url: $url")
                        break
                    }
                    val n = `in`.read(arr)
                    if (n < 0) break
                    bytesRead += n
                    output.write(arr, 0, n)
                }
                return output.toString(this.encoding.toString())
            }
        } catch (e: IOException) {
            println("$e url: $url")
        }
        return ""
    }

    fun grabHeadTag(`is`: InputStream, encoding: String?): String {
        this.encoding = encoding
        // Http 1.1. standard is iso-8859-1 not utf8 :(
        // but we force utf-8 as youtube assumes it ;)
        if (this.encoding == null || this.encoding!!.isEmpty()) this.encoding = UTF8
        val headTagContents = StringBuilder()
        try {
            InputStreamReader(`is`, encoding).use { inputStreamReader ->
                BufferedReader(inputStreamReader).use { bufferedReader ->
                    var temp: String
                    var insideHeadTag = false
                    while (bufferedReader.readLine().also { temp = it } != null) {
                        if (temp.contains("<head")) {
                            insideHeadTag = true
                        }
                        if (insideHeadTag) {
                            headTagContents.append(temp)
                        }
                        if (temp.contains("</head>")) {
                            // Exit
                            break
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println(e)
        }
        return headTagContents.toString()
    }

    /**
     * This method detects the charset even if the first call only returns some
     * bytes. It will read until 4K bytes are reached and then try to determine
     * the encoding
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun detectCharset(
        key: String, bos: ByteArrayOutputStream, `in`: BufferedInputStream,
        enc: String?
    ): String? {
        // Grab better encoding from stream
        val arr = ByteArray(K2)
        var nSum = 0
        while (nSum < K2) {
            val n = `in`.read(arr)
            if (n < 0) break
            nSum += n
            bos.write(arr, 0, n)
        }
        val str = bos.toString(enc)
        var encIndex = str.indexOf(key)
        val clength = key.length
        if (encIndex > 0) {
            val startChar = str[encIndex + clength]
            var lastEncIndex: Int
            if (startChar == '\'') // if we have charset='something'
                lastEncIndex = str.indexOf("'", ++encIndex + clength) else if (startChar == '\"') // if we have charset="something"
                lastEncIndex = str.indexOf("\"", ++encIndex + clength) else {
                // if we have "text/html; charset=utf-8"
                var first = str.indexOf("\"", encIndex + clength)
                if (first < 0) first = Int.MAX_VALUE

                // or "text/html; charset=utf-8 "
                var sec = str.indexOf(" ", encIndex + clength)
                if (sec < 0) sec = Int.MAX_VALUE
                lastEncIndex = Math.min(first, sec)

                // or "text/html; charset=utf-8 '
                val third = str.indexOf("'", encIndex + clength)
                if (third > 0) lastEncIndex = Math.min(lastEncIndex, third)
            }

            // re-read byte array with different encoding
            // assume that the encoding string cannot be greater than 40 chars
            if (lastEncIndex > encIndex + clength && lastEncIndex < encIndex + clength + 40) {
                val tmpEnc = encodingCleanup(str.substring(encIndex + clength, lastEncIndex))
                try {
                    `in`.reset()
                    bos.reset()
                    return tmpEnc
                } catch (ex: IOException) {
                    println("$enc Couldn't reset stream to re-read with new encoding")
                }
            }
        }
        return null
    }

    companion object {
        const val UTF8 = "UTF-8"
        const val ISO = "ISO-8859-1"
        const val K2 = 2048

        /**
         * Tries to extract type of encoding for the given content type.
         *
         * @param contentType Content type gotten from [HttpURLConnection.getContentType]
         * @return
         */
        fun extractEncoding(contentType: String?): String {
            val values: Array<String?> = contentType?.split(";".toRegex())?.toTypedArray() ?: arrayOfNulls(0)
            var charset = ""
            var value: String? = ""
            for (singleValue in values) {
                value = singleValue
                value = value?.trim { it <= ' ' }?.lowercase(Locale.getDefault())
                if (value?.startsWith("charset=") == true) charset = value.substring("charset=".length)
            }
            // http1.1 says ISO-8859-1 is the default charset
            if (charset.isEmpty()) charset = ISO
            return charset
        }

        fun encodingCleanup(str: String): String {
            val sb = StringBuilder()
            var startedWithCorrectString = false
            for (i in 0 until str.length) {
                val c = str[i]
                if (Character.isDigit(c) || Character.isLetter(c) || c == '-' || c == '_') {
                    startedWithCorrectString = true
                    sb.append(c)
                    continue
                }
                if (startedWithCorrectString) break
            }
            return sb.toString().trim { it <= ' ' }
        }
    }
}

// https://stackoverflow.com/questions/18504404/get-the-webarchive-that-by-webview-savewebarchive
fun WebView.loadLocallyArchivedWebsite(archiveFilePath: String, fileName: String) {
    loadDataWithBaseURL(null, "$archiveFilePath/$fileName.mhtml", "application/x-webarchive-xml", "UTF-8", null);
}

// https://github.com/Singularity-Coder/Instant-Android/tree/master/kotlin/AndroidStorageMadness
fun Context.onWebPageLoaded(
    url: String,
    onLoaded: (webView: WebView?, favicon: Bitmap?) -> Unit,
) {
    WebView(this).apply {
        webChromeClient = object : WebChromeClient() {
            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                val favicon = icon?.copy(icon.config, true)
                if (icon?.isRecycled?.not() == true) icon.recycle()
                onLoaded.invoke(view, favicon)
            }
        }
        loadUrl(url)
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun Context.getHtmlOnWebPageLoad(
    url: String,
    onLoaded: (webView: WebView?, html: String?) -> Unit,
) {
    WebView(this).apply {
        settings.javaScriptEnabled = true
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(webView: WebView?, url: String?) {
                // https://stackoverflow.com/questions/8200945/how-to-get-html-content-from-a-webview
                webView?.evaluateJavascript("document.documentElement.outerHTML") { htmlString: String? ->
                    val html = htmlString?.replace("\\u003C", "<")
                    onLoaded.invoke(webView, html)
//                    val result = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            }
        }
        loadUrl(url)
    }
}

// https://github.com/LineageOS/android_packages_apps_Jelly
val ACCEPTED_URI_SCHEMA: Pattern = Pattern.compile(
    "(?i)" +  // switch on case insensitive matching
            "(" +  // begin group for schema
            "(?:http|https|content|file|chrome)://" +
            "|(?:inline|data|about|javascript):" +
            ")" +
            "(.*)"
)

/**
 * Attempts to determine whether user input is a URL or search
 * terms.  Anything with a space is passed to search if canBeSearch is true.
 *
 *
 * Converts to lowercase any mistakenly uppercased schema (i.e.,
 * "Http://" converts to "http://"
 *
 * @return Original or modified URL
 */
// https://github.com/LineageOS/android_packages_apps_Jelly
fun smartUrlFilter(url: String): String? {
    var inUrl = url.trim { it <= ' ' }
    val hasSpace = inUrl.indexOf(' ') != -1
    val matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl)
    if (matcher.matches()) {
        // force scheme to lowercase
        val scheme = matcher.group(1)
        val lcScheme = scheme?.toLowCase()
        if (lcScheme != scheme) {
            inUrl = lcScheme + matcher.group(2)
        }
        if (hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
            inUrl = inUrl.replace(" ", "%20")
        }
        return inUrl
    }
    return if (hasSpace.not() && Patterns.WEB_URL.matcher(inUrl).matches()) {
        URLUtil.guessUrl(inUrl)
    } else null
}

/**
 * Formats a launch-able uri out of the template uri by replacing the template parameters with
 * actual values.
 */
// https://github.com/LineageOS/android_packages_apps_Jelly
fun getFormattedUri(
    templateUri: String?,
    query: String?
): String = URLUtil.composeSearchUrl(
    /* inQuery = */ query,
    /* template = */ templateUri,
    /* queryPlaceHolder = */ "{searchTerms}"
)

// https://github.com/LineageOS/android_packages_apps_Jelly
fun openInNewTab(context: Context, url: String?, incognito: Boolean) {
    val intent = Intent(context, MainActivity::class.java)
    if (url != null && url.isNotEmpty()) {
        intent.data = Uri.parse(url)
    }
    intent.flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    intent.putExtra("extra_incognito", incognito)
    context.startActivity(intent)
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun Context.addShortcut(
    webView: WebView?,
    favicon: Bitmap?,
    themeColorWithFallback: Int = Color.TRANSPARENT
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(this, MainActivity::class.java)
        intent.data = Uri.parse(webView?.url)
        intent.action = Intent.ACTION_MAIN
        val launcherIcon: Icon = if (favicon != null) {
            Icon.createWithBitmap(
                getShortcutIcon(favicon, themeColorWithFallback)
            )
        } else {
            Icon.createWithResource(this, R.mipmap.ic_launcher)
        }
        val title = webView?.title.toString()
        val shortcutInfo = ShortcutInfo.Builder(this, title)
            .setShortLabel(title)
            .setIcon(launcherIcon)
            .setIntent(intent)
            .build()
        getSystemService(ShortcutManager::class.java).requestPinShortcut(shortcutInfo, null)
    }
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun Activity.setImmersiveMode(enable: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(!enable)
        window.insetsController?.let {
            val flags = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            val behavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (enable) {
                it.hide(flags)
                it.systemBarsBehavior = behavior
            } else {
                it.show(flags)
                it.systemBarsBehavior = behavior.inv()
            }
        }
    } else {
        var flags = window.decorView.systemUiVisibility
        val immersiveModeFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        flags = if (enable) {
            flags or immersiveModeFlags
        } else {
            flags and immersiveModeFlags.inv()
        }
        window.decorView.systemUiVisibility = flags
    }
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun WebView.screenshot(): Bitmap {
    this.measure(
        View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    this.layout(0, 0, this.measuredWidth, this.measuredHeight)
    val size = if (this.measuredWidth > this.measuredHeight) this.measuredHeight else this.measuredWidth
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    val height = bitmap.height
    canvas.drawBitmap(bitmap, 0f, height.toFloat(), paint)
    this.draw(canvas)
    return bitmap
}

// https://www.baeldung.com/java-validate-url
@Throws(MalformedURLException::class, URISyntaxException::class)
fun String?.isValidURL(): Boolean {
    return try {
        this ?: return false
        URL(this).toURI()
        true
    } catch (e: MalformedURLException) {
        false
    } catch (e: URISyntaxException) {
        false
    }
}

fun trimHostFrom(url: String?): String? {
    return url
        ?.substringAfter("//")
        ?.substringBefore("/")
}

fun String?.simplifyUrl(): String? {
    return this?.replace(oldValue = "https://www.", newValue = "")
        ?.replace(oldValue = "http://www.", newValue = "")
        ?.replace(oldValue = "http://", newValue = "")
        ?.replace(oldValue = "https://", newValue = "")
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun Context.shareUrl(url: String?, webView: WebView?) {
    if (url == null) return
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_TEXT, url)
    if (url == webView?.url) {
        val file = File(cacheDir, "$timeNow.png")
        try {
            FileOutputStream(file).use { out ->
                val bm = webView.screenshot()
                bm.compress(Bitmap.CompressFormat.PNG, 70, out)
                out.flush()
                out.close()
                intent.putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this, FILE_PROVIDER, file)
                )
                intent.type = "image/png"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: IOException) {
        }
    } else {
        intent.type = "text/plain"
    }
    startActivity(Intent.createChooser(intent, "Send to"))
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun WebView.setDesktopMode(
    isDesktopMode: Boolean,
    desktopUserAgent: String?,
    mobileUserAgent: String?
) {
    this.settings.apply {
        userAgentString = if (isDesktopMode) desktopUserAgent else mobileUserAgent
        useWideViewPort = isDesktopMode
        loadWithOverviewMode = isDesktopMode
    }
    this.reload()
}

fun getHostFrom(url: String?): String = try {
    Uri.parse(url).host ?: ""
} catch (e: Exception) {
    ""
}

/**
  Tests:
  https://video.google.co.uk
  https://twitter.com
  https://en.m.wikipedia.org
 * */
fun getDomainFrom(host: String?): String {
    var website = host?.replace(".m.", ".") ?: "" // This trims mobile subdomain
    if (website.filter { it == '.' }.length > 1) {
        repeat(website.filter { it == '.' }.length - 1) {
            website = website.substringBeforeLast(".") // This trims top-level domains etc
        }
    } else {
        website = website.substringBeforeLast(".")
    }
    if (website.filter { it == '.' }.isNotBlank()) {
        website = website.substringAfter(".") // This trims subdomains if any exist
    }
    return website
}