package com.singularitycoder.connectme.downloads

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Download(
    val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val time: String? = "",
    val link: String? = "",
    val isDirectory: Boolean = false
) : Parcelable
