package com.singularitycoder.connectme.downloads

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize
import java.io.File

@Entity(tableName = Table.DOWNLOAD)
@Parcelize
data class Download(
    @PrimaryKey var id: Long = 0,
    var path: String? = "",
    var title: String = "",
    var time: Long? = 0L,
    var size: String? = "",
    var link: String? = "",
    var extension: String? = "",
    var isDirectory: Boolean = false
) : Parcelable
