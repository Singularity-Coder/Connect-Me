package com.singularitycoder.connectme.followingWebsite

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.FOLLOWING_WEBSITE)
@Parcelize
data class FollowingWebsite(
    var favicon: String? = "",
    var title: String? = "",
    var time: Long? = 0L,
    @PrimaryKey var website: String = "",
    var link: String = "",
    var postCount: Long = 0L,
    var rssUrl: String? = ""
) : Parcelable
