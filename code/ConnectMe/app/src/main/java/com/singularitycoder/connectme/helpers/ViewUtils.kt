package com.singularitycoder.connectme.helpers

import android.app.Activity
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.AnimRes
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.singularitycoder.connectme.MainActivity
import com.singularitycoder.connectme.R

fun Context.getThemeAttrColor(attributeColor: Int): Int {
    this.theme.resolveAttribute(attributeColor, ConnectMeUtils.typedValue, true)
    return ConnectMeUtils.typedValue.data
}

// underline text programatically
fun TextView.strike() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

// https://stackoverflow.com/questions/2004344/how-do-i-handle-imeoptions-done-button-click
fun EditText.onImeClick(
    imeAction: Int = EditorInfo.IME_ACTION_DONE,
    callback: () -> Unit
) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == imeAction) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

// https://stackoverflow.com/questions/6115715/how-do-i-programmatically-set-the-background-color-gradient-on-a-custom-title-ba
// https://stackoverflow.com/questions/17823451/set-android-shape-color-programmatically
// https://stackoverflow.com/questions/28578701/how-to-create-android-shape-background-programmatically
fun gradientDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        colors = intArrayOf(
            R.color.purple_500,
            R.color.purple_50,
        )
        orientation = GradientDrawable.Orientation.RIGHT_LEFT
        gradientType = GradientDrawable.SWEEP_GRADIENT
        shape = GradientDrawable.RECTANGLE
    }
}

// https://stackoverflow.com/questions/22192291/how-to-change-the-status-bar-color-in-android
fun Activity.setStatusBarColor(@ColorRes color: Int) {
    window.statusBarColor = ContextCompat.getColor(this, color)
}

fun MainActivity.showScreen(
    fragment: Fragment,
    tag: String,
    isAdd: Boolean = false
) {
    if (isAdd) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_to_left, R.anim.slide_to_right, R.anim.slide_to_left, R.anim.slide_to_right)
            .add(R.id.cl_main_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.cl_main_container, fragment, tag)
            .commit()
    }
}

fun AppCompatActivity.showScreen(
    fragment: Fragment,
    tag: String
) {
    supportFragmentManager.beginTransaction()
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .add(R.id.cl_main_container, fragment, tag)
        .addToBackStack(null)
        .commit()
}

// https://stackoverflow.com/questions/37104960/bottomsheetdialog-with-transparent-background
fun BottomSheetDialogFragment.setTransparentBackground() {
    dialog?.apply {
        // window?.setDimAmount(0.2f) // Set dim amount here
        setOnShowListener {
            val bottomSheet = findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
    }
}

fun TextView.showHideIcon(
    context: Context,
    showTick: Boolean,
    @DrawableRes leftIcon: Int = android.R.drawable.ic_delete,
    @DrawableRes topIcon: Int = android.R.drawable.ic_delete,
    @DrawableRes rightIcon: Int = android.R.drawable.ic_delete,
    @DrawableRes bottomIcon: Int = android.R.drawable.ic_delete,
    @ColorRes leftIconColor: Int = android.R.color.white,
    @ColorRes topIconColor: Int = android.R.color.white,
    @ColorRes rightIconColor: Int = android.R.color.white,
    @ColorRes bottomIconColor: Int = android.R.color.white,
    direction: Int
) {
    val left = 1
    val top = 2
    val right = 3
    val bottom = 4
    val leftRight = 5
    val topBottom = 6

    val leftDrawable = ContextCompat.getDrawable(context, leftIcon)?.changeColor(context = context, color = leftIconColor)
    val topDrawable = ContextCompat.getDrawable(context, topIcon)?.changeColor(context = context, color = topIconColor)
    val rightDrawable = ContextCompat.getDrawable(context, rightIcon)?.changeColor(context = context, color = rightIconColor)
    val bottomDrawable = ContextCompat.getDrawable(context, bottomIcon)?.changeColor(context = context, color = bottomIconColor)

    if (showTick) {
        when (direction) {
            left -> this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null)
            top -> this.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null)
            right -> this.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
            bottom -> this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, bottomDrawable)
            leftRight -> this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, rightDrawable, null)
            topBottom -> this.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, bottomDrawable)
        }
    } else this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}

fun Drawable.changeColor(
    context: Context,
    @ColorRes color: Int
): Drawable {
    val unwrappedDrawable = this
    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, color))
    return this
}

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(this, drawableRes)

fun Context.showAlertDialog(
    title: String = "",
    message: String,
    positiveBtnText: String,
    negativeBtnText: String = "",
    neutralBtnText: String = "",
    positiveAction: () -> Unit = {},
    negativeAction: () -> Unit = {},
    neutralAction: () -> Unit = {},
) {
    MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog).apply {
        setCancelable(false)
        if (title.isNotBlank()) setTitle(title)
        setMessage(message)
        background = drawable(R.drawable.alert_dialog_bg)
        setPositiveButton(positiveBtnText) { dialog, int ->
            positiveAction.invoke()
        }
        if (negativeBtnText.isNotBlank()) {
            setNegativeButton(negativeBtnText) { dialog, int ->
                negativeAction.invoke()
            }
        }
        if (neutralBtnText.isNotBlank()) {
            setNeutralButton(neutralBtnText) { dialog, int ->
                neutralAction.invoke()
            }
        }
        val dialog = create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).apply {
            isAllCaps = false
            setPadding(0, 0, 16.dpToPx().toInt(), 0)
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).apply {
            isAllCaps = false
        }
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).apply {
            isAllCaps = false
            setPadding(16.dpToPx().toInt(), 0, 0, 0)
        }
    }
}

fun Context.showPopup(
    view: View,
    menuList: List<String>,
    onItemClick: (position: Int) -> Unit
) {
    PopupMenu(this, view).apply {
        menuList.forEach {
            menu.add(it)
        }
        setOnMenuItemClickListener { it: MenuItem? ->
            view.setHapticFeedback()
            onItemClick.invoke(menuList.indexOf(it?.title))
            false
        }
        show()
    }
}

// Create custom list item to get correct width
// Refer for a fix - https://stackoverflow.com/questions/14200724/listpopupwindow-not-obeying-wrap-content-width-spec
fun Context.showListPopupMenu(
    anchorView: View,
    adapter: ArrayAdapter<String>,
    onItemClick: (position: Int) -> Unit
) {
//    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, todayOptions)
    ListPopupWindow(this, null, com.google.android.material.R.attr.listPopupWindowStyle).apply {
        this.anchorView = anchorView
        setAdapter(adapter)
//        setContentWidth(ListPopupWindow.WRAP_CONTENT)
//        setContentWidth(measureContentWidth(adapter))
        width = ListPopupWindow.MATCH_PARENT
        setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            view?.setHapticFeedback()
            onItemClick.invoke(position)
            this.dismiss()
        }
        show()
    }
}

// https://stackoverflow.com/questions/28578701/how-to-create-android-shape-background-programmatically
fun Context.createCustomView(backgroundColor: Int, borderColor: Int): View {
    val shape = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f)
        setColor(backgroundColor)
        setStroke(3, borderColor)
    }
    return View(this).apply {
        background = shape
    }
}

// https://stackoverflow.com/questions/5776684/how-can-i-convert-a-view-to-a-drawable
fun createGradientDrawable(width: Int, height: Int): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(Color.TRANSPARENT)
        setSize(width, height)
    }
}

fun Context.layoutAnimationController(@AnimRes animationRes: Int): LayoutAnimationController {
    return AnimationUtils.loadLayoutAnimation(this, animationRes)
}

// To reanimate list if necessary
fun RecyclerView.runLayoutAnimation(@AnimRes animationRes: Int) {
    layoutAnimation = context.layoutAnimationController(animationRes)
    scheduleLayoutAnimation()
}

inline fun <reified T> List<T>.toArrayList(): ArrayList<T> = ArrayList(this)

fun Context.resourceUri(@AnyRes resourceId: Int): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(packageName)
    .path(resourceId.toString())
    .build()

fun Context.getResourceUri(@AnyRes resourceId: Int): Uri {
    return Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/$resourceId")
}

fun Context.clearNotification(notificationId: Int) {
    (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(packageName, notificationId)
}

// https://stackoverflow.com/questions/2228151/how-to-enable-haptic-feedback-on-button-view
fun View.setHapticFeedback() {
    isHapticFeedbackEnabled = true
    performHapticFeedback(
        HapticFeedbackConstants.VIRTUAL_KEY,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING  // Ignore device's setting. Otherwise, you can use FLAG_IGNORE_VIEW_SETTING to ignore view's setting.
    )
}

// https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
fun View.onSafeClick(
    delayAfterClick: Long = 100.milliSeconds(),
    onSafeClick: (Pair<View?, Boolean>) -> Unit
) {
    val onSafeClickListener = OnSafeClickListener(delayAfterClick, onSafeClick)
    setOnClickListener(onSafeClickListener)
}

class OnSafeClickListener(
    private val delayAfterClick: Long,
    private val onSafeClick: (Pair<View?, Boolean>) -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L
    private var isClicked = false

    override fun onClick(v: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if (elapsedRealtime - lastClickTime < delayAfterClick) return
        lastClickTime = elapsedRealtime
        v?.startAnimation(AlphaAnimation(1F, 0.8F))
//        v?.setTouchEffect()
        isClicked = !isClicked
        onSafeClick(v to isClicked)
        v?.setHapticFeedback()
    }
}