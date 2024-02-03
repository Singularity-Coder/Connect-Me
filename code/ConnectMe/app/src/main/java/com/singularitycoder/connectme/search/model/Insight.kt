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
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var website: String = "",
    var created: Long? = null,
    var userType: Int = 1,
    var insightType: Int = 1,
    var insight: String? = "",
    var imageList: List<String> = emptyList()
) : Parcelable

class InsightObject {
    @Parcelize
    data class Root(
        var id: String? = null,
        @SerializedName("object") var objectField: String? = null,
        var created: Long? = null,
        var choices: List<Choice> = emptyList(),
        var usage: Usage? = null,
    ) : Parcelable

    @Parcelize
    data class Choice(
        var index: Long? = null,
        var message: Message? = null,
        @SerializedName("finish_reason") var finishReason: String? = null,
    ) : Parcelable

    @Parcelize
    data class Message(
        var role: String? = null,
        var content: String? = null
    ) : Parcelable

    @Parcelize
    data class Usage(
        @SerializedName("prompt_tokens") var promptTokens: Long? = null,
        @SerializedName("completion_tokens") var completionTokens: Long? = null,
        @SerializedName("total_tokens") var totalTokens: Long? = null,
    ) : Parcelable

    @Parcelize
    data class ErrorObject(
        var error: Error? = null
    ) : Parcelable

    @Parcelize
    data class Error(
        var message: String? = null,
        var type: String? = null,
        var param: String? = null,
        var code: String? = null,
    ) : Parcelable
}

class ImageInsightObject {
    @Parcelize
    data class Root(
        var created: Long,
        var data: List<Image>,
    ) : Parcelable

    @Parcelize
    data class Image(
        var url: String,
    ) : Parcelable
}