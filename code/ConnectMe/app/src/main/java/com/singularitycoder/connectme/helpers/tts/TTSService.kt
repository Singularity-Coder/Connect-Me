package com.singularitycoder.connectme.helpers.tts

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener
import android.speech.tts.UtteranceProgressListener
import android.view.KeyEvent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.singularitycoder.connectme.R
import java.io.IOException
import java.util.Arrays

//class TTSService : Service() {
//    private val blueToothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            println("blueToothReceiver", intent)
//            TTSEngine.get().stop()
//            TTSNotification.showLast()
//        }
//    }
//    var width = 0
//    var height = 0
//    var mAudioManager: AudioManager? = null
//    var mMediaSessionCompat: MediaSessionCompat? = null
//    var isActivated = false
//    var isPlaying = false
//    var audioFocusRequest: Any? = null
//
//    @Volatile
//    var isStartForeground = false
//    var cache: CodecDocument? = null
//    var path: String? = null
//    var wh = 0
//    var emptyPageCount = 0
//    val listener = OnAudioFocusChangeListener { focusChange ->
//        println("onAudioFocusChange $focusChange")
//        if (AppState.get().isEnableAccessibility) {
//            return@OnAudioFocusChangeListener
//        }
//        if (!AppState.get().stopReadingOnCall) {
//            return@OnAudioFocusChangeListener
//        }
//        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//            println("Ingore Duck")
//            return@OnAudioFocusChangeListener
//        }
//        if (focusChange < 0) {
//            isPlaying = TTSEngine.get().isPlaying()
//            println("onAudioFocusChange - Is playing - $isPlaying")
//            TTSEngine.get().stop()
//            TTSNotification.showLast()
//        } else {
//            if (isPlaying) {
//                playPage("", AppSP.get().lastBookPage, null)
//            }
//        }
//        EventBus.getDefault().post(TtsStatus())
//    }
//    private var wakeLock: WakeLock? = null
//
//    init {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                .setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
//                        .build()
//                )
//                .setAcceptsDelayedFocusGain(true)
//                .setWillPauseWhenDucked(false)
//                .setOnAudioFocusChangeListener(listener)
//                .build()
//        }
//    }
//
//    override fun onCreate() {
//        println("Create")
//        startMyForeground()
//        val myPowerManager = getSystemService(POWER_SERVICE) as PowerManager
//        wakeLock = myPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Librera:TTSServiceLock")
//        AppProfile.init(applicationContext)
//        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mAudioManager!!.requestAudioFocus((audioFocusRequest as AudioFocusRequest?)!!)
//        } else {
//            mAudioManager!!.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
//        }
//
//        //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
//        val penginIntent = PendingIntent.getActivity(applicationContext, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE)
//        mMediaSessionCompat = MediaSessionCompat(applicationContext, "Tag", null, penginIntent)
//        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
//        mMediaSessionCompat.setCallback(object : Callback() {
//            fun onMediaButtonEvent(intent: Intent): Boolean {
//                val event = intent.extras!![Intent.EXTRA_KEY_EVENT] as KeyEvent?
//                val isPlaying: Boolean = TTSEngine.get().isPlaying()
//                println("onMediaButtonEvent", "isActivated", isActivated, "isPlaying", isPlaying, "event", event)
//                val list = Arrays.asList(KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE)
//                if (KeyEvent.ACTION_DOWN == event!!.action) {
//                    if (list.contains(event.keyCode)) {
//                        println("onMediaButtonEvent", "isPlaying", isPlaying, "isFastBookmarkByTTS", AppState.get().isFastBookmarkByTTS)
//                        if (AppState.get().isFastBookmarkByTTS) {
//                            if (isPlaying) {
//                                TTSEngine.get().fastTTSBookmakr(baseContext, AppSP.get().lastBookPath, AppSP.get().lastBookPage + 1, AppSP.get().lastBookPageCount)
//                            } else {
//                                playPage("", AppSP.get().lastBookPage, null)
//                            }
//                        } else {
//                            if (isPlaying) {
//                                TTSEngine.get().stop()
//                            } else {
//                                playPage("", AppSP.get().lastBookPage, null)
//                            }
//                        }
//                    } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.keyCode) {
//                        TTSEngine.get().stop()
//                        playPage("", AppSP.get().lastBookPage + 1, null)
//                    } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.keyCode) {
//                        TTSEngine.get().stop()
//                        playPage("", AppSP.get().lastBookPage - 1, null)
//                    }
//                }
//                EventBus.getDefault().post(TtsStatus())
//                TTSNotification.showLast()
//                //  }
//                return true
//            }
//        })
//        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
//        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent)
//
//        //setSessionToken(mMediaSessionCompat.getSessionToken());
//
//
//        // mMediaSessionCompat.setPlaybackState(new
//        // PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).setState(PlaybackStateCompat.STATE_CONNECTING,
//        // 0, 0f).build());
//        TTSEngine.get().getTTS()
//        if (Build.VERSION.SDK_INT >= 24) {
//            val mp = MediaPlayer()
//            try {
//                val afd = assets.openFd("silence.mp3")
//                mp.setDataSource(afd)
//                mp.prepareAsync()
//                mp.start()
//                mp.setOnCompletionListener {
//                    try {
//                        afd.close()
//                    } catch (e: IOException) {
//
//                    }
//                }
//                println("silence")
//            } catch (e: IOException) {
//                println("silence error")
//
//            }
//        }
//        val filter = IntentFilter()
//        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
//        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
//        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
//        registerReceiver(blueToothReceiver, filter)
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    fun startMyForeground() {
//        if (!isStartForeground) {
//            if (TxtUtils.isNotEmpty(AppSP.get().lastBookPath)) {
//                try {
//                    val show: Notification = TTSNotification.show(AppSP.get().lastBookPath, AppSP.get().lastBookPage, AppSP.get().lastBookPageCount)
//                    if (show != null) {
//                        startForeground(TTSNotification.NOT_ID, show)
//                    } else {
//                        startServiceWithNotification()
//                    }
//                } catch (e: Exception) {
//
//                    startServiceWithNotification()
//                }
//            } else {
//                startServiceWithNotification()
//            }
//            isStartForeground = true
//        }
//    }
//
//    private fun startServiceWithNotification() {
//        val stopDestroy = PendingIntent.getService(this, 0, Intent(TTSNotification.TTS_STOP_DESTROY, null, this, TTSService::class.java), PendingIntent.FLAG_IMMUTABLE)
//        val notification: Notification = NotificationCompat.Builder(this, TTSNotification.DEFAULT) //
//            .setSmallIcon(R.drawable.glyphicons_smileys_100_headphones) //
//            .setContentTitle(Apps.getApplicationName(this)) //
//            .setContentText(getString(R.string.please_wait))
//            .addAction(R.drawable.glyphicons_599_menu_close, getString(R.string.stop), stopDestroy) //
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT) //
//            .build()
//        startForeground(TTSNotification.NOT_ID, notification)
//    }
//
//    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        updateTimer()
//        startMyForeground()
//        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent)
//        println("onStartCommand", intent)
//        if (intent == null) {
//            return START_STICKY
//        }
//        println("onStartCommand", intent.action)
//        if (intent.extras != null) {
//            println("onStartCommand", intent.action, intent.extras)
//            for (key in intent.extras!!.keySet()) println(key, "=>", intent.extras!![key])
//        }
//        if (TTSNotification.TTS_STOP_DESTROY.equals(intent.action)) {
//            TTSEngine.get().mp3Destroy()
//            BookCSS.get().mp3BookPath(null)
//            AppState.get().mp3seek = 0
//            TTSEngine.get().stop()
//            TTSEngine.get().stopDestroy()
//            if (wakeLock!!.isHeld) {
//                wakeLock!!.release()
//            }
//            EventBus.getDefault().post(TtsStatus())
//            TTSNotification.hideNotification()
//            stopForeground(true)
//            stopSelf()
//            return START_STICKY
//        }
//        if (TTSNotification.TTS_PLAY_PAUSE.equals(intent.action)) {
//            if (TTSEngine.get().isMp3PlayPause()) {
//                return START_STICKY
//            }
//            if (TTSEngine.get().isPlaying()) {
//                TTSEngine.get().stop()
//            } else {
//                playPage("", AppSP.get().lastBookPage, null)
//            }
//            if (wakeLock!!.isHeld) {
//                wakeLock!!.release()
//            }
//            TTSNotification.showLast()
//        }
//        if (TTSNotification.TTS_PAUSE.equals(intent.action)) {
//            if (TTSEngine.get().isMp3PlayPause()) {
//                return START_STICKY
//            }
//            TTSEngine.get().stop()
//            if (wakeLock!!.isHeld) {
//                wakeLock!!.release()
//            }
//            TTSNotification.showLast()
//        }
//        if (TTSNotification.TTS_PLAY.equals(intent.action)) {
//            if (TTSEngine.get().isMp3PlayPause()) {
//                return START_STICKY
//            }
//            TTSEngine.get().stop()
//            playPage("", AppSP.get().lastBookPage, null)
//            if (!wakeLock!!.isHeld) {
//                wakeLock!!.acquire()
//            }
//            TTSNotification.showLast()
//        }
//        if (TTSNotification.TTS_NEXT.equals(intent.action)) {
//            if (TTSEngine.get().isMp3()) {
//                TTSEngine.get().mp3Next()
//                return START_STICKY
//            }
//            AppSP.get().lastBookParagraph = 0
//            TTSEngine.get().stop()
//            playPage("", AppSP.get().lastBookPage + 1, null)
//            if (!wakeLock!!.isHeld) {
//                wakeLock!!.acquire()
//            }
//        }
//        if (TTSNotification.TTS_PREV.equals(intent.action)) {
//            if (TTSEngine.get().isMp3()) {
//                TTSEngine.get().mp3Prev()
//                return START_STICKY
//            }
//            AppSP.get().lastBookParagraph = 0
//            TTSEngine.get().stop()
//            playPage("", AppSP.get().lastBookPage - 1, null)
//            if (!wakeLock!!.isHeld) {
//                wakeLock!!.acquire()
//            }
//        }
//        if (ACTION_PLAY_CURRENT_PAGE == intent.action) {
//            if (TTSEngine.get().isMp3PlayPause()) {
//                TTSNotification.show(AppSP.get().lastBookPath, -1, -1)
//                return START_STICKY
//            }
//            val pageNumber = intent.getIntExtra(EXTRA_INT, -1)
//            AppSP.get().lastBookPath = intent.getStringExtra(EXTRA_PATH)
//            val anchor = intent.getStringExtra(EXTRA_ANCHOR)
//            if (pageNumber != -1) {
//                playPage("", pageNumber, anchor)
//            }
//            if (!wakeLock!!.isHeld) {
//                wakeLock!!.acquire()
//            }
//        }
//        EventBus.getDefault().post(TtsStatus())
//        return START_STICKY
//    }
//
//    val dC: CodecDocument?
//        get() = try {
//            if (AppSP.get().lastBookPath != null && AppSP.get().lastBookPath.equals(path) && cache != null && wh == AppSP.get().lastBookWidth + AppSP.get().lastBookHeight) {
//                println("CodecDocument from cache", AppSP.get().lastBookPath)
//                return cache
//            }
//            if (cache != null) {
//                cache.recycle()
//                cache = null
//            }
//            path = AppSP.get().lastBookPath
//            cache = ImageExtractor.singleCodecContext(AppSP.get().lastBookPath, "", AppSP.get().lastBookWidth, AppSP.get().lastBookHeight)
//            if (cache == null) {
//                TTSNotification.hideNotification()
//                return null
//            }
//            cache.getPageCount(AppSP.get().lastBookWidth, AppSP.get().lastBookHeight, BookCSS.get().fontSizeSp)
//            wh = AppSP.get().lastBookWidth + AppSP.get().lastBookHeight
//            println("CodecDocument new", AppSP.get().lastBookPath, AppSP.get().lastBookWidth, AppSP.get().lastBookHeight)
//            cache
//        } catch (e: Exception) {
//
//            null
//        }
//
//    private fun playPage(preText: String, pageNumber: Int, anchor: String?) {
//        var preText = preText
//        mMediaSessionCompat.setActive(true)
//        println("playPage", preText, pageNumber, anchor)
//        if (pageNumber != -1) {
//            isActivated = true
//            EventBus.getDefault().post(MessagePageNumber(pageNumber))
//            AppSP.get().lastBookPage = pageNumber
//            val dc: CodecDocument? = dC
//            if (dc == null) {
//                println("CodecDocument", "is NULL")
//                TTSNotification.hideNotification()
//                return
//            }
//            AppSP.get().lastBookPageCount = dc.getPageCount()
//            println("CodecDocument PageCount", pageNumber, AppSP.get().lastBookPageCount)
//            if (pageNumber >= AppSP.get().lastBookPageCount) {
//                Vibro.vibrate(1000)
//                try {
//                    Thread.sleep(2000)
//                } catch (e: InterruptedException) {
//                }
//                TTSEngine.get().getTTS().setOnUtteranceCompletedListener(null)
//                TTSEngine.get().speek(LibreraApp.context.getString(R.string.the_book_is_over))
//                EventBus.getDefault().post(TtsStatus())
//                stopSelf()
//                return
//            }
//            val page: CodecPage = dc.getPage(pageNumber)
//            var pageHTML: String = page.getPageHTML()
//            page.recycle()
//            pageHTML = TxtUtils.replaceHTMLforTTS(pageHTML)
//            if (TxtUtils.isNotEmpty(anchor)) {
//                val indexOf = pageHTML.indexOf(anchor!!)
//                if (indexOf > 0) {
//                    pageHTML = pageHTML.substring(indexOf)
//                    println("find anchor new text", pageHTML)
//                }
//            }
//            println(pageHTML)
//            if (TxtUtils.isEmpty(pageHTML)) {
//                println("empty page play next one", emptyPageCount)
//                emptyPageCount++
//                if (emptyPageCount < 3) {
//                    playPage("", AppSP.get().lastBookPage + 1, null)
//                }
//                return
//            }
//            emptyPageCount = 0
//            val parts: Array<String> = TxtUtils.getParts(pageHTML)
//            var firstPart = if (pageNumber + 1 >= AppSP.get().lastBookPageCount || AppState.get().ttsTunnOnLastWord) pageHTML else parts[0]
//            val secondPart = if (pageNumber + 1 >= AppSP.get().lastBookPageCount || AppState.get().ttsTunnOnLastWord) "" else parts[1]
//            if (TxtUtils.isNotEmpty(preText)) {
//                val last = preText[preText.length - 1]
//                if (last == '-') {
//                    preText = TxtUtils.replaceLast(preText, "-", "")
//                    firstPart = preText + firstPart
//                } else {
//                    firstPart = "$preText $firstPart"
//                }
//            }
//            val preText1 = preText
//            TTSEngine.get().getTTS().setOnUtteranceProgressListener(object : UtteranceProgressListener() {
//                override fun onStart(utteranceId: String) {
//                    println("onUtteranceCompleted onStart $utteranceId")
//                }
//
//                override fun onError(utteranceId: String) {
//                    println("onUtteranceCompleted onError $utteranceId")
//                    if (utteranceId != TTSEngine.UTTERANCE_ID_DONE) {
//                        return
//                    }
//                    TTSEngine.get().stop()
//                    EventBus.getDefault().post(TtsStatus())
//                }
//
//                override fun onDone(utteranceId: String) {
//                    println("onUtteranceCompleted $utteranceId")
//                    if (utteranceId.startsWith(TTSEngine.STOP_SIGNAL)) {
//                        TTSEngine.get().stop()
//                        return
//                    }
//                    if (utteranceId.startsWith(TTSEngine.FINISHED_SIGNAL)) {
//                        if (TxtUtils.isNotEmpty(preText1)) {
//                            AppSP.get().lastBookParagraph = utteranceId.replace(TTSEngine.FINISHED_SIGNAL, "").toInt()
//                        } else {
//                            AppSP.get().lastBookParagraph = utteranceId.replace(TTSEngine.FINISHED_SIGNAL, "").toInt() + 1
//                        }
//                        return
//                    }
//                    if (utteranceId != TTSEngine.UTTERANCE_ID_DONE) {
//                        println("onUtteranceCompleted skip")
//                        return
//                    }
//                    if (System.currentTimeMillis() > TempHolder.get().timerFinishTime) {
//                        println("Update-timer-Stop1")
//                        stopSelf()
//                        return
//                    }
//                    AppSP.get().lastBookParagraph = 0
//                    playPage(secondPart, AppSP.get().lastBookPage + 1, null)
//                }
//            })
//            TTSEngine.get().speek(firstPart)
//            TTSNotification.show(AppSP.get().lastBookPath, pageNumber + 1, dc.getPageCount())
//            println("TtsStatus send")
//            EventBus.getDefault().post(TtsStatus())
//            TTSNotification.showLast()
//            Thread({
//                try {
//                    Thread.sleep(500)
//                } catch (_: InterruptedException) {
//                }
//                val load: AppBook = SharedBooks.load(AppSP.get().lastBookPath)
//                load.currentPageChanged(pageNumber + 1, AppSP.get().lastBookPageCount)
//                SharedBooks.save(load, false)
//                AppProfile.save(this)
//            }, "@T TTS Save").start()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        isStartForeground = false
//        unregisterReceiver(blueToothReceiver)
//        if (wakeLock!!.isHeld) {
//            wakeLock!!.release()
//        }
//        TTSEngine.get().stop()
//        TTSEngine.get().shutdown()
//        TTSNotification.hideNotification()
//        isActivated = false
//
//
//        //mAudioManager.abandonAudioFocus(listener);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mAudioManager!!.abandonAudioFocusRequest((audioFocusRequest as AudioFocusRequest?)!!)
//        } else {
//            mAudioManager!!.abandonAudioFocus(listener)
//        }
//
//        //mMediaSessionCompat.setCallback(null);
//        mMediaSessionCompat.setActive(false)
//        mMediaSessionCompat.release()
//        if (cache != null) {
//            cache.recycle()
//        }
//        path = null
//        println("onDestroy")
//    }
//
//    companion object {
//        const val EXTRA_PATH = "EXTRA_PATH"
//        const val EXTRA_ANCHOR = "EXTRA_ANCHOR"
//        const val EXTRA_INT = "INT"
//        private const val TAG = "TTSService"
//        var ACTION_PLAY_CURRENT_PAGE = "ACTION_PLAY_CURRENT_PAGE"
//        fun playLastBook() {
//            playBookPage(
//                AppSP.get().lastBookPage,
//                AppSP.get().lastBookPath,
//                "",
//                AppSP.get().lastBookWidth,
//                AppSP.get().lastBookHeight,
//                AppSP.get().lastFontSize,
//                AppSP.get().lastBookTitle
//            )
//        }
//
//        fun updateTimer() {
//            TempHolder.get().timerFinishTime = System.currentTimeMillis() + AppState.get().ttsTimer * 60 * 1000
//            println("Update-timer ${TempHolder.get().timerFinishTime} ${AppState.get().ttsTimer}")
//        }
//
//        fun playPause(context: Context?, controller: DocumentController?) {
//            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context!!, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale((context as Activity?)!!, Manifest.permission.POST_NOTIFICATIONS)) {
////                final Intent i = new Intent();
////                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
////                i.setData( Uri.fromParts("package", context.getPackageName(), null));
////                context.startActivity(i);
//                } else {
//                    ActivityCompat.requestPermissions((context as Activity?)!!, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 11)
//                    return
//                }
//            }
//            if (TTSEngine.get().isPlaying) {
//                val next = PendingIntent.getService(context, 0, Intent(TTSNotification.TTS_PAUSE, null, context, TTSService::class.java), PendingIntent.FLAG_IMMUTABLE)
//                try {
//                    next.send()
//                } catch (e: CanceledException) {
//                    println(e)
//                }
//            } else {
//                if (controller != null) {
//                    playBookPage(controller.getCurentPageFirst1() - 1, controller.getCurrentBook().getPath(), "", controller.getBookWidth(), controller.getBookHeight(), BookCSS.get().fontSizeSp, controller.getTitle())
//                }
//            }
//        }
//
//        @TargetApi(26)
//        fun playBookPage(
//            page: Int,
//            path: String,
//            anchor: String,
//            width: Int,
//            height: Int,
//            fontSize: Int,
//            title: String
//        ) {
//            println("playBookPage", page, path, width, height)
//            TTSEngine.get().stop()
//            AppSP.get().lastBookWidth = width
//            AppSP.get().lastBookHeight = height
//            AppSP.get().lastFontSize = fontSize
//            AppSP.get().lastBookTitle = title
//            AppSP.get().lastBookPage = page
//            val intent = playBookIntent(page, path, anchor)
//            if (Build.VERSION.SDK_INT >= 26) {
//                LibreraApp.context.startForegroundService(intent)
//            } else {
//                LibreraApp.context.startService(intent)
//            }
//        }
//
//        private fun playBookIntent(page: Int, path: String, anchor: String): Intent {
//            val intent = Intent(LibreraApp.context, TTSService::class.java)
//            intent.action = ACTION_PLAY_CURRENT_PAGE
//            intent.putExtra(EXTRA_INT, page)
//            intent.putExtra(EXTRA_PATH, path)
//            intent.putExtra(EXTRA_ANCHOR, anchor)
//            return intent
//        }
//    }
//}
