package com.singularitycoder.connectme.feed

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.singularitycoder.connectme.helpers.*
import com.singularitycoder.connectme.helpers.constants.WorkerData
import com.singularitycoder.connectme.helpers.db.ConnectMeDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class RssFollowWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ThisEntryPoint {
        fun db(): ConnectMeDatabase
        fun networkStatus(): NetworkStatus
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, ThisEntryPoint::class.java)
            val dao = dbEntryPoint.db().feedDao()
            val networkStatus = dbEntryPoint.networkStatus()
            val rssUrl = inputData.getString(WorkerData.RSS_URL)

            try {
                getRssFeedFrom(
                    url = rssUrl,
                    feedDao = dao,
                    networkStatus = networkStatus
                )
                Result.success()
            } catch (_: Exception) {
                Result.failure()
            }
        }
    }
}