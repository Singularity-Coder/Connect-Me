package com.singularitycoder.connectme.history

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: History?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<History?>)


    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(history: History?)

    @Query("UPDATE ${Table.HISTORY} SET link = :link WHERE website LIKE :website")
    fun updateWithPromptsList(link: String?, website: String)


    @Transaction
    @Query("SELECT * FROM ${Table.HISTORY} WHERE website LIKE :website LIMIT 1")
    suspend fun getItemByWebsite(website: String?): History?

    @Query("SELECT * FROM ${Table.HISTORY}")
    fun getAllItemsLiveData(): LiveData<List<History?>>

    @Query("SELECT * FROM ${Table.HISTORY}")
    fun getAllItemsStateFlow(): Flow<List<History?>>

    @Query("SELECT * FROM ${Table.HISTORY} WHERE website = :website")
    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<History?>>

    @Query("SELECT * FROM ${Table.HISTORY} WHERE website = :website")
    fun getItemByWebsiteStateFlow(website: String?): Flow<History?>

    @Query("SELECT * FROM ${Table.HISTORY}")
    suspend fun getAll(): List<History?>


    @Transaction
    @Delete
    suspend fun delete(history: History?)

    @Transaction
    @Query("DELETE FROM ${Table.HISTORY} WHERE website = :website")
    suspend fun deleteByWebsite(website: String?)

    @Transaction
    @Query("DELETE FROM ${Table.HISTORY}")
    suspend fun deleteAll()
}