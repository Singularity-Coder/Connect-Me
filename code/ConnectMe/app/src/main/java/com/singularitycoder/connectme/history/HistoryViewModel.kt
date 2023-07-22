package com.singularitycoder.connectme.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao,
) : ViewModel() {

    fun addToHistory(history: History) = viewModelScope.launch {
        historyDao.insert(history)
    }

    fun getAllHistory() = historyDao.getAllItemsStateFlow()

    fun deleteItem(history: History?) = viewModelScope.launch {
        historyDao.delete(history)
    }

    fun deleteAllHistory() = viewModelScope.launch {
        historyDao.deleteAll()
    }

    fun deleteAllHistoryByTIme(elapsedTime: Long?) = viewModelScope.launch {
        historyDao.deleteAllByTime(elapsedTime)
    }
}
