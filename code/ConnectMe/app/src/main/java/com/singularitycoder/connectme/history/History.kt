package com.singularitycoder.connectme.history

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.HISTORY)
@Parcelize
data class History(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val favicon: String? = "",
    val title: String? = "",
    val time: Long? = 0L,
    val website: String? = "",
    val link: String? = ""
) : Parcelable
