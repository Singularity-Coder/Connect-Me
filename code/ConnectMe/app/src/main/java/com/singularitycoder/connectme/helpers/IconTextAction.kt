package com.singularitycoder.connectme.helpers

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class IconTextAction(
    val title: String = "",
    @DrawableRes val icon: Int,
    @DrawableRes val actionIcon: Int,
) : Parcelable
