package com.singularitycoder.connectme.collections

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionsDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collectionWebPage: CollectionWebPage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CollectionWebPage>)

    @Query("SELECT Count(*) FROM ${Table.COLLECTION}") // Add WHERE if necessary
    suspend fun getAllItemsCount(): Int

    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(collectionWebPage: CollectionWebPage)

    @Query("UPDATE ${Table.COLLECTION} SET collectionTitle = :newCollectionTitle WHERE collectionTitle LIKE :oldCollectionTitle")
    suspend fun renameCollection(newCollectionTitle: String?, oldCollectionTitle: String?)


//    @Transaction
//    @Query("SELECT * FROM ${Table.COLLECTION} WHERE website LIKE :website LIMIT 1")
//    suspend fun getItemByWebsite(website: String?): CollectionWebPage

    @Query("SELECT * FROM ${Table.COLLECTION}")
    fun getAllItemsLiveData(): LiveData<List<CollectionWebPage>>

    @Query("SELECT * FROM ${Table.COLLECTION}")
    fun getAllItemsStateFlow(): Flow<List<CollectionWebPage>>

    @Query("SELECT * FROM ${Table.COLLECTION} WHERE collectionTitle = :collectionTitle")
    fun getAllItemsByCollectionTitleStateFlow(collectionTitle: String?): Flow<List<CollectionWebPage>>

//    @Query("SELECT * FROM ${Table.COLLECTION} WHERE website = :website")
//    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<CollectionWebPage>>

//    @Query("SELECT * FROM ${Table.COLLECTION} WHERE website = :website")
//    fun getByItemWebsiteStateFlow(website: String?): Flow<CollectionWebPage>

    @Query("SELECT * FROM ${Table.COLLECTION}")
    suspend fun getAll(): List<CollectionWebPage>

    @Query("SELECT * FROM ${Table.COLLECTION} WHERE collectionTitle = :collectionTitle LIMIT 4")
    suspend fun getTop4By(collectionTitle: String?): List<CollectionWebPage>

    @Query("SELECT title FROM ${Table.COLLECTION}")
    suspend fun getAllTitles(): List<String?>

    @Query("SELECT collectionTitle FROM ${Table.COLLECTION}")
    suspend fun getAllCollectionTitles(): List<String?>

    // https://developer.android.com/topic/performance/sqlite-performance-best-practices
    @Query("SELECT DISTINCT collectionTitle FROM ${Table.COLLECTION}")
    suspend fun getAllUniqueTitles(): List<String?>


    @Transaction
    @Delete
    suspend fun delete(collectionWebPage: CollectionWebPage)

    @Transaction
    @Query("DELETE FROM ${Table.COLLECTION} WHERE collectionTitle = :collectionTitle")
    suspend fun deleteBy(collectionTitle: String?)

    @Transaction
    @Query("DELETE FROM ${Table.COLLECTION}")
    suspend fun deleteAll()

    // https://stackoverflow.com/questions/51139833/room-how-to-check-whether-row-exists
//    @Query("SELECT EXISTS(SELECT * FROM ${Table.COLLECTION} WHERE website = :website)")
//    suspend fun isItemPresent(website: String?): Boolean

    @Query("SELECT EXISTS(SELECT * FROM ${Table.COLLECTION})")
    suspend fun isExists(): Boolean
}
