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

    fun getCollectionsByCollectionTitles(collectionTitle: String?) = collectionsDao.getAllItemsByCollectionTitleStateFlow(collectionTitle)

    suspend fun getAllUniqueCollectionTitles() = collectionsDao.getAllUniqueTitles()

    suspend fun getTop4CollectionsBy(collectionTitle: String?) = collectionsDao.getTop4By(collectionTitle)

    fun addToCollections(collectionWebPage: CollectionWebPage?) = viewModelScope.launch {
        collectionsDao.insert(collectionWebPage)
    }

    fun updateCollection(collectionWebPage: CollectionWebPage?) = viewModelScope.launch {
        collectionsDao.update(collectionWebPage)
    }

    fun deleteItem(collectionWebPage: CollectionWebPage?) = viewModelScope.launch {
        collectionsDao.delete(collectionWebPage)
    }

    fun deleteAllItemsBy(collectionTitle: String?) = viewModelScope.launch {
        collectionsDao.deleteBy(collectionTitle)
    }

    // https://developer.android.com/kotlin/coroutines/coroutines-best-practices#create-coroutines-data-layer
//    suspend fun isItemPresent(website: String?): Boolean = collectionsDao.isItemPresent(website)
}
