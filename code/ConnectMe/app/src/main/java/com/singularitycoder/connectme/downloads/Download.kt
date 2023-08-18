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
    val path: String? = "",
    val title: String = "",
    val time: Long? = 0L,
    val size: String? = "",
    val link: String? = "",
    val extension: String? = "",
    val isDirectory: Boolean = false
) : Parcelable
