package com.singularitycoder.connectme.helpers

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

fun View.showSnackBar(
    message: String,
    anchorView: View? = null,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionBtnText: String = "NA",
    action: () -> Unit = {},
) {
    Snackbar.make(this, message, duration).apply {
        this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        if (null != anchorView) this.anchorView = anchorView
        if ("NA" != actionBtnText) setAction(actionBtnText) { action.invoke() }
        this.show()
    }
}

fun getDeviceSize(): Point = try {
    Point(deviceWidth(), deviceHeight())
} catch (e: Exception) {
    e.printStackTrace()
    Point(0, 0)
}

fun deviceWidth() = Resources.getSystem().displayMetrics.widthPixels

fun deviceHeight() = Resources.getSystem().displayMetrics.heightPixels

fun File?.customPath(directory: String?, fileName: String?): String {
    var path = this?.absolutePath

    if (directory != null) {
        path += File.separator + directory
    }

    if (fileName != null) {
        path += File.separator + fileName
    }

    return path ?: ""
}

/** /data/user/0/com.singularitycoder.audioweb/files */
fun Context.internalFilesDir(
    directory: String? = null,
    fileName: String? = null,
): File = File(filesDir.customPath(directory, fileName))

/** /storage/emulated/0/Android/data/com.singularitycoder.audioweb/files */
fun Context.externalFilesDir(
    rootDir: String = "",
    subDir: String? = null,
    fileName: String? = null,
): File = File(getExternalFilesDir(rootDir).customPath(subDir, fileName))

inline fun deleteAllFilesFrom(
    directory: File?,
    withName: String,
    crossinline onDone: () -> Unit = {},
) {
    CoroutineScope(Default).launch {
        directory?.listFiles()?.forEach files@{ it: File? ->
            it ?: return@files
            if (it.name.contains(withName)) {
                if (it.exists()) it.delete()
            }
        }

        withContext(Main) { onDone.invoke() }
    }
}

fun Context.isCameraPresent(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}

val mainActivityPermissions = arrayOf(
    Manifest.permission.READ_CONTACTS,
//    Manifest.permission.WRITE_CONTACTS,
//    Manifest.permission.READ_EXTERNAL_STORAGE,
//    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.CAMERA,
)

fun Context.isLocationPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED

fun Context.showPermissionSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", this@showPermissionSettings.packageName, null)
    }
    startActivity(intent)
}

fun Context.showToast(
    message: String,
    duration: Int = Toast.LENGTH_LONG,
) = Toast.makeText(this, message, duration).show()

fun doAfter(duration: Long, task: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(task, duration)
}

fun Number.dpToPx(): Float = this.toFloat() * Resources.getSystem().displayMetrics.density

fun Number.pxToDp(): Float = this.toFloat() / Resources.getSystem().displayMetrics.density

fun Number.spToPx(): Float = this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity

fun Number.pxToSp(): Float = this.toFloat() / Resources.getSystem().displayMetrics.scaledDensity

// https://stackoverflow.com/questions/44109057/get-video-thumbnail-from-uri
@RequiresApi(Build.VERSION_CODES.O_MR1)
fun Context.getVideoThumbnailBitmap(docUri: Uri): Bitmap? {
    return try {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(this, docUri)
        mmr.getScaledFrameAtTime(
            1000, /* Time in Video */
            MediaMetadataRetriever.OPTION_NEXT_SYNC,
            128,
            128
        )
    } catch (e: Exception) {
        null
    }
}

// https://stackoverflow.com/questions/33222918/sharing-bitmap-via-android-intent
fun Context.shareImageAndTextViaApps(
    uri: Uri,
    title: String,
    subtitle: String,
    intentTitle: String? = null
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, subtitle)
    }
    startActivity(Intent.createChooser(intent, intentTitle ?: "Share to..."))
}

fun Context.makeCall(phoneNum: String) {
    val callIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNum, null))
    callIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    startActivity(callIntent)
}

fun Context.sendSms(phoneNum: String) = try {
    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("sms:$phoneNum")
        putExtra("sms_body", "")
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    }
    startActivity(smsIntent)
} catch (e: Exception) {
}

fun Context.sendWhatsAppMessage(whatsAppPhoneNum: String) {
    try {
        // checks if such an app exists or not
        packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
        val uri = Uri.parse("smsto:$whatsAppPhoneNum")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply { setPackage("com.whatsapp") }
        startActivity(Intent.createChooser(intent, "Dummy Title"))
    } catch (e: PackageManager.NameNotFoundException) {
        Toast.makeText(this, "WhatsApp not found. Install from PlayStore.", Toast.LENGTH_SHORT)
            .show()
        try {
            val uri = Uri.parse("market://details?id=com.whatsapp")
            val intent = Intent(
                Intent.ACTION_VIEW,
                uri
            ).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) }
            startActivity(intent)
        } catch (e: Exception) {
        }
    }
}

fun View.setMargins(
    all: Int? = null,
    start: Int = 0,
    top: Int = 0,
    end: Int = 0,
    bottom: Int = 0
) {
    if (this.layoutParams !is ViewGroup.MarginLayoutParams) return
    val params = this.layoutParams as ViewGroup.MarginLayoutParams
    if (all != null) {
        params.setMargins(all, all, all, all)
    } else {
        params.setMargins(start, top, end, bottom)
    }
    this.requestLayout()
}

// https://stackoverflow.com/questions/2004344/how-do-i-handle-imeoptions-done-button-click
inline fun EditText.onImeDoneClick(crossinline callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE ||
            (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
        ) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }
}

// https://stackoverflow.com/questions/7200535/how-to-convert-views-to-bitmaps
// https://www.youtube.com/watch?v=laySURtxUTk
/** If layout inflated already */
fun View.toBitmapWith(defaultColor: Int): Bitmap? = try {
    val bitmap = Bitmap.createBitmap(
        /* width = */ this.width,
        /* height = */ this.height,
        /* config = */ Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap).apply {
        drawColor(defaultColor)
    }
    this.draw(canvas)
    bitmap
} catch (e: Exception) {
    println("Error: $e")
    null
}

// https://stackoverflow.com/questions/7200535/how-to-convert-views-to-bitmaps
// https://www.youtube.com/watch?v=laySURtxUTk
/** When layout not inflated yet. Measure the view first before extracting the bitmap.
 * Else the width and height will be 0. Which means u cant do view.width & view.height.
 * If unsure of width and height specify MeasureSpec.UNSPECIFIED */
fun View.toBitmapOf(width: Int, height: Int): Bitmap? = try {
    this.measure(
        /* widthMeasureSpec = */ View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        /* heightMeasureSpec = */ View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    )
    val bitmap = Bitmap.createBitmap(
        /* width = */ this.measuredWidth,
        /* height = */ this.measuredHeight,
        /* config = */ Bitmap.Config.ARGB_8888 // Each pixel is set to 4 bytes of memory in this config
    )
    this.layout(
        /* l = */ 0,
        /* t = */ 0,
        /* r = */ this.measuredWidth,
        /* b = */ this.measuredHeight
    )
    this.draw(Canvas(bitmap /* The canvas is drawn on the bitmap */)) // We are basically drawing the view on the Canvas
    bitmap
} catch (e: Exception) {
    println("Error: $e")
    null
}

// Credit: Philip Lackner
fun <T> AppCompatActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun showSettingsAlert(context: Context) {
    AlertDialog.Builder(context).apply {
        setTitle("Enable GPS")
        setMessage("We need GPS location permission for this feature to work!")
        setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
        setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        show()
    }
}

// https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled#:~:text=%40lenik%2C%20some%20devices%20provide%20a,if%20specific%20providers%20are%20enabled.
// https://developer.android.com/reference/android/provider/Settings.Secure#LOCATION_PROVIDERS_ALLOWED
fun Context.isLocationToggleEnabled(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val isProvidersListEmpty = locationManager.getProviders(true).isEmpty()
        locationManager.isLocationEnabled
    } else {
        val locationProviders: String = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        locationProviders.isNotBlank()
    }
}

fun MainActivity.showScreen(
    fragment: Fragment,
    tag: String,
    isAdd: Boolean = false,
    isAddToBackStack: Boolean = true
) {
    if (isAdd) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_to_left, R.anim.slide_to_right, R.anim.slide_to_left, R.anim.slide_to_right)
            .add(R.id.fragment_container_view, fragment, tag)
        if (isAddToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment, tag)
            .commit()
    }
}

fun Context?.clipboard(): ClipboardManager? =
    this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device-or-not
val View.isKeyboardVisible: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        WindowInsetsCompat
            .toWindowInsetsCompat(rootWindowInsets)
            .isVisible(WindowInsetsCompat.Type.ime())
    } else {
        false
    }

/** Request focus before showing keyboard - editText.requestFocus() */
fun EditText?.showKeyboard() {
    this?.requestFocus()
    if (this?.hasFocus() == true) {
        val imm = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

/** Request focus before hiding keyboard - editText.requestFocus() */
fun EditText?.hideKeyboard() {
    this?.requestFocus()
    if (this?.hasFocus() == true) {
        val imm = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(this.windowToken, 0)
    }
}
