package com.singularitycoder.connectme.search.view.speedDial

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpeedDial(
    val type: String? = null,
    val favicon: String? = null,
    val imageUrl: String? = null,
    val bitmap: Bitmap? = null,
    val title: String? = null,
    val time: Long? = 0L,
    val timeInString: String? = null,
    val link: String = "",
    val path: String = "",
    var isDateShown: Boolean = false
) : Parcelable