package com.singularitycoder.connectme.search.model

import android.os.Parcelable
import com.singularitycoder.connectme.helpers.constants.NewTabType
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchTab(
    val type: NewTabType = NewTabType.NEW_TAB,
    val link: String? = "",
    val title: String? = "",
) : Parcelable
