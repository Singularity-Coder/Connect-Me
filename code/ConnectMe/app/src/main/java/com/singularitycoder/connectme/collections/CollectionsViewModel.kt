package com.singularitycoder.connectme.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionsDao: CollectionsDao
) : ViewModel() {

    fun getAllCollections() = collectionsDao.getAllItemsStateFlow()

    fun deleteItem(collection: Collection?) = viewModelScope.launch {
        collectionsDao.delete(collection)
    }

    fun addCollection(collection: Collection?) = viewModelScope.launch {
        collectionsDao.insert(collection)
    }

    // https://developer.android.com/kotlin/coroutines/coroutines-best-practices#create-coroutines-data-layer
//    suspend fun isItemPresent(website: String?): Boolean = coroutineScope {
//        collectionsDao.isItemPresent(website)
//    }
}
