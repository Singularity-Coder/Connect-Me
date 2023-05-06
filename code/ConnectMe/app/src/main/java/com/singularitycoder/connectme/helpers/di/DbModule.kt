package com.singularitycoder.connectme.helpers.di

import android.content.Context
import androidx.room.Room
import com.singularitycoder.connectme.helpers.constants.Db
import com.singularitycoder.connectme.helpers.db.ConnectMeDatabase
import com.singularitycoder.connectme.search.dao.InsightDao
import com.singularitycoder.connectme.search.dao.PromptDao
import com.singularitycoder.connectme.search.dao.WebAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectConnectMeDatabase(@ApplicationContext context: Context): ConnectMeDatabase {
        return Room.databaseBuilder(context, ConnectMeDatabase::class.java, Db.CONNECT_ME).build()
    }

    @Singleton
    @Provides
    fun injectWebAppDao(db: ConnectMeDatabase): WebAppDao = db.webAppDao()

    @Singleton
    @Provides
    fun injectInsightDao(db: ConnectMeDatabase): InsightDao = db.insightDao()

    @Singleton
    @Provides
    fun injectPromptDao(db: ConnectMeDatabase): PromptDao = db.promptDao()
}
