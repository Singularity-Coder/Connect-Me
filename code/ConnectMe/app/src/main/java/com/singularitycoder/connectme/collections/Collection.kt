package com.singularitycoder.connectme.collections

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import com.singularitycoder.connectme.search.model.WebApp
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.COLLECTION)
@Parcelize
data class Collection(
    @PrimaryKey val id: Long = 0,
    val title: String = "",
    val websitesList: List<WebApp> = emptyList()
) : Parcelable

// Add item to coll
// Add to the webapp pool
// get prim key of each webapp
// store prim key in list of each coll
// Foreign key is the only way

