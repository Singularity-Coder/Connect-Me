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

    fun deleteItem(followingWebsite: FollowingWebsite?) = viewModelScope.launch {
        followingWebsiteDao.delete(followingWebsite)
    }

    fun deleteAllFollowingWebsites() = viewModelScope.launch {
        followingWebsiteDao.deleteAll()
    }

    fun addFollowingWebsite(followingWebsite: FollowingWebsite?) = viewModelScope.launch {
        followingWebsiteDao.insert(followingWebsite)
    }

    // https://developer.android.com/kotlin/coroutines/coroutines-best-practices#create-coroutines-data-layer
    suspend fun isItemPresent(website: String?): Boolean = coroutineScope {
        followingWebsiteDao.isItemPresent(website)
    }
}
