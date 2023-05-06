package com.singularitycoder.connectme.search.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import com.singularitycoder.connectme.search.model.Prompt
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prompt: Prompt?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<Prompt?>)


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(prompt: Prompt?)

    @Query("UPDATE ${Table.PROMPT} SET promptsJson = :promptsJson WHERE website LIKE :website")
    fun updateWithPromptsList(promptsJson: String?, website: String)

    @Transaction
    @Query("SELECT * FROM ${Table.PROMPT} WHERE website LIKE :website LIMIT 1")
    suspend fun getInsightByWebsite(website: String): Prompt?

    @Query("SELECT * FROM ${Table.PROMPT}")
    fun getAllLiveData(): LiveData<List<Prompt?>>

    @Query("SELECT * FROM ${Table.PROMPT}")
    fun getAllStateFlow(): Flow<List<Prompt?>>

    @Query("SELECT * FROM ${Table.PROMPT} WHERE website = :website")
    fun getAllByWebsiteStateFlow(website: String?): Flow<List<Prompt?>>

    @Query("SELECT * FROM ${Table.PROMPT} WHERE website = :website")
    fun getByWebsiteStateFlow(website: String?): Flow<Prompt?>

    @Query("SELECT * FROM ${Table.PROMPT}")
    suspend fun getAll(): List<Prompt?>


    @Delete
    suspend fun delete(prompt: Prompt?)

    @Query("DELETE FROM ${Table.PROMPT} WHERE website = :website")
    suspend fun deleteByWebsite(website: String?)

    @Query("DELETE FROM ${Table.PROMPT}")
    suspend fun deleteAll()
}
