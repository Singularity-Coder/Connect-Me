package com.singularitycoder.connectme.helpers.constants

// https://developer.android.com/guide/topics/media/platform/supported-formats

enum class ImageFormat(val value: String) {
    BMP(value = "bmp"),
    GIF(value = "gif"),
    JPG(value = "jpg"),
    JPEG(value = "jpeg"),
    PNG(value = "png"),
    WebP(value = "webp"),
    HEIF_HEIC(value = "heic"),
    HEIF_HEIF(value = "heif"),
}

enum class VideoFormat(val value: String) {
    _3GPP(value = "3gp"),
    MPEG_4_MP4(value = "mp4"),
    Matroska(value = "mkv"),
    MPEG_TS(value = "ts"),
    WebM(value = "webm"),
}

enum class AudioFormat(val value: String) {
    _3GPP(value = "3gp"),
    MPEG_4_MP4(value = "mp4"),
    MPEG_4_M4A(value = "m4a"),
    Matroska(value = "mkv"),
    MPEG_TS(value = "ts"),
    ADTS_AAC(value = "aac"),
    AMR(value = "amr"),
    FLAC(value = "flac"),
    MIDI_TYPE_0_1_MID(value = "mid"),
    MIDI_TYPE_0_1_XMF(value = "xmf"),
    MIDI_TYPE_0_1_MXMF(value = "mxmf"),
    RTTTL(value = "rtttl"),
    RTX(value = "rtx"),
    OTA(value = "ota"),
    iMelody(value = "imy"),
    MP3(value = "mp3"),
    Ogg(value = "ogg"),
    WAVE(value = "wav"),
}

enum class DocumentFormat(val value: String) {
    PDF(value = "pdf"),
    TXT(value = "txt"),
    WORD(value = "word"),
}

enum class ArchiveFormat(val value: String) {
    ZIP(value = "zip"),
    RAR(value = "rar"),
    TAR(value = "tar"),
    TAR_GZ(value = "tar.gz"),
    TGZ(value = "tgz"),
    GZ(value = "gz"),
    _7Z(value = "7z"),
}

enum class AndroidFormat(val value: String) {
    APK(value = "apk"),
}
