package com.singularitycoder.connectme.helpers.tts

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.EngineInfo
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast
import com.singularitycoder.connectme.R
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DecimalFormat
import java.util.Timer
import java.util.TimerTask

//class TTSEngine {
//
//    companion object {
//        const val FINISHED_SIGNAL = "Finished"
//        const val STOP_SIGNAL = "Stoped"
//        const val UTTERANCE_ID_DONE = "LirbiReader"
//        const val WAV = ".wav"
//        const val MP3 = ".mp3"
//        private const val TAG = "TTSEngine"
//
//        private val INSTANCE = TTSEngine()
//
//        fun get(): TTSEngine {
//            return INSTANCE
//        }
//
//        fun fastTTSBookmark(dc: DocumentController): AppBookmark? {
//            return fastTTSBookmark(dc.getActivity(), dc.getCurrentBook().getPath(), dc.getCurentPageFirst1(), dc.getPageCount())
//        }
//
//        fun fastTTSBookmark(c: Context, bookPath: String?, page: Int, pages: Int): AppBookmark? {
//            println("fastTTSBookmakr", page, pages)
//            if (pages == 0) {
//                println("fastTTSBookmakr skip")
//                return null
//            }
//            val hasBookmark: Boolean = BookmarksData.get().hasBookmark(bookPath, page, pages)
//            if (!hasBookmark) {
//                val bookmark = AppBookmark(bookPath, c.getString(R.string.fast_bookmark), MyMath.percent(page, pages))
//                BookmarksData.get().add(bookmark)
//                val TEXT = c.getString(R.string.fast_bookmark) + " " + TxtUtils.LONG_DASH1 + " " + c.getString(R.string.page) + " " + page + ""
//                Toast.makeText(c, TEXT, Toast.LENGTH_SHORT).show()
//                return bookmark
//            }
//            Vibro.vibrate()
//            return null
//        }
//
//        fun engineToString(info: EngineInfo): String {
//            return info.label
//        }
//    }
//
//    @Volatile
//    var ttsEngine: TextToSpeech? = null
//
//    @Volatile
//    var mP: MediaPlayer? = null
//    var mTimer: Timer? = null
//    var helpObject = Any()
//    var map = HashMap<String, String>()
//    var mapTemp = HashMap<String, String>()
//    var listener = OnInitListener { status ->
//        println("onInit - SUCCESS ${status == TextToSpeech.SUCCESS}")
//        if (status == TextToSpeech.ERROR) {
////            Toast.makeText(context, R.string.msg_unexpected_error, Toast.LENGTH_LONG).show()
//        }
//    }
//    private var text = ""
//
//    init {
//        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = UTTERANCE_ID_DONE
//        mapTemp[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "Temp"
//
//    }
//
//    fun shutdown() {
//        println("shutdown")
//        synchronized(helpObject) {
//            if (ttsEngine != null) {
//                ttsEngine!!.shutdown()
//            }
//            ttsEngine = null
//        }
//    }
//
//    val tTS: TextToSpeech?
//        get() = getTTS(null)
//
//    fun getTTS(onLisnter: OnInitListener?): TextToSpeech? {
//        var onLisnter = onLisnter
//        if (LibreraApp.context == null) {
//            return null
//        }
//        synchronized(helpObject) {
//            if (get().isMp3 && mP == null) {
//                get().loadMP3(BookCSS.get().mp3BookPathGet())
//            }
//            if (ttsEngine != null) {
//                return ttsEngine
//            }
//            if (onLisnter == null) {
//                onLisnter = listener
//            }
//            ttsEngine = TextToSpeech(LibreraApp.context, onLisnter)
//        }
//        return ttsEngine
//    }
//
//    @get:Synchronized
//    val isShutdown: Boolean
//        get() = ttsEngine == null
//
//    fun stop() {
//        println("stop")
//        synchronized(helpObject) {
//            if (ttsEngine != null) {
//                ttsEngine!!.setOnUtteranceProgressListener(null)
//                ttsEngine!!.stop()
//                EventBus.getDefault().post(TtsStatus())
//            }
//        }
//    }
//
//    fun stopDestroy() {
//        println("stop")
//        TxtUtils.dictHash = ""
//        synchronized(helpObject) {
//            if (ttsEngine != null) {
//                ttsEngine!!.shutdown()
//            }
//            ttsEngine = null
//        }
//        AppSP.get().lastBookParagraph = 0
//    }
//
//    fun setTTSWithEngine(engine: String?): TextToSpeech? {
//        shutdown()
//        synchronized(helpObject) { ttsEngine = TextToSpeech(LibreraApp.context, listener, engine) }
//        return ttsEngine
//    }
//
//    @Synchronized
//    fun speek(text: String) {
//        this.text = text
//        if (AppSP.get().tempBookPage !== AppSP.get().lastBookPage) {
//            AppSP.get().tempBookPage = AppSP.get().lastBookPage
//            AppSP.get().lastBookParagraph = 0
//        }
//        println("speek", AppSP.get().lastBookPage, "par", AppSP.get().lastBookParagraph)
//        if (TxtUtils.isEmpty(text)) {
//            return
//        }
//        if (ttsEngine == null) {
//            println("getTTS-status was null")
//        } else {
//            println("getTTS-status not null")
//        }
//        ttsEngine = getTTS { status ->
//            println("getTTS-status", status)
//            if (status == TextToSpeech.SUCCESS) {
//                try {
//                    Thread.sleep(1000)
//                } catch (e: InterruptedException) {
//                }
//                speek(text)
//            }
//        }
//        ttsEngine!!.setPitch(AppState.get().ttsPitch)
//        if (AppState.get().ttsSpeed === 0.0f) {
//            AppState.get().ttsSpeed = 0.01f
//        }
//        ttsEngine!!.setSpeechRate(AppState.get().ttsSpeed)
//        println("Speek s", AppState.get().ttsSpeed)
//        println("Speek AppSP.get().lastBookParagraph", AppSP.get().lastBookParagraph)
//        if (AppState.get().ttsPauseDuration > 0 && text.contains(TxtUtils.TTS_PAUSE)) {
//            val parts: Array<String> = text.split(TxtUtils.TTS_PAUSE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//            ttsEngine!!.playSilence(0L, TextToSpeech.QUEUE_FLUSH, mapTemp)
//            for (i in AppSP.get().lastBookParagraph until parts.size) {
//                var big = parts[i]
//                big = big.trim { it <= ' ' }
//                if (TxtUtils.isNotEmpty(big)) {
//                    if (big.length == 1 && !Character.isLetterOrDigit(big[0])) {
//                        println("Skip: $big")
//                        continue
//                    }
//                    if (big.contains(TxtUtils.TTS_SKIP)) {
//                        continue
//                    }
//                    if (big.contains(TxtUtils.TTS_STOP)) {
//                        val mapStop = HashMap<String, String>()
//                        mapStop[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = STOP_SIGNAL
//                        ttsEngine!!.playSilence(AppState.get().ttsPauseDuration, TextToSpeech.QUEUE_ADD, mapStop)
//                        println("Add stop signal")
//                    }
//                    if (big.contains(TxtUtils.TTS_NEXT)) {
//                        ttsEngine!!.playSilence(0L, TextToSpeech.QUEUE_ADD, map)
//                        println("next-page signal")
//                        break
//                    }
//                    val mapTemp1 = HashMap<String, String>()
//                    mapTemp1[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = FINISHED_SIGNAL + i
//                    ttsEngine!!.speak(big, TextToSpeech.QUEUE_ADD, mapTemp1)
//                    ttsEngine!!.playSilence(AppState.get().ttsPauseDuration, TextToSpeech.QUEUE_ADD, mapTemp)
//                    println("pageHTML-parts", i, big)
//                }
//            }
//            ttsEngine!!.playSilence(0L, TextToSpeech.QUEUE_ADD, map)
//        } else {
//            val textToPlay: String = text.replace(TxtUtils.TTS_PAUSE, "")
//            println("pageHTML-parts-single", text)
//            ttsEngine!!.speak(textToPlay, TextToSpeech.QUEUE_FLUSH, map)
//        }
//    }
//
//    fun speakToFile(controller: DocumentController, info: ResultResponse<String?>, from: Int, to: Int) {
//        val dirFolder: File = File(BookCSS.get().ttsSpeakPath, "TTS_" + controller.getCurrentBook().getName())
//        if (!dirFolder.exists()) {
//            dirFolder.mkdirs()
//        }
//        if (!dirFolder.exists()) {
//            info.onResultRecive(controller.getActivity().getString(R.string.file_not_found) + " " + dirFolder.path)
//            return
//        }
//        val path = dirFolder.path
//        speakToFile(controller, from - 1, path, info, from - 1, to)
//    }
//
//    fun speakToFile(controller: DocumentController?, page: Int, folder: String?, info: ResultResponse<String?>, from: Int, to: Int) {
//        println("speakToFile", page, controller.getPageCount())
//        if (ttsEngine == null) {
//            println("TTS is null")
//            if (controller != null && controller.getActivity() != null) {
//                Toast.makeText(controller.getActivity(), R.string.msg_unexpected_error, Toast.LENGTH_SHORT).show()
//            }
//            return
//        }
//        ttsEngine!!.setPitch(AppState.get().ttsPitch)
//        ttsEngine!!.setSpeechRate(AppState.get().ttsSpeed)
//        if (page >= to || !TempHolder.isRecordTTS) {
//            println("speakToFile finish", page, controller.getPageCount())
//            info.onResultRecive(controller.getActivity().getString(R.string.success))
//            TempHolder.isRecordTTS = false
//            return
//        }
//        info.onResultRecive((page + 1).toString() + " / " + to)
//        val df = DecimalFormat("0000")
//        val pageName = "page-" + df.format((page + 1).toLong())
//        val wav = File(folder, pageName + WAV).path
//        var fileText: String = controller.getTextForPage(page)
//        controller.recyclePage(page)
//        println("synthesizeToFile", fileText)
//        if (TxtUtils.isEmpty(fileText)) {
//            speakToFile(controller, page + 1, folder, info, from, to)
//        } else {
//            if (fileText.length > 3950) {
//                fileText = TxtUtils.substringSmart(fileText, 3950) + " " + controller.getString(R.string.text_is_too_long)
//                println("Text-too-long", page)
//            }
//            ttsEngine!!.synthesizeToFile(fileText, map, wav)
//            get().tTS!!.setOnUtteranceCompletedListener {
//                println("speakToFile onUtteranceCompleted", page, controller.getPageCount())
//                if (AppState.get().isConvertToMp3) {
//                    try {
//                        val file = File(wav)
//                        val lame = Lame()
//                        var input: InputStream = BufferedInputStream(FileInputStream(file))
//                        input.mark(44)
//                        val bitrate: Int = MobiParserIS.asInt_LITTLE_ENDIAN(input, 24, 4)
//                        println("bitrate", bitrate)
//                        input.close()
//                        input = FileInputStream(file)
//                        val bytes: ByteArray = IOUtils.toByteArray(input)
//                        val shorts = ShortArray(bytes.size / 2)
//                        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
//                        lame.open(1, bitrate, 128, 4)
//                        val res: ByteArray = lame.encode(shorts, 44, shorts.size)
//                        lame.close()
//                        val toFile = File(wav.replace(".wav", ".mp3"))
//                        toFile.delete()
//                        IO.copyFile(ByteArrayInputStream(res), toFile)
//                        input.close()
//                        file.delete()
//                    } catch (_: Exception) {
//
//                    }
//                }
//                //lame.encode();
//                speakToFile(
//                    controller = controller,
//                    page = page + 1,
//                    folder = folder,
//                    info = info,
//                    from = from,
//                    to = to
//                )
//            }
//        }
//    }
//
//    val isTempPausing: Boolean
//        get() = if (AppState.get().isEnableAccessibility) {
//            true
//        } else mP != null || ttsEngine != null
//    val isPlaying: Boolean
//        get() {
//            if (TempHolder.isRecordTTS) {
//                return false
//            }
//            if (isMp3) {
//                return mP != null && mP!!.isPlaying
//            }
//            synchronized(helpObject) {
//                return if (ttsEngine == null) {
//                    false
//                } else ttsEngine != null && ttsEngine!!.isSpeaking
//            }
//        }
//
//    fun hasNoEngines(): Boolean {
//        return try {
//            ttsEngine != null && (ttsEngine!!.engines == null || ttsEngine!!.engines.size == 0)
//        } catch (_: Exception) {
//            true
//        }
//    }
//
//    val currentLang: String
//        get() {
//            try {
//                if (ttsEngine != null && ttsEngine!!.defaultVoice != null && ttsEngine!!.defaultVoice.locale != null) {
//                    return ttsEngine!!.defaultVoice.locale.displayLanguage
//                }
//            } catch (_: Exception) {
//
//            }
//            return "---"
//        }
//    val engineCount: Int
//        get() {
//            try {
//                return if (ttsEngine == null || ttsEngine!!.engines == null) {
//                    -1
//                } else ttsEngine!!.engines.size
//            } catch (_: Exception) {
//
//            }
//            return 0
//        }
//    val currentEngineName: String
//        get() {
//            try {
//                if (ttsEngine != null) {
//                    val enginePackage = ttsEngine!!.defaultEngine
//                    val engines = ttsEngine!!.engines
//                    for (eInfo in engines) {
//                        if (eInfo.name == enginePackage) {
//                            return engineToString(eInfo)
//                        }
//                    }
//                }
//            } catch (_: Exception) {
//
//            }
//            return "---"
//        }
//
//    @JvmOverloads
//    fun loadMP3(ttsPlayMp3Path: String?, play: Boolean = false) {
//        println("loadMP3-", ttsPlayMp3Path)
//        if (TxtUtils.isEmpty(ttsPlayMp3Path) || !File(ttsPlayMp3Path).isFile) {
//            println("loadMP3-skip mp3")
//            return
//        }
//        try {
//            mp3Destroy()
//            mP = MediaPlayer()
//            mP!!.setDataSource(ttsPlayMp3Path)
//            mP!!.prepare()
//            mP!!.setOnCompletionListener { mp -> mp.pause() }
//            if (play) {
//                mP!!.start()
//            }
//            mTimer = Timer()
//            mTimer!!.schedule(object : TimerTask() {
//                override fun run() {
//                    AppState.get().mp3seek = mP!!.currentPosition
//                    //println("Run timer-task");
//                    EventBus.getDefault().post(TtsStatus())
//                }
//            }, 1000, 1000)
//        } catch (_: Exception) {
//
//        }
//    }
//
//    fun mp3Destroy() {
//        if (mP != null) {
//            mP!!.stop()
//            mP!!.reset()
//            mP = null
//            if (mTimer != null) {
//                mTimer!!.purge()
//                mTimer!!.cancel()
//                mTimer = null
//            }
//        }
//        println("mp3Desproy")
//    }
//
//    fun mp3Next() {
//        val seek = mP!!.currentPosition
//        mP!!.seekTo(seek + 5 * 1000)
//    }
//
//    fun mp3Prev() {
//        val seek = mP!!.currentPosition
//        mP!!.seekTo(seek - 5 * 1000)
//    }
//
//    val isMp3PlayPause: Boolean
//        get() {
//            if (isMp3) {
//                if (mP == null) {
//                    loadMP3(BookCSS.get().mp3BookPathGet())
//                }
//                if (mP == null) {
//                    return false
//                }
//                if (mP!!.isPlaying) {
//                    mP!!.pause()
//                } else {
//                    mP!!.start()
//                }
//                TTSNotification.showLast()
//                return true
//            }
//            return false
//        }
//
//    fun playMp3() {
//        if (mP != null) {
//            mP!!.start()
//        }
//    }
//
//    fun pauseMp3() {
//        if (mP != null) {
//            mP!!.pause()
//        }
//    }
//
//    val isMp3: Boolean
//        get() = TxtUtils.isNotEmpty(BookCSS.get().mp3BookPathGet())
//
//    fun seekTo(i: Int) {
//        if (mP != null) {
//            mP!!.seekTo(i)
//        }
//    }
//}
