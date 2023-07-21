package com.singularitycoder.connectme.helpers

import android.util.TypedValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object ConnectMeUtils {
    val gson: Gson = GsonBuilder().setLenient().create()
    val typedValue = TypedValue()
    val webpageFragmentIdList = mutableListOf<String?>()
}