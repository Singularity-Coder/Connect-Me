package com.singularitycoder.connectme.search.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import com.singularitycoder.connectme.search.model.Insight
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: Insight?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<Insight?>)

    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(insight: Insight?)

    @Transaction
    @Query("SELECT * FROM ${Table.INSIGHT} WHERE website LIKE :website LIMIT 1")
    suspend fun getInsightByWebsite(website: String?): Insight?

    @Query("SELECT * FROM ${Table.INSIGHT} WHERE website LIKE :website")
    fun getInsightByWebsiteStateFlow(website: String?): Flow<Insight?>

    @Query("SELECT * FROM ${Table.INSIGHT}")
    fun getAllLiveData(): LiveData<List<Insight?>>

    @Query("SELECT * FROM ${Table.INSIGHT}")
    fun getAllStateFlow(): Flow<List<Insight?>>

    @Query("SELECT * FROM ${Table.INSIGHT} WHERE website = :website")
    fun getAllByWebsiteStateFlow(website: String?): Flow<List<Insight?>>

    @Query("SELECT * FROM ${Table.INSIGHT}")
    suspend fun getAll(): List<Insight?>

    @Transaction
    @Query("SELECT insight FROM ${Table.INSIGHT} WHERE website LIKE :website")
    suspend fun getAllInsights(website: String?): List<String?>


    @Transaction
    @Delete
    suspend fun delete(insight: Insight?)

    @Transaction
    @Query("DELETE FROM ${Table.INSIGHT} WHERE website = :website")
    suspend fun deleteByWebsite(website: String?)

    @Transaction
    @Query("DELETE FROM ${Table.INSIGHT}")
    suspend fun deleteAll()
}
