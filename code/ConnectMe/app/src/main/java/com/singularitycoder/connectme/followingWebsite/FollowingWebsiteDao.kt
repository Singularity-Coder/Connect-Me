package com.singularitycoder.connectme.followingWebsite

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singularitycoder.connectme.helpers.constants.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowingWebsiteDao {

    /** room database will replace data based on primary key */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(followingWebsite: FollowingWebsite?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appList: List<FollowingWebsite?>)


    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(followingWebsite: FollowingWebsite?)

    @Query("UPDATE ${Table.FOLLOWING_WEBSITE} SET link = :link WHERE website LIKE :website")
    fun updateWithPromptsList(link: String?, website: String)


    @Transaction
    @Query("SELECT * FROM ${Table.FOLLOWING_WEBSITE} WHERE website LIKE :website LIMIT 1")
    suspend fun getItemByWebsite(website: String?): FollowingWebsite?

    @Query("SELECT * FROM ${Table.FOLLOWING_WEBSITE}")
    fun getAllItemsLiveData(): LiveData<List<FollowingWebsite?>>

    @Query("SELECT * FROM ${Table.FOLLOWING_WEBSITE}")
    fun getAllItemsStateFlow(): Flow<List<FollowingWebsite?>>

    @Query("SELECT * FROM ${Table.FOLLOWING_WEBSITE} WHERE website = :website")
    fun getAllItemsByWebsiteStateFlow(website: String?): Flow<List<FollowingWebsite?>>

    @Query("SELECT * FROM ${Table.FOLLOWING_WEBSITE} WHERE website = :website")
    fun getByItemWebsiteStateFlow(website: String?): Flow<FollowingWebsite?>

    @Query("SELECT * FROM ${Table.FOLLOWING_WEBSITE}")
    suspend fun getAll(): List<FollowingWebsite?>


    @Transaction
    @Delete
    suspend fun delete(followingWebsite: FollowingWebsite?)

    @Transaction
    @Query("DELETE FROM ${Table.FOLLOWING_WEBSITE} WHERE website = :website")
    suspend fun deleteByWebsite(website: String?)

    @Transaction
    @Query("DELETE FROM ${Table.FOLLOWING_WEBSITE}")
    suspend fun deleteAll()

    // https://stackoverflow.com/questions/51139833/room-how-to-check-whether-row-exists
    @Query("SELECT EXISTS(SELECT * FROM ${Table.FOLLOWING_WEBSITE} WHERE website = :website)")
    suspend fun isItemPresent(website: String?): Boolean

    @Query("SELECT EXISTS(SELECT * FROM ${Table.FOLLOWING_WEBSITE})")
    suspend fun isExists(): Boolean
}
