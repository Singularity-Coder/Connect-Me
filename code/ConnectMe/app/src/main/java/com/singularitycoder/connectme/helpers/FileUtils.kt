package com.singularitycoder.connectme.helpers

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import com.singularitycoder.connectme.downloads.Download
import com.singularitycoder.connectme.helpers.constants.FILE_PROVIDER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*


const val KB = 1024.0
const val MB = 1024.0 * KB
const val GB = 1024.0 * MB
const val TB = 1024.0 * GB

fun File.sizeInBytes(): Int {
    if (!this.exists()) return 0
    return this.length().toInt()
}

/** metric is KB, MB, GB, TB constants which is 1024.0, etc */
fun File.showSizeIn(metric: Double): Double {
    if (!this.exists()) return 0.0
    return this.sizeInBytes().div(metric)
}

fun File.extension(): String {
    if (!this.exists()) return ""
    return this.absolutePath.substringAfterLast(delimiter = ".").lowercase().trim()
}

fun File.nameWithExtension(): String {
    if (!this.exists()) return ""
    return this.absolutePath.substringAfterLast(delimiter = "/")
}

fun File.name(): String {
    if (!this.exists()) return ""
    return this.nameWithExtension().substringBeforeLast(".")
}

fun File.customName(prefix: String = "my_file"): String {
    if (!this.exists()) return ""
    return prefix.sanitize() + "_" + this.name().sanitize()
}

fun File?.customPath(directory: String?, fileName: String?): String {
    var path = this?.absolutePath

    if (directory != null) {
        path += File.separator + directory
    }

    if (fileName != null) {
        path += File.separator + fileName
    }

    return path ?: ""
}

/** /data/user/0/com.singularitycoder.aniflix/files */
fun Context.internalFilesDir(
    directory: String? = null,
    fileName: String? = null,
): File = File(filesDir.customPath(directory, fileName))

/** /storage/emulated/0/Android/data/com.singularitycoder.aniflix/files */
fun Context.externalFilesDir(
    rootDir: String = "",
    subDir: String? = null,
    fileName: String? = null,
): File = File(getExternalFilesDir(rootDir).customPath(subDir, fileName))

inline fun deleteAllFilesFrom(
    directory: File?,
    withName: String,
    crossinline onDone: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.Default).launch {
        directory?.listFiles()?.filter { it.exists() }?.forEach files@{ it: File? ->
            it ?: return@files
            if (it.name.contains(withName)) {
                if (it.exists()) it.delete()
            }
        }

        withContext(Dispatchers.Main) { onDone.invoke() }
    }
}

// Get path from Uri
// content resolver instance used for firing a query inside the internal sqlite database that contains all file info from android os
// projection is the set of columns u want to fetch from sqlite db
// query returns Cursor instance which is an interface which holds the data returned by the query
// So cursor holds the data and in this case it holds a single file
// cursor.moveToFirst() moves the cursor on first row, in this case only 1 row. with the cursor u can get each column data
// The 2 columns here are OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
// filesDir is the internal storage path
/** Copy file from external to internal storage */
fun Context.readFileFromExternalDbAndWriteFileToInternalDb(inputFileUri: Uri): File? {
    // Get file name and size
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    val cursor = contentResolver?.query(inputFileUri, projection, null, null, null)?.also {
        it.moveToFirst() // We are in first row of the table now
    }
    val inputFileNamePositionInRow = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    val inputFileSizePositionInRow = cursor?.getColumnIndex(OpenableColumns.SIZE)
    val inputFileName = cursor?.getString(inputFileNamePositionInRow ?: 0)
    val inputFileSize = cursor?.getLong(inputFileSizePositionInRow ?: 0)

    println(
        """
            Input File name: $inputFileName
            Input File size: $inputFileSize
        """.trimIndent()
    )

    // Copy file to internal storage
    return copyFileToInternalStorage(inputFileUri = inputFileUri, inputFileName = inputFileName ?: "")
}

fun Context.copyFileToInternalStorage(
    inputFileUri: Uri,
    inputCustomPath: String = "",
    inputFileName: String,
): File? {
    return try {
        val outputFile = if (inputCustomPath.isNotBlank()) {
            File(filesDir?.absolutePath + File.separator + inputCustomPath + File.separator + inputFileName) // Place where our input file is copied
        } else {
            File(filesDir?.absolutePath + File.separator + inputFileName) // Place where our input file is copied
        }
        val fileOutputStream = FileOutputStream(outputFile)
        val fileInputStream = contentResolver?.openInputStream(inputFileUri)
        fileOutputStream.write(fileInputStream?.readBytes())
        fileInputStream?.close()
        fileOutputStream.flush()
        fileOutputStream.close()
        outputFile
    } catch (e: IOException) {
        println(e.message)
        null
    }
}

fun Context.isOldStorageReadPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

// https://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
fun Bitmap?.saveToStorage(
    fileName: String,
    fileDir: String,
) {
//    val root: String = Environment.getExternalStorageDirectory().absolutePath
    val directory = File(fileDir).also {
        if (it.exists().not()) it.mkdirs()
    }
    val file = File(/* parent = */ directory, /* child = */ fileName).also {
        if (it.exists().not()) it.createNewFile() else return
    }
    try {
        val out = FileOutputStream(file)
        this?.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteBitmapFromInternalStorage(
    fileName: String,
    fileDir: String,
) {
    val directory = File(fileDir).also {
        if (it.exists().not()) return
    }
    File(/* parent = */ directory, /* child = */ fileName).also {
        if (it.exists()) it.delete()
    }
}

fun File?.toBitmap(): Bitmap? {
    return BitmapFactory.decodeFile(this?.absolutePath)
}

fun Context.getHomeLayoutBlurredImageFileDir(): String {
    return "${filesDir.absolutePath}/common_images"
}

/** Checks if a volume containing external storage is available for read and write. */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/** Checks if a volume containing external storage is available to at least read. */
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

fun Cursor.isStatusSuccessful(): Boolean {
    val columnStatus = this.getColumnIndex(DownloadManager.COLUMN_STATUS)
    return this.getInt(columnStatus) == DownloadManager.STATUS_SUCCESSFUL
}

fun Cursor.fileName(): String {
    val columnTitle = this.getColumnIndex(DownloadManager.COLUMN_TITLE)
    return this.getString(columnTitle)
}

fun Cursor.uriString(): String {
    val columnUri = this.getColumnIndex(DownloadManager.COLUMN_URI)
    return this.getString(columnUri)
}

fun Cursor.localUriString(): String {
    val columnLocalUri = this.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
    return this.getString(columnLocalUri)
}

fun prepareCustomName(
    url: String,
    prefix: String,
): String {
    if (url.isBlank() || prefix.isBlank()) return "file_${UUID.randomUUID()}".sanitize()
    val validExtensionsList = listOf(".mp4", ".jpg", ".jpeg", ".gif", ".png")
    val extension = ".${url.substringAfterLast(delimiter = ".")}".toLowCase().trim()
    val validExtension = if (validExtensionsList.contains(extension)) extension else ".png"
    return prefix.sanitize() + "_" +
            url.substringAfterLast(delimiter = "/")
                .substringBeforeLast(delimiter = ".")
                .toLowCase()
                .sanitize() + validExtension
}

/**
 * The idea is to replace all special characters with underscores
 * 48 to 57 are ASCII characters of numbers from 0 to 1
 * 97 to 122 are ASCII characters of lowercase alphabets from a to z
 * https://www.w3schools.com/charsets/ref_html_ascii.asp
 * */
fun String?.sanitize(): String {
    if (this.isNullOrBlank()) return ""
    var sanitizedString = ""
    val range0to9 = '0'.code..'9'.code
    val rangeLowerCaseAtoZ = 'a'.code..'z'.code
    this.forEachIndexed { index: Int, char: Char ->
        if (char.code !in range0to9 && char.code !in rangeLowerCaseAtoZ) {
            if (sanitizedString.lastOrNull() != '_' && this.lastIndex != index) {
                sanitizedString += "_"
            }
        } else {
            sanitizedString += char
        }
    }
    return sanitizedString
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun readStringFromStream(
    inputStream: InputStream,
    encoding: String
): String {
    val reader = BufferedReader(InputStreamReader(inputStream, encoding))
    val result = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        result.append(line)
    }
    return result.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.availableStorageSpace(
    storageType: StorageType = StorageType.INTERNAL,
): Long {
    val internalStorage = filesDir
    val externalStorage = getExternalFilesDir("") ?: File("")
    val storageManager = applicationContext.getSystemService<StorageManager>() ?: return 0L
    val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(if (storageType == StorageType.INTERNAL) internalStorage else externalStorage)
    return storageManager.getAllocatableBytes(appSpecificInternalDirUuid) // Available Bytes
}

enum class StorageType {
    INTERNAL, EXTERNAL
}

// https://stackoverflow.com/questions/8295773/how-can-i-transform-a-bitmap-into-a-uri
fun Bitmap?.uri(): Uri {
    val tempFile = File.createTempFile("temp_image", ".png")
    val bytes = ByteArrayOutputStream()
    this?.compress(Bitmap.CompressFormat.PNG, 100, bytes)
    val bitmapData = bytes.toByteArray()
    FileOutputStream(tempFile).apply {
        write(bitmapData)
        flush()
        close()
    }
    return Uri.fromFile(tempFile)
}

// https://developer.android.com/training/data-storage/shared/documents-files
private fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        val parcelFileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(this, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        image
    } catch (_: Exception) {
        null
    }
}

// https://stackoverflow.com/questions/7036381/copying-files-from-sdcard-to-android-internal-storage-directory
// https://stackoverflow.com/questions/4770004/how-to-move-rename-file-from-internal-app-storage-to-external-storage-on-android
fun copyFile(
    source: File,
    destination: File
) {
    if (source.absolutePath == source.absolutePath) return

    try {
        val `is`: InputStream = FileInputStream(source)
        val os: OutputStream = FileOutputStream(destination)
        val buff = ByteArray(1024)
        var len: Int
        while (`is`.read(buff).also { len = it } > 0) {
            os.write(buff, 0, len)
        }
        `is`.close()
        os.close()
    } catch (_: Exception) {
    }
}

// https://github.com/android/storage-samples/tree/main/ActionOpenDocumentTree
fun getMimeType(url: String): String {
    val ext = MimeTypeMap.getFileExtensionFromUrl(url)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/plain"
}

// https://github.com/android/storage-samples/tree/main/ActionOpenDocumentTree
fun getFilesListFrom(folder: File): List<File> {
    val rawFilesList = folder.listFiles()?.filter { !it.isHidden }

    return if (folder == Environment.getExternalStorageDirectory()) {
        rawFilesList?.toList() ?: listOf()
    } else {
        listOf(folder.parentFile) + (rawFilesList?.toList() ?: listOf())
    }
}

// https://github.com/android/storage-samples/tree/main/ActionOpenDocumentTree
fun Activity.openFile(selectedItem: File) {
    try {
        // Get URI and MIME type of file
        val uri = FileProvider.getUriForFile(this.applicationContext, FILE_PROVIDER, selectedItem)
        val mime: String = getMimeType(uri.toString())

        // Open file with user selected app
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, mime)
        }
        this.startActivity(intent)
    } catch (_: Exception) {
        showToast("Sorry. Cannot open this file.")
    }
}

fun File.getAppropriateSize(): String {
    return when {
        this.showSizeIn(MB) < 1 -> {
            "${String.format("%1.2f", this.showSizeIn(KB))} KB"
        }
        this.showSizeIn(MB) >= GB -> {
            "${String.format("%1.2f", this.showSizeIn(GB))} GB"
        }
        else -> {
            "${String.format("%1.2f", this.showSizeIn(MB))} MB"
        }
    }
}

/** /storage/emulated/0/Download/ConnectMe */
fun getDownloadDirectory(): File {
    return File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOWNLOADS}/ConnectMe").also {
        if (it.exists().not()) it.mkdirs()
    }
}

/**
 * https://gist.github.com/fiftyonemoon/433b563f652039e32c07d1d629f913fb
 * A class to read write external shared storage for android R.
 * Since Android 11 you can only access the android specified directories such as
 * DCIM, Download, Documents, Pictures, Movies, Music etc.
 */
fun Context.createNewMediaUri(
    directory: String?,
    filename: String?,
    mimetype: String?
): Uri? {
    val contentResolver = this.contentResolver
    val contentValues = ContentValues()

    //Set filename, if you don't system automatically use current timestamp as name
    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)

    //Set mimetype if you want
    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimetype)

    //To create folder in Android directories use below code
    //pass your folder path here, it will create new folder inside directory
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
    }

    //pass new ContentValues() for no values.
    //Specified uri will save object automatically in android specified directories.
    //ex. MediaStore.Images.Media.EXTERNAL_CONTENT_URI will save object into android Pictures directory.
    //ex. MediaStore.Videos.Media.EXTERNAL_CONTENT_URI will save object into android Movies directory.
    //if content values not provided, system will automatically add values after object was written.
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

/**
 * https://gist.github.com/fiftyonemoon/433b563f652039e32c07d1d629f913fb
 * If [ContentResolver] failed to delete the file, use trick,
 * SDK version is >= 29(Q)? use [SecurityException] and again request for delete.
 * SDK version is >= 30(R)? use [MediaStore.createDeleteRequest].
 */
fun Context.deleteFile(
    launcher: ActivityResultLauncher<IntentSenderRequest?>,
    fileUri: Uri
) {
    val contentResolver = this.contentResolver
    try {
        //delete object using resolver
        contentResolver.delete(fileUri, null, null)
    } catch (e: SecurityException) {
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val collection: ArrayList<Uri> = ArrayList()
            collection.add(fileUri)
            pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //if exception is recoverable then again send delete request using intent
            if (e is RecoverableSecurityException) {
                pendingIntent = e.userAction.actionIntent
            }
        }
        if (pendingIntent != null) {
            val sender = pendingIntent.intentSender
            val request: IntentSenderRequest = IntentSenderRequest.Builder(sender).build()
            launcher.launch(request)
        }
    }
}

// https://gist.github.com/fiftyonemoon/433b563f652039e32c07d1d629f913fb
fun Context.renameFile(
    fileUri: Uri?,
    newName: String?
) {
    fileUri ?: return
    // create content values with new name and update
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.contentResolver.update(fileUri, contentValues, null)
    }
}

// https://gist.github.com/fiftyonemoon/433b563f652039e32c07d1d629f913fb
fun Context.duplicateFile(fileUri: Uri): Uri? {
    val contentResolver = this.contentResolver
    val output = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
    val input = getPathFrom(fileUri)
    try {
        FileInputStream(input).use { inputStream ->  //input stream
            val out = contentResolver.openOutputStream(output!!) //output stream
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                out!!.write(buf, 0, len) //write input file data to output file
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return output
}

// https://gist.github.com/fiftyonemoon/433b563f652039e32c07d1d629f913fb
fun Context.getPathFrom(fileUri: Uri): String? {
    val cursor = this.contentResolver.query(fileUri, null, null, null, null) ?: return null
    var text: String? = null
    if (cursor.moveToNext()) {
        text = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA) ?: -1)
    }
    cursor.close()
    return text
}

fun File.getDownload(): Download? {
    if (this.exists().not()) return null
    val size = if (this.isDirectory) {
        "${getFilesListFrom(this).size} items"
    } else {
        if (this.extension.isBlank()) {
            this.getAppropriateSize()
        } else {
            "${this.extension.toUpCase()}  •  ${this.getAppropriateSize()}"
        }
    }
    return Download(
        path = this.absolutePath,
        title = this.nameWithoutExtension,
        time = this.lastModified(),
        size = size,
        link = "",
        extension = this.extension,
        isDirectory = this.isDirectory
    )
}

// https://stackoverflow.com/questions/9015372/how-to-rotate-a-bitmap-90-degrees
fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/** Problem: File size is increasing by atleast 3 to 10 times */
fun createRotatedImage(
    rotationInDegrees: Float,
    imagePath: String?,
    imageFolder: File?
) {
    val fileToRotate = File(imagePath ?: "")
    val rotatedBitmap = fileToRotate.toBitmap()?.rotate(rotationInDegrees)
    val rotatedFile = File(
        /* parent = */ imageFolder,
        /* child = */ "${fileToRotate.nameWithoutExtension}_${timeNow}.${fileToRotate.extension}"
    ).also {
        if (it.exists().not()) it.createNewFile()
    }
    rotatedFile.setLastModified(timeNow)
    FileOutputStream(rotatedFile).use {
        it.write(rotatedBitmap.toByteArray())
    }
}