package com.singularitycoder.connectme.collections

import android.os.Parcelable
import com.singularitycoder.connectme.search.model.WebApp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Collection(
    val id: Long = 0,
    val title: String = "",
    val websitesList: List<WebApp> = emptyList()
) : Parcelable {
    constructor() : this(id = 0, title = "", websitesList = emptyList())
}

