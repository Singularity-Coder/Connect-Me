package com.singularitycoder.connectme.followingWebsite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingWebsiteViewModel @Inject constructor(
    private val followingWebsiteDao: FollowingWebsiteDao,
) : ViewModel() {

    fun getAllFollowingWebsites() = followingWebsiteDao.getAllItemsStateFlow()

    suspend fun getTop4FollowingWebsites() = followingWebsiteDao.getTop4By()

    fun deleteItem(followingWebsite: FollowingWebsite?) = viewModelScope.launch {
        followingWebsiteDao.delete(followingWebsite)
    }

    fun deleteAllFollowingWebsites() = viewModelScope.launch {
        followingWebsiteDao.deleteAll()
    }

    fun addFollowingWebsite(followingWebsite: FollowingWebsite?) = viewModelScope.launch {
        followingWebsiteDao.insert(followingWebsite)
    }

    suspend fun isItemPresent(website: String?): Boolean = followingWebsiteDao.isItemPresent(website)
}
