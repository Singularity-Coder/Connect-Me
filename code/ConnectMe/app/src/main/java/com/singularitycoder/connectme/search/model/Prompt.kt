package com.singularitycoder.connectme.search.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.PROMPT)
@Parcelize
data class Prompt(
    @PrimaryKey val website: String = "",
    val promptsJson: String? = null,
) : Parcelable
