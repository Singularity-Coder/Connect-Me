package com.singularitycoder.connectme.downloads

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularitycoder.connectme.search.model.WebViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadDao: DownloadDao,
) : ViewModel() {

    private val _markedUpBitmapStateFlow = MutableStateFlow<Bitmap?>(null)
    val markedUpBitmapStateFlow = _markedUpBitmapStateFlow.asStateFlow()

    fun addToDownloads(download: Download?) = viewModelScope.launch {
        downloadDao.insert(download ?: return@launch)
    }

    fun getAllDownloadsFlow() = downloadDao.getAllItemsStateFlow()

    suspend fun getAllDownloads() = downloadDao.getAll()

    suspend fun getDownloadItemByLink(link: String?) = downloadDao.getItemByLink(link)

    suspend fun getLast3DownloadItems() = downloadDao.getLast3By()

    fun deleteItem(download: Download?) = viewModelScope.launch {
        downloadDao.delete(download ?: return@launch)
    }

    fun deleteAllDownloads() = viewModelScope.launch {
        downloadDao.deleteAll()
    }

    fun deleteAllDownloadsByTime(elapsedTime: Long?) = viewModelScope.launch {
        downloadDao.deleteAllByTime(elapsedTime)
    }

    fun setMarkedUpBitmap(bitmap: Bitmap?) {
        _markedUpBitmapStateFlow.value = bitmap
    }

    fun resetMarkedUpBitmap() {
        _markedUpBitmapStateFlow.value = null
    }
}
