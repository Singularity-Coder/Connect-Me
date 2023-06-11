package com.singularitycoder.connectme.collections

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

// TODO To support multiple same links in different collections u need to split the collections db. collectionTitle wil be list. Use one to many relationship to associate
// https://developer.android.com/training/data-storage/room/relationships
// https://stackoverflow.com/questions/47511750/how-to-use-foreign-key-in-room-persistence-library
// https://medium.com/android-news/android-architecture-components-room-relationships-bf473510c14a


@Entity(tableName = Table.COLLECTION)
@Parcelize
data class CollectionWebPage(
    val collectionTitle: String? = "",
    val title: String? = "",
    val favicon: String? = "",
    val time: Long? = 0L,
    @PrimaryKey val link: String = "",
) : Parcelable

data class LinksCollection(
    val title: String? = "",
    val count: Int? = 0,
    val linkList: List<CollectionWebPage?> = listOf()
)

