package com.singularitycoder.connectme.helpers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class GenericWebsite(
    val image: String? = "",
    val title: String = "",
    val time: String? = "",
    val website: String? = "",
    val link: String = "",
) : Parcelable
