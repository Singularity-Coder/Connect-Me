package com.singularitycoder.connectme.search.view.speedDial

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpeedDial(
    val favicon: String? = null,
    val imageUrl: String? = null,
    val title: String? = null,
    val time: Long? = 0L,
    val timeInString: String? = null,
    val link: String = "",
    var isDateShown: Boolean = false
) : Parcelable