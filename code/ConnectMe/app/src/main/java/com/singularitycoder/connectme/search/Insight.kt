package com.singularitycoder.connectme.search

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class InsightObject {
    @Parcelize
    data class Insight(
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
    data class Error(
        var code: Int? = null,
        var status: String? = null,
        var message: String? = null
    ) : Parcelable
}