package com.singularitycoder.connectme.helpers

import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// https://stackoverflow.com/questions/4772425/change-date-format-in-a-java-string

val DATE_FORMAT_LIST = listOf(
    "dd-MMMM hh:mm",
    "dd-MM-yyyy",
    "dd/MM/yyyy",
    "dd-MMM-yyyy",
    "dd/MMM/yyyy",
    "dd-MMM-yyyy",
    "dd MMM yyyy",
    "dd-MMM-yyyy h:mm a",
    "dd MMM yyyy, hh:mm a",
    "dd MMM yyyy, hh:mm:ss a",
    "dd MMM yyyy, h:mm:ss aaa",
    "yyyy/MM/dd",
    "yyyy-MM-dd",
    "yyyy.MM.dd HH:mm",
    "yyyy/MM/dd hh:mm aa",
    "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
    "hh:mm a",

    // https://code.google.com/archive/p/android-rss/
    "EEE, dd MMM yyyy HH:mm:ss z", // RFC_822
    "EEE, dd MMM yyyy HH:mm zzzz",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz", // Blogger Atom feed has millisecs also
    "yyyy-MM-dd'T'HH:mm:sszzzz",
    "yyyy-MM-dd'T'HH:mm:ss z",
    "yyyy-MM-dd'T'HH:mm:ssz", // ISO_8601
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HHmmss.SSSz",
)

val GMT_DATE_FORMAT_LIST = listOf("")

fun Int.milliSeconds(): Long = this.toLong()

fun Int.seconds(): Long = TimeUnit.SECONDS.toMillis(this.toLong())

fun Int.minutes(): Long = TimeUnit.MINUTES.toMillis(this.toLong())

fun Int.hours(): Long = TimeUnit.HOURS.toMillis(this.toLong())

// Get Epoch Time
val timeNow: Long
    get() = System.currentTimeMillis()

/**
 * https://developer.android.com/reference/android/os/SystemClock
 * System.currentTimeMillis() -> current time in millis
 * uptimeMillis() -> is counted in milliseconds since the system was booted.
 * elapsedRealtime() and elapsedRealtimeNanos() -> return the time since the system was booted
 * */
val deviceStartTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()

fun Long.toIntuitiveDateTime(dateType: DateType = DateType.dd_MMM_yyyy_hh_mm_a): String {
    val postedTime = this
    val elapsedTimeMillis = timeNow - postedTime
    val elapsedTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis)
    val elapsedTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis)
    val elapsedTimeInHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
    val elapsedTimeInDays = TimeUnit.MILLISECONDS.toDays(elapsedTimeMillis)
    val elapsedTimeInMonths = elapsedTimeInDays / 30
    return when {
        elapsedTimeInSeconds < 60 -> "Now"
        elapsedTimeInMinutes == 1L -> "$elapsedTimeInMinutes min ago"
        elapsedTimeInMinutes < 60 -> "$elapsedTimeInMinutes min ago"
        elapsedTimeInHours == 1L -> "$elapsedTimeInHours hr ago"
        elapsedTimeInHours < 24 -> "$elapsedTimeInHours hr ago"
        elapsedTimeInDays == 1L -> "$elapsedTimeInDays day ago"
        elapsedTimeInDays < 30 -> "$elapsedTimeInDays days ago"
        elapsedTimeInMonths == 1L -> "$elapsedTimeInMonths mon ago"
        elapsedTimeInMonths < 12 -> "$elapsedTimeInMonths mon ago"
        else -> postedTime toTimeOfType dateType
    }
}

infix fun Long.toTimeOfType(type: DateType): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(type.value, Locale.getDefault())
    return dateFormat.format(date)
}

fun convertDateToLong(date: String, dateType: String): Long {
    if (date.isNullOrBlankOrNaOrNullString()) return convertDateToLong(date = Date().toString(), dateType)
    val dateFormat = SimpleDateFormat(dateType, Locale.getDefault())
    return try {
        if (dateFormat.parse(date) is Date) {
            dateFormat.parse(date).time
        } else convertDateToLong(date = Date().toString(), dateType)
    } catch (e: Exception) {
        0L
    }
}

fun String?.toFormattedHolidayDate(): String? {
    return when {
        this?.contains("to") == true -> {
            this.substringBefore(",").substringAfter("of ").trim()
        }
        this?.contains(",") == true -> {
            this.substringBefore(",").trim()
        }
        else -> {
            this?.substringAfter("of ")?.trim()
        }
    }
}

fun Long?.toShortDate(): String? {
    val timeNowString = (timeNow toTimeOfType DateType.dd_MMM_yyyy_hh_mm_a).substringBefore(",").trim()
    val calculatedTime = this?.toTimeOfType(DateType.dd_MMM_yyyy_hh_mm_a)?.substringBefore(",")?.trim()
    return if (calculatedTime == timeNowString) "Today" else calculatedTime
}

fun Long?.toShortTime(): String? = this?.toTimeOfType(DateType.h_mm_a)

enum class DateType(val value: String) {
    h_mm_a(value = "h:mm a"),
    dd_MMM_yyyy(value = "dd MMM yyyy"),
    MMM_d_yyyy(value = "MMM d, yyyy"),
    dd_MMM_yyyy_h_mm_a(value = "dd-MMM-yyyy h:mm a"),
    dd_MMM_yyyy_hh_mm_a(value = "dd MMM yyyy, hh:mm a"),
    dd_MMM_yyyy_hh_mm_ss_a(value = "dd MMM yyyy, hh:mm:ss a"),
    dd_MMM_yyyy_h_mm_ss_aaa(value = "dd MMM yyyy, h:mm:ss aaa"),
    yyyy_MM_dd_T_HH_mm_ss_SS_Z(value = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
}