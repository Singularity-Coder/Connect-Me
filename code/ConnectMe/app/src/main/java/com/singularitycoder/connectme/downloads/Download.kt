package com.singularitycoder.connectme.downloads

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.DOWNLOAD)
@Parcelize
data class Download(
    @PrimaryKey val id: Long = 0,
    val imageUrl: String? = "",
    val title: String = "",
    val time: String? = "",
    val link: String? = "",
    val isDirectory: Boolean = false
) : Parcelable
