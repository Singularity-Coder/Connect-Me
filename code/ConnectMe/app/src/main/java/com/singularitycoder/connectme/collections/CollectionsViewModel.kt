package com.singularitycoder.connectme.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionsDao: CollectionsDao
) : ViewModel() {

    fun getAllCollections() = collectionsDao.getAllItemsStateFlow()

    suspend fun getAllCollectionTitles() = collectionsDao.getAllCollectionTitles()

    suspend fun getAllUniqueCollectionTitles() = collectionsDao.getAllUniqueTitles()

    fun addToCollections(collectionWebPage: CollectionWebPage?) = viewModelScope.launch {
        collectionsDao.insert(collectionWebPage)
    }

    fun updateCollection(collectionWebPage: CollectionWebPage?) = viewModelScope.launch {
        collectionsDao.update(collectionWebPage)
    }

    fun deleteItem(collectionWebPage: CollectionWebPage?) = viewModelScope.launch {
        collectionsDao.delete(collectionWebPage)
    }

    // https://developer.android.com/kotlin/coroutines/coroutines-best-practices#create-coroutines-data-layer
//    suspend fun isItemPresent(website: String?): Boolean = collectionsDao.isItemPresent(website)
}
