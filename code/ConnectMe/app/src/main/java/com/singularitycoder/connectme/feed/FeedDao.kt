package com.singularitycoder.connectme.feed

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: Feed?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<Feed?>)


    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(feed: Feed?)

    @Query("UPDATE ${Table.FEED} SET link = :link WHERE website LIKE :website")
    fun updateLinkWithWebsite(link: String?, website: String)


    @Transaction
    @Query("SELECT * FROM ${Table.FEED} WHERE website LIKE :website LIMIT 1")
    suspend fun getItemByWebsite(website: String?): Feed?

    @Query("SELECT * FROM ${Table.FEED}")
    fun getAllItemsLiveData(): LiveData<List<Feed?>>

    @Query("SELECT * FROM ${Table.FEED}")
    fun getAllItemsStateFlow(): Flow<List<Feed?>>

    @Query("SELECT * FROM ${Table.FEED} WHERE website = :website")
    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<Feed?>>

    @Query("SELECT * FROM ${Table.FEED} WHERE isSaved = 1")
    fun getAllSavedItemsStateFlow(): Flow<List<Feed?>>

    @Query("SELECT * FROM ${Table.FEED} WHERE website = :website")
    fun getItemByWebsiteStateFlow(website: String?): Flow<Feed?>

    @Query("SELECT * FROM ${Table.FEED}")
    suspend fun getAll(): List<Feed?>


    @Transaction
    @Delete
    suspend fun delete(feed: Feed?)

    @Transaction
    @Query("DELETE FROM ${Table.FEED} WHERE website = :website")
    suspend fun deleteByWebsite(website: String?)

    @Transaction
    @Query("DELETE FROM ${Table.FEED} WHERE time >= :time")
    suspend fun deleteAllByTime(time: Long?)

    @Transaction
    @Query("DELETE FROM ${Table.FEED}")
    suspend fun deleteAll()
}
