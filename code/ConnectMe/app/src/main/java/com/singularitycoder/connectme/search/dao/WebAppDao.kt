package com.singularitycoder.connectme.search.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import com.singularitycoder.connectme.search.model.WebApp
import kotlinx.coroutines.flow.Flow

@Dao
interface WebAppDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: WebApp)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<WebApp>)


    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(app: WebApp)


//    @Transaction
//    @Query("SELECT * FROM ${Table.WEB_APP} WHERE packageName LIKE :packageName LIMIT 1")
//    suspend fun getAppByPackage(packageName: String): WebApp

    @Query("SELECT * FROM ${Table.WEB_APP}")
    fun getAllLiveData(): LiveData<List<WebApp>>

    @Query("SELECT * FROM ${Table.WEB_APP}")
    fun getAllStateFlow(): Flow<List<WebApp>>

    @Query("SELECT * FROM ${Table.WEB_APP}")
    suspend fun getAll(): List<WebApp>


    @Transaction
    @Delete
    suspend fun delete(app: WebApp)

//    @Transaction
//    @Query("DELETE FROM ${Table.WEB_APP} WHERE packageName = :packageName")
//    suspend fun deleteByPackageName(packageName: String?)

    @Transaction
    @Query("DELETE FROM ${Table.WEB_APP}")
    suspend fun deleteAll()
}
