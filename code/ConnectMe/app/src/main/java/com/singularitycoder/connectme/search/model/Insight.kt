package com.singularitycoder.connectme.search.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.parcelize.Parcelize

@Entity(tableName = Table.INSIGHT)
@Parcelize
data class Insight(
    @PrimaryKey val website: String = "",
    val created: Long? = null,
    val userType: Int = 1,
    val insightType: Int = 1,
    val insight: String? = "",
    val imageList: List<String> = emptyList(),
) : Parcelable

class InsightObject {
    @Parcelize
    data class Root(
        val id: String? = null,
        @SerializedName("object") val objectField: String? = null,
        val created: Long? = null,
        val choices: List<Choice> = emptyList(),
        val usage: Usage? = null,
    ) : Parcelable

    @Parcelize
    data class Choice(
        val index: Long? = null,
        val message: Message? = null,
        @SerializedName("finish_reason") val finishReason: String? = null,
    ) : Parcelable

    @Parcelize
    data class Message(
        val role: String? = null,
        val content: String? = null
    ) : Parcelable

    @Parcelize
    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Long? = null,
        @SerializedName("completion_tokens") val completionTokens: Long? = null,
        @SerializedName("total_tokens") val totalTokens: Long? = null,
    ) : Parcelable

    @Parcelize
    data class ErrorObject(
        val error: Error? = null
    ) : Parcelable

    @Parcelize
    data class Error(
        val message: String? = null,
        val type: String? = null,
        val param: String? = null,
        val code: String? = null,
    ) : Parcelable
}

class ImageInsightObject {
    @Parcelize
    data class Root(
        val created: Long,
        val data: List<Image>,
    ) : Parcelable

    @Parcelize
    data class Image(
        val url: String,
    ) : Parcelable
}