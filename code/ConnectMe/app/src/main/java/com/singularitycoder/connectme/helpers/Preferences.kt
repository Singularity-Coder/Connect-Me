package com.singularitycoder.connectme.helpers

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object Preferences {
    const val KEY_SEARCH_SUGGESTION_PROVIDER = "KEY_SEARCH_SUGGESTION_PROVIDER"
    const val KEY_ENABLE_VPN = "KEY_ENABLE_VPN"
    const val KEY_ENABLE_AD_BLOCKER = "KEY_ENABLE_AD_BLOCKER"

    private const val PREFERENCE_STORAGE_NAME = "connect_me_preferences"
    private fun Context.preferences(): SharedPreferences = getSharedPreferences(PREFERENCE_STORAGE_NAME, MODE_PRIVATE)
    fun write(context: Context): SharedPreferences.Editor = context.preferences().edit()
    fun read(context: Context): SharedPreferences = context.preferences()
}