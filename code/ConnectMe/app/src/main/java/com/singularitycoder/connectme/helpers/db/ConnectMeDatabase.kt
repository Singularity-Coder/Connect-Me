package com.singularitycoder.connectme.helpers.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularitycoder.connectme.search.WebApp
import com.singularitycoder.connectme.search.WebAppDao

@Database(
    entities = [
        WebApp::class,
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
}
