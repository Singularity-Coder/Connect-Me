package com.singularitycoder.connectme.helpers.di

import android.content.Context
import android.content.SharedPreferences
import com.singularitycoder.connectme.helpers.NetworkStatus
import com.singularitycoder.connectme.helpers.constants.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun injectPreferences(
        @ApplicationContext appContext: Context
    ): SharedPreferences = appContext.getSharedPreferences(Preferences.PREFERENCE_STORAGE_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun injectNetworkStatus(
        @ApplicationContext appContext: Context
    ): NetworkStatus = NetworkStatus(appContext)
}
