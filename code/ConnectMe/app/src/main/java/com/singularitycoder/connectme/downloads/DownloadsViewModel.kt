package com.singularitycoder.connectme.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadDao: DownloadDao,
) : ViewModel() {

    fun addToDownloads(download: Download?) = viewModelScope.launch {
        downloadDao.insert(download)
    }

    fun getAllDownloadsFlow() = downloadDao.getAllItemsStateFlow()

    suspend fun getAllDownloads() = downloadDao.getAll()

    suspend fun getDownloadItemByLink(link: String?) = downloadDao.getItemByLink(link)

    suspend fun getLast3DownloadItems() = downloadDao.getLast3By()

    fun deleteItem(download: Download?) = viewModelScope.launch {
        downloadDao.delete(download)
    }

    fun deleteAllDownloads() = viewModelScope.launch {
        downloadDao.deleteAll()
    }

    fun deleteAllDownloadsByTime(elapsedTime: Long?) = viewModelScope.launch {
        downloadDao.deleteAllByTime(elapsedTime)
    }
}
