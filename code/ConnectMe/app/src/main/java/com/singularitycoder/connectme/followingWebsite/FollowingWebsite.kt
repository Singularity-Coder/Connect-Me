package com.singularitycoder.connectme.followingWebsite

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.FOLLOWING_WEBSITE)
@Parcelize
data class FollowingWebsite(
    val favicon: String? = "",
    val title: String? = "",
    val time: Long? = 0L,
    @PrimaryKey val website: String = "",
    val link: String = "",
    val postCount: Long = 0L
) : Parcelable
