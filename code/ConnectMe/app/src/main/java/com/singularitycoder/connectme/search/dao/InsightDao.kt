package com.singularitycoder.connectme.search.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import com.singularitycoder.connectme.search.model.Insight
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: Insight?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<Insight?>)


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(insight: Insight?)


    @Transaction
    @Query("SELECT * FROM ${Table.INSIGHT} WHERE website LIKE :website LIMIT 1")
    suspend fun getInsightByWebsite(website: String): Insight?

    @Query("SELECT * FROM ${Table.INSIGHT}")
    fun getAllLiveData(): LiveData<List<Insight?>>

    @Query("SELECT * FROM ${Table.INSIGHT}")
    fun getAllStateFlow(): Flow<List<Insight?>>

    @Query("SELECT * FROM ${Table.INSIGHT}")
    suspend fun getAll(): List<Insight?>


    @Delete
    suspend fun delete(insight: Insight?)

    @Query("DELETE FROM ${Table.INSIGHT} WHERE website = :website")
    suspend fun deleteByWebsite(website: String?)

    @Query("DELETE FROM ${Table.INSIGHT}")
    suspend fun deleteAll()
}
