package com.singularitycoder.connectme.collections

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.NewTabType
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

// TODO To support multiple same links in different collections u need to split the collections db. collectionTitle wil be list. Use one to many relationship to associate
// https://developer.android.com/training/data-storage/room/relationships
// https://stackoverflow.com/questions/47511750/how-to-use-foreign-key-in-room-persistence-library
// https://medium.com/android-news/android-architecture-components-room-relationships-bf473510c14a


@Entity(tableName = Table.COLLECTION)
@Parcelize
data class CollectionWebPage(
    var collectionTitle: String? = "",
    var title: String? = "",
    var favicon: String? = "",
    var time: Long? = 0L,
    @PrimaryKey var link: String = "",
    var tabType: String = NewTabType.NEW_TAB.value
) : Parcelable

data class LinksCollection(
    var title: String? = "",
    var count: Int? = 0,
    var linkList: List<CollectionWebPage?> = listOf()
)

