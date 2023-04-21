package com.singularitycoder.connectme.search

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.WEB_APP)
@Parcelize
data class WebApp(
    @PrimaryKey val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val source: String? = "",
    val time: String? = "",
    val link: String? = "",
    val tabTag: String? = "",
) : Parcelable
