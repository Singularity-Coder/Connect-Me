package com.singularitycoder.connectme.downloads

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<Download?>)


    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(download: Download?)

//    @Query("UPDATE ${Table.DOWNLOAD} SET link = :link WHERE website LIKE :website")
//    fun updateWithPromptsList(link: String?, website: String)


    @Transaction
    @Query("SELECT * FROM ${Table.DOWNLOAD} WHERE link LIKE :link LIMIT 1")
    suspend fun getItemByLink(link: String?): Download?

//    @Transaction
//    @Query("SELECT * FROM ${Table.DOWNLOAD} WHERE website LIKE :website LIMIT 1")
//    suspend fun getItemByWebsite(website: String?): Download?

    @Query("SELECT * FROM ${Table.DOWNLOAD}")
    fun getAllItemsLiveData(): LiveData<List<Download?>>

    @Query("SELECT * FROM ${Table.DOWNLOAD}")
    fun getAllItemsStateFlow(): Flow<List<Download?>>

    @Query("SELECT * FROM ${Table.DOWNLOAD} ORDER BY time DESC LIMIT 3")
    suspend fun getLast3By(): List<Download?>

//    @Query("SELECT * FROM ${Table.DOWNLOAD} WHERE website = :website")
//    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<Download?>>

//    @Query("SELECT * FROM ${Table.DOWNLOAD} WHERE website = :website")
//    fun getItemByWebsiteStateFlow(website: String?): Flow<Download?>

    @Query("SELECT * FROM ${Table.DOWNLOAD}")
    suspend fun getAll(): List<Download?>


    @Transaction
    @Delete
    suspend fun delete(download: Download?)

//    @Transaction
//    @Query("DELETE FROM ${Table.DOWNLOAD} WHERE website = :website")
//    suspend fun deleteByWebsite(website: String?)

    @Transaction
    @Query("DELETE FROM ${Table.DOWNLOAD} WHERE time >= :time")
    suspend fun deleteAllByTime(time: Long?)

    @Transaction
    @Query("DELETE FROM ${Table.DOWNLOAD}")
    suspend fun deleteAll()
}
