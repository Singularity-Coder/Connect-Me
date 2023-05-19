package com.singularitycoder.connectme.helpers

import android.util.TypedValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.InputStream

object ConnectMeUtils {
    val gson: Gson = GsonBuilder().setLenient().create()
    val typedValue = TypedValue()
    val webpageIdList = mutableListOf<String?>()
}