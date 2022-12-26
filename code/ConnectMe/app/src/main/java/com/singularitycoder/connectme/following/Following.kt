package com.singularitycoder.connectme.following

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Following(
    val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val source: String? = "",
    val time: String? = "",
    val link: String? = "",
    val posts: Long = 0L
) : Parcelable {
    constructor() : this(0, "", "", "", "", "", 0L)
}
