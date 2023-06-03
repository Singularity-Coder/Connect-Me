package com.singularitycoder.connectme.search.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

// https://www.sqlite.org/limits.html
// The maximum number of bytes in the text of an SQL statement is limited to SQLITE_MAX_SQL_LENGTH which defaults to 1,000,000,000. 1 billion.

@Entity(tableName = Table.WEB_APP)
@Parcelize
data class WebApp(
    val favicon: String? = "",
    val title: String? = "",
    val time: Long? = 0L,
    val website: String? = "",
    @PrimaryKey val link: String = "",
    val tabTag: String? = "",
) : Parcelable
