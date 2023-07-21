package com.singularitycoder.connectme.helpers

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.work.impl.WorkDatabasePathHelper.getDatabasePath
import com.singularitycoder.connectme.helpers.db.ConnectMeDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.OutputStream

fun toCsv(db: ConnectMeDatabase) {
    // Get a reference to the underlying SQLiteDatabase
    val sqLiteDatabase = db.openHelper.writableDatabase

    // Execute your query and get a Cursor
    val cursor = sqLiteDatabase.query("SELECT * FROM your_table_name")
    val csvData = StringBuilder()
    if (cursor.moveToFirst()) {
        do {
            // Retrieve the data from the cursor
            val column1Data: String = cursor.getString(cursor.getColumnIndex("column1") ?: 0)
            val column2Data: String = cursor.getString(cursor.getColumnIndex("column2") ?: 0)
            // ... repeat this for each column you want to export

            // Append the data to the CSV string
            csvData.append(column1Data).append(",").append(column2Data).append("\n")
        } while (cursor.moveToNext())
    }
    val csvFile = File(Environment.getExternalStorageDirectory(), "exported_data.csv")
    try {
        val writer = FileWriter(csvFile)
        writer.append(csvData.toString())
        writer.flush()
        writer.close()
    } catch (_: Exception) {
    }
}


// https://github.com/android/architecture-components-samples/issues/340
fun Activity?.importDbFrom(uri: Uri) {
    this ?: return
    val inStream = getDatabasePath(this).inputStream()
    val outStream = contentResolver.openOutputStream(uri)

    inStream.use { input: FileInputStream ->
        outStream.use { output: OutputStream? ->
            if (output != null) {
                input.copyTo(output)
                this.recreate() // Restarting activity/app to show new db changes
            }
        }
    }
}

fun Context.exportDbTo(path: String) {
    println("Abs path: ${getDatabasePath(this).absolutePath}")
    println("Canon file: ${getDatabasePath(this).canonicalFile}")
    println("Canon path: ${getDatabasePath(this).canonicalPath}")
    println("free space: ${getDatabasePath(this).freeSpace}")
    println("isAbs: ${getDatabasePath(this).isAbsolute}")
    println("isDir: ${getDatabasePath(this).isDirectory}")
    println("isFile: ${getDatabasePath(this).isFile}")
    println("isHidden: ${getDatabasePath(this).isHidden}")
    println("Name: ${getDatabasePath(this).name}")
    println("Path: ${getDatabasePath(this).path}")
    println("Parent file: ${getDatabasePath(this).parentFile}")
    println("Total space: ${getDatabasePath(this).totalSpace}")
    println("usable space: ${getDatabasePath(this).usableSpace}")
    println("extension: ${getDatabasePath(this).extension}")
    println("invariant separators path: ${getDatabasePath(this).invariantSeparatorsPath}")
    println("name without ext: ${getDatabasePath(this).nameWithoutExtension}")
    println("isRooted: ${getDatabasePath(this).isRooted}")

    // This is giving /data/user/0/com.singularitycoder.connectme/no_backup/androidx.work.workdb
    val source = getDatabasePath(this).absoluteFile // copy file to external files dir
    copyFile(source = source, destination = File(path))
}