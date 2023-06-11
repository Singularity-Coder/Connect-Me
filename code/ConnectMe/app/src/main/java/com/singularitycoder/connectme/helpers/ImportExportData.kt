package com.singularitycoder.connectme.helpers

import android.os.Environment
import com.singularitycoder.connectme.helpers.db.ConnectMeDatabase
import java.io.File
import java.io.FileWriter


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