package com.singularitycoder.connectme.history

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.NewTabType
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

// https://stackoverflow.com/questions/44213446/cannot-find-setter-for-field-using-kotlin-with-room-database
@Entity(tableName = Table.HISTORY)
@Parcelize
data class History(
    val favicon: String? = "",
    val title: String? = "",
    val time: Long? = 0L,
    val website: String? = "",
    @PrimaryKey val link: String = ""
) : Parcelable {

    @IgnoredOnParcel
    @Ignore
    var isDateShown: Boolean = false
}
