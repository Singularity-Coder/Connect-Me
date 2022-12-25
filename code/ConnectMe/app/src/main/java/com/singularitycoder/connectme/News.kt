package com.singularitycoder.connectme

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class News(
    val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val source: String? = "",
    val time: String? = "",
    val link: String? = ""
) : Parcelable {
    constructor() : this(0, "", "", "", "", "")
}
