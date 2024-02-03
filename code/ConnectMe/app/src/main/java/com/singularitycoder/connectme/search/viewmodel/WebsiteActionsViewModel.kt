package com.singularitycoder.connectme.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.connectme.feed.Feed
import com.singularitycoder.connectme.feed.FeedDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteActionsViewModel @Inject constructor(
    private val feedDao: FeedDao,
) : ViewModel() {

    fun getAllItemsStateFlow() = feedDao.getAllItemsStateFlow()

    fun getAllSavedItemsStateFlow() = feedDao.getAllSavedItemsStateFlow()

    fun updatedFeedItemToSaved(feed: Feed?) = viewModelScope.launch {
        feedDao.update(feed ?: return@launch)
    }

    fun deleteItem(feed: Feed?) = viewModelScope.launch {
        feedDao.delete(feed ?: return@launch)
    }
}