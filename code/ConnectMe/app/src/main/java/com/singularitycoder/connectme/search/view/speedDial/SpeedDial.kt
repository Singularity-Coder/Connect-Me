package com.singularitycoder.connectme.search.view.speedDial

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpeedDial(
    val favicon: String? = "",
    val title: String? = "",
    val time: Long? = 0L,
    val link: String = "",
    var isDateShown: Boolean = false
) : Parcelable