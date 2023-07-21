package com.singularitycoder.connectme.helpers.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.singularitycoder.connectme.collections.CollectionsDao
import com.singularitycoder.connectme.feed.FeedDao
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteDao
import com.singularitycoder.connectme.helpers.constants.DEFAULT_FOLLOWING_SITES
import com.singularitycoder.connectme.helpers.constants.DEFAULT_WEB_APPS
import com.singularitycoder.connectme.helpers.constants.Db
import com.singularitycoder.connectme.helpers.db.ConnectMeDatabase
import com.singularitycoder.connectme.history.HistoryDao
import com.singularitycoder.connectme.search.dao.InsightDao
import com.singularitycoder.connectme.search.dao.PromptDao
import com.singularitycoder.connectme.search.dao.WebAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectConnectMeDatabase(@ApplicationContext context: Context): ConnectMeDatabase {
        fun getRoomDb(): ConnectMeDatabase = Room.databaseBuilder(context, ConnectMeDatabase::class.java, Db.CONNECT_ME)
            .addCallback(object : RoomDatabase.Callback() {
                // prepopulate the database after onCreate was called - https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(IO).launch {
                        getRoomDb().followingWebsiteDao().insertAll(DEFAULT_FOLLOWING_SITES) // prepopulate following websites
                        getRoomDb().collectionsDao().insertAll(DEFAULT_WEB_APPS) // prepopulate default webapps
                    }
                }
            })
//            .createFromFile(File(""))
            .build()
        return getRoomDb()
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

    @Singleton
    @Provides
    fun injectHistoryDao(db: ConnectMeDatabase): HistoryDao = db.historyDao()

    @Singleton
    @Provides
    fun injectFollowingWebsiteDao(db: ConnectMeDatabase): FollowingWebsiteDao = db.followingWebsiteDao()

    @Singleton
    @Provides
    fun injectCollectionsDao(db: ConnectMeDatabase): CollectionsDao = db.collectionsDao()

    @Singleton
    @Provides
    fun injectFeedDao(db: ConnectMeDatabase): FeedDao = db.feedDao()
}
