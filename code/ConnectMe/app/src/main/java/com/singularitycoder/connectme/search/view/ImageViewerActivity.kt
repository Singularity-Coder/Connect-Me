package com.singularitycoder.connectme.search.view

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.transition.Fade
import android.transition.Transition
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.singularitycoder.connectme.R
import com.singularitycoder.connectme.helpers.PullDownLayout
import dagger.hilt.android.components.ActivityComponent
import java.util.*
import javax.inject.Inject


//class ImageViewerActivity : AppCompatActivity(), PullDownLayout.Callback, OnGlobalLayoutListener {
//
//    companion object {
//        const val ATTACHMENTS = "attachments"
//        const val ATTACHMENT_POSITION = "position"
//        const val NAME = "name"
//        const val DATE = "date"
//        const val ITEM_ID = "itemId"
//        private const val UI_FLAGS_DEFAULT = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//    }
//
//    @Inject
//    var viewModelFactory: Factory? = null
//    private var viewModel: ImageViewModel? = null
//    private var layout: PullDownLayout? = null
//    private var appBarLayout: AppBarLayout? = null
//    private var viewPager: ViewPager2? = null
//    private var attachments: List<AttachmentItem>? = null
//    private var conversationMessageId: MessageId? = null
//    private val launcher: ActivityResultLauncher<String>? = registerForActivityResult(CreateDocumentAdvanced()) { uri: Uri? -> onImageUriSelected(uri) }
//
//    fun injectActivity(component: ActivityComponent) {
//        component.inject(this)
//        viewModel = ViewModelProvider(this, viewModelFactory).get(ImageViewModel::class.java)
//    }
//
//    override fun onCreate(state: Bundle?) {
//        super.onCreate(state)
//
//        // Transitions
//        if (state == null) supportPostponeEnterTransition()
//        val window: Window = getWindow()
//        val transition: Transition = Fade()
//        setSceneTransitionAnimation(transition, null, transition)
//
//        // Intent Extras
//        val i: Intent = getIntent()
//        attachments = Objects.requireNonNull<ArrayList<AttachmentItem>?>(i.getParcelableArrayListExtra<Parcelable>(ATTACHMENTS))
//        val position = i.getIntExtra(ATTACHMENT_POSITION, -1)
//        check(position != -1)
//        val name = i.getStringExtra(NAME)
//        val time = i.getLongExtra(DATE, 0)
//        val messageIdBytes = Objects.requireNonNull(i.getByteArrayExtra(ITEM_ID))
//
//        // connect to View Model
//        viewModel.expectAttachments(attachments)
//        viewModel.getSaveState().observeEvent(this) { error: Boolean? -> onImageSaveStateChanged(error) }
//
//        // inflate layout
//        setContentView(R.layout.activity_image)
//        layout = findViewById(R.id.layout)
//        layout.setCallback(this)
//        layout.getViewTreeObserver().addOnGlobalLayoutListener(this)
//
//        // Status Bar
//        window.statusBarColor = Color.TRANSPARENT
//
//        // Toolbar
//        appBarLayout = findViewById(R.id.appBarLayout)
//        val toolbar: Toolbar = Objects.requireNonNull(setUpCustomToolbar(true))
//        val contactName = toolbar.findViewById<TextView>(R.id.contactName)
//        val dateView = toolbar.findViewById<TextView>(R.id.dateView)
//
//        // Set contact name and message time
//        val date: String = formatDateAbsolute(this, time)
//        contactName.text = name
//        dateView.text = date
//        conversationMessageId = MessageId(messageIdBytes)
//
//        // Set up image ViewPager
//        viewPager = findViewById(R.id.viewPager)
//        val pagerAdapter: ImagePagerAdapter = ImagePagerAdapter()
//        viewPager!!.adapter = pagerAdapter
//        viewPager!!.currentItem = position
//        viewModel.getOnImageClicked().observeEvent(this) { clicked: Boolean? -> onImageClicked(clicked) }
//        window.decorView.systemUiVisibility = UI_FLAGS_DEFAULT
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        getMenuInflater().inflate(R.menu.image_actions, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            onBackPressed()
//            return true
//        } else if (item.itemId == R.id.action_save_image) {
//            showSaveImageDialog()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    override fun onGlobalLayout() {
//        viewModel.setToolbarPosition(
//            appBarLayout!!.top, appBarLayout!!.bottom
//        )
//        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this)
//    }
//
//    override fun onPullStart() {
//        appBarLayout!!.animate()
//            .alpha(0f)
//            .start()
//    }
//
//    override fun onPull(progress: Float) {}
//    override fun onPullCancel() {
//        appBarLayout!!.animate()
//            .alpha(1f)
//            .start()
//    }
//
//    override fun onPullComplete() {
//        showStatusBarBeforeFinishing()
//        supportFinishAfterTransition()
//    }
//
//    override fun onBackPressed() {
//        showStatusBarBeforeFinishing()
//        super.onBackPressed()
//    }
//
//    private fun onImageClicked(clicked: Boolean?) {
//        if (clicked != null && clicked) {
//            toggleSystemUi()
//        }
//    }
//
//    private fun toggleSystemUi() {
//        val decorView: View = getWindow().getDecorView()
//        if (appBarLayout!!.visibility == View.VISIBLE) {
//            hideSystemUi(decorView)
//        } else {
//            showSystemUi(decorView)
//        }
//    }
//
//    private fun hideSystemUi(decorView: View) {
//        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or UI_FLAGS_DEFAULT
//        appBarLayout!!.animate()
//            .translationYBy((-1 * appBarLayout!!.height).toFloat())
//            .alpha(0f)
//            .withEndAction { appBarLayout!!.visibility = View.GONE }
//            .start()
//    }
//
//    private fun showSystemUi(decorView: View) {
//        decorView.systemUiVisibility = UI_FLAGS_DEFAULT
//        appBarLayout!!.animate()
//            .translationYBy(appBarLayout!!.height.toFloat())
//            .alpha(1f)
//            .withStartAction { appBarLayout!!.visibility = View.VISIBLE }
//            .start()
//    }
//
//    /**
//     * If we don't show the status bar again before finishing this activity,
//     * the return transition will "jump" down the size of the status bar
//     * when the previous activity (with visible status bar) is shown.
//     */
//    private fun showStatusBarBeforeFinishing() {
//        if (appBarLayout!!.visibility == View.GONE) {
//            val decorView: View = getWindow().getDecorView()
//            decorView.systemUiVisibility = UI_FLAGS_DEFAULT
//        }
//    }
//
//    private fun showSaveImageDialog() {
//        val okListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
//            val name: String = viewModel.getFileName() + "." +
//                    visibleAttachment.getExtension()
//            try {
//                Objects.requireNonNull(launcher)?.launch(name)
//            } catch (e: ActivityNotFoundException) {
//                viewModel.onSaveImageError()
//            }
//        }
//        val builder = AlertDialog.Builder(this, R.style.BriarDialogTheme)
//        builder.setTitle(getString(R.string.dialog_title_save_image))
//        builder.setMessage(getString(R.string.dialog_message_save_image))
//        builder.setIcon(getDialogIcon(this, R.drawable.ic_security))
//        builder.setPositiveButton(R.string.save_image, okListener)
//        builder.setNegativeButton(R.string.cancel, null)
//        builder.show()
//    }
//
//    private fun onImageUriSelected(uri: Uri?) {
//        if (uri == null) return
//        viewModel.saveImage(visibleAttachment, uri)
//    }
//
//    private fun onImageSaveStateChanged(error: Boolean?) {
//        if (error == null) return
//        val stringRes: Int = if (error) R.string.save_image_error else R.string.save_image_success
//        val colorRes: Int = if (error) R.color.briar_red_500 else R.color.briar_primary
//        BriarSnackbarBuilder()
//            .setBackgroundColor(colorRes)
//            .make(layout, stringRes, BaseTransientBottomBar.LENGTH_LONG)
//            .show()
//    }
//
//    val visibleAttachment: AttachmentItem
//        get() = attachments!![viewPager!!.currentItem]
//
//    private inner class ImagePagerAdapter private constructor() : FragmentStateAdapter(this@ImageActivity) {
//        private var isFirst = true
//        override fun createFragment(position: Int): Fragment {
//            val f: Fragment = ImageFragment.newInstance(
//                attachments!![position], conversationMessageId, isFirst
//            )
//            isFirst = false
//            return f
//        }
//
//        override fun getItemCount(): Int {
//            return attachments!!.size
//        }
//    }
//}
