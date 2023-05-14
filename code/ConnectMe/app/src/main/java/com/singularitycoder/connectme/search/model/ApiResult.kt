package com.singularitycoder.connectme.search.model

data class ApiResult(
    val insight: Insight? = null,
    val error: InsightObject.ErrorObject? = null,
    val apiState: ApiState = ApiState.NONE,
    val insightType: InsightType = InsightType.NONE,
    val screen: String? = null
)

enum class ApiState {
    NONE, LOADING, SUCCESS, ERROR
}

enum class InsightType {
    NONE, TEXT, IMAGE
}