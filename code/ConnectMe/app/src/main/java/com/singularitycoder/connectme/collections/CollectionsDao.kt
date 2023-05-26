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
    suspend fun insert(collection: Collection?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<Collection?>)


    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(collection: Collection?)

//    @Query("UPDATE ${Table.COLLECTION} SET link = :link WHERE website LIKE :website")
//    fun updateWithPromptsList(link: String?, website: String)


//    @Transaction
//    @Query("SELECT * FROM ${Table.COLLECTION} WHERE website LIKE :website LIMIT 1")
//    suspend fun getItemByWebsite(website: String?): Collection?

    @Query("SELECT * FROM ${Table.COLLECTION}")
    fun getAllItemsLiveData(): LiveData<List<Collection?>>

    @Query("SELECT * FROM ${Table.COLLECTION}")
    fun getAllItemsStateFlow(): Flow<List<Collection?>>

//    @Query("SELECT * FROM ${Table.COLLECTION} WHERE website = :website")
//    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<Collection?>>

//    @Query("SELECT * FROM ${Table.COLLECTION} WHERE website = :website")
//    fun getByItemWebsiteStateFlow(website: String?): Flow<Collection?>

    @Query("SELECT * FROM ${Table.COLLECTION}")
    suspend fun getAll(): List<Collection?>


    @Transaction
    @Delete
    suspend fun delete(collection: Collection?)

//    @Transaction
//    @Query("DELETE FROM ${Table.COLLECTION} WHERE website = :website")
//    suspend fun deleteByWebsite(website: String?)

    @Transaction
    @Query("DELETE FROM ${Table.COLLECTION}")
    suspend fun deleteAll()

    // https://stackoverflow.com/questions/51139833/room-how-to-check-whether-row-exists
//    @Query("SELECT EXISTS(SELECT * FROM ${Table.COLLECTION} WHERE website = :website)")
//    suspend fun isItemPresent(website: String?): Boolean

    @Query("SELECT EXISTS(SELECT * FROM ${Table.COLLECTION})")
    suspend fun isExists(): Boolean
}
