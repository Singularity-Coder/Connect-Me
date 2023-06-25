package com.singularitycoder.connectme.feed

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.FEED)
@Parcelize
data class Feed(
    val image: String? = "",
    @PrimaryKey val title: String = "",
    val time: String? = "",
    val website: String? = "",
    val link: String = ""
) : Parcelable
