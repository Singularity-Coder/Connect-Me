package com.singularitycoder.connectme.helpers.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.connectme.search.dao.InsightDao
import com.singularitycoder.connectme.search.dao.PromptDao
import com.singularitycoder.connectme.search.model.WebApp
import com.singularitycoder.connectme.search.dao.WebAppDao
import com.singularitycoder.connectme.search.model.Insight
import com.singularitycoder.connectme.search.model.Prompt

@Database(
    entities = [
        WebApp::class,
        Insight::class,
        Prompt::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    IntListConverter::class
)
abstract class ConnectMeDatabase : RoomDatabase() {
    abstract fun webAppDao(): WebAppDao
    abstract fun insightDao(): InsightDao
    abstract fun promptDao(): PromptDao
}

