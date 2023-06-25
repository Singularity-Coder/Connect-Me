package com.singularitycoder.connectme.helpers.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.connectme.collections.CollectionWebPage
import com.singularitycoder.connectme.collections.CollectionsDao
import com.singularitycoder.connectme.feed.Feed
import com.singularitycoder.connectme.feed.FeedDao
import com.singularitycoder.connectme.followingWebsite.FollowingWebsite
import com.singularitycoder.connectme.followingWebsite.FollowingWebsiteDao
import com.singularitycoder.connectme.history.History
import com.singularitycoder.connectme.history.HistoryDao
import com.singularitycoder.connectme.search.dao.InsightDao
import com.singularitycoder.connectme.search.dao.PromptDao
import com.singularitycoder.connectme.search.dao.WebAppDao
import com.singularitycoder.connectme.search.model.Insight
import com.singularitycoder.connectme.search.model.Prompt
import com.singularitycoder.connectme.search.model.WebApp

@Database(
    entities = [
        WebApp::class,
        Insight::class,
        Prompt::class,
        History::class,
        FollowingWebsite::class,
        CollectionWebPage::class,
        Feed::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    IntListConverter::class,
    WebAppListConverter::class
)
abstract class ConnectMeDatabase : RoomDatabase() {
    abstract fun webAppDao(): WebAppDao
    abstract fun insightDao(): InsightDao
    abstract fun promptDao(): PromptDao
    abstract fun historyDao(): HistoryDao
    abstract fun websiteFollowingDao(): FollowingWebsiteDao
    abstract fun collectionsDao(): CollectionsDao
    abstract fun feedDao(): FeedDao
}

