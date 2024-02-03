package com.singularitycoder.connectme.feed

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.FEED)
@Parcelize
data class Feed(
    var image: String? = "",
    @PrimaryKey var title: String = "",
    var time: String? = "",
    var website: String? = "",
    var link: String = "",
    var isSaved: Boolean = false
) : Parcelable
