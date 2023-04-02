package com.singularitycoder.connectme.explore

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class ExploreItemColor(
    @ColorRes val textColor: Int = 0,
    @ColorRes val iconColor: Int = 0,
    @DrawableRes val gradientColor: Int = 0
)