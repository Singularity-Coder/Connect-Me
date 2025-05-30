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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.media.MediaScannerConnection
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
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.singularitycoder.connectme.downloads.Download
import com.singularitycoder.connectme.helpers.constants.FILE_PROVIDER
import com.singularitycoder.connectme.helpers.constants.ImageFormat
import com.singularitycoder.connectme.helpers.constants.MimeType
import com.singularitycoder.connectme.helpers.librera.LinkedJSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


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
fun Context.getPathFrom(fileUri: Uri): String? {
    val cursor = this.contentResolver.query(fileUri, null, null, null, null) ?: return null
    var text: String? = null
    if (cursor.moveToNext()) {
        text = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA) ?: -1)
    }
    cursor.close()
    return text
}

fun File.toDownload(): Download? {
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
    return Bitmap.createBitmap(
        /* source = */ this,
        /* x = */ 0,
        /* y = */ 0,
        /* width = */ width,
        /* height = */ height,
        /* m = */ matrix,
        /* filter = */ true
    )
}

/** Problem: File size is increasing by atleast 3 to 10 times */
fun createRotatedImage(
    rotationInDegrees: Float,
    imageFileToRotate: File,
    outputFolder: File?
) {
    if (isExternalStorageWritable().not()) return
    val rotatedBitmap = imageFileToRotate.toBitmap()?.rotate(rotationInDegrees)
    rotatedBitmap.createFile(inputFile = imageFileToRotate, outputFolder = outputFolder)
}

fun Bitmap?.createFile(
    inputFile: File,
    outputFolder: File?
) {
    if (isExternalStorageWritable().not()) return
    val outputFile = File(
        /* parent = */ outputFolder,
        /* child = */ "${inputFile.nameWithoutExtension}_${timeNow}.${inputFile.extension}"
    ).also {
        if (it.exists().not()) it.createNewFile()
    }
    FileOutputStream(outputFile).use {
        it.write(this.toByteArray())
    }
}

// https://stackoverflow.com/questions/36305362/how-to-convert-image-to-pdf
fun createPdf(
    pathWithFileNameList: List<String?>,
    outputFolder: File
) {
    if (isExternalStorageWritable().not()) return
    if (pathWithFileNameList.isEmpty()) return
    val document = PdfDocument()
    try {
        pathWithFileNameList.forEachIndexed { index: Int, pathWithFileName: String? ->
            val imageFile = File(pathWithFileName ?: return@forEachIndexed)
            if (imageFile.extension !in ImageFormat.values().map { it.value }) return
            val bitmap = imageFile.toBitmap() ?: return@forEachIndexed
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
            val pageInfo = PageInfo.Builder(
                /* pageWidth = */ bitmap.width,
                /* pageHeight = */ bitmap.height,
                /* pageNumber = */ index
            ).create()
            val paint = Paint().apply { this.color = Color.WHITE }
            val page = document.startPage(pageInfo).apply {
                this.canvas.drawPaint(paint)
                this.canvas.drawBitmap(
                    /* bitmap = */ scaledBitmap,
                    /* left = */ 0f,
                    /* top = */ 0f,
                    /* paint = */ null
                )
            }
            document.finishPage(page)
        }
        val pdfFileName = if (pathWithFileNameList.size == 1) {
            "${File(pathWithFileNameList.first() ?: "").nameWithoutExtension}_${timeNow}"
        } else {
            "PDF_${timeNow}"
        }
        // write the document content
        val pdfFile = File("${outputFolder.absolutePath}/${pdfFileName}.pdf")
        document.writeTo(FileOutputStream(pdfFile))
    } catch (_: Exception) {
    } finally {
        document.close()
    }
}

// https://stackoverflow.com/questions/22507635/convert-image-format-in-android-java
fun convertImage(
    imageFile: File,
    outputFolder: File?,
    imageFormat: ImageFormat
) {
    if (isExternalStorageWritable().not()) return
    val convertedFile = File(
        /* parent = */ outputFolder,
        /* child = */ "${imageFile.nameWithoutExtension}_${timeNow}.${imageFormat.value}"
    ).also {
        if (it.exists().not()) it.createNewFile()
    }
    val out = FileOutputStream(convertedFile)
    try {
        val format = when (imageFormat) {
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImageFormat.WebP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSLESS
            } else {
                Bitmap.CompressFormat.PNG
            }

            else -> Bitmap.CompressFormat.PNG
        }
        // 100 is best quality
        val bitmap = imageFile.toBitmap()
        bitmap?.compress(
            /* format = */ format,
            /* quality = */ 100,
            /* stream = */ out
        )
        out.use {
            it.write(bitmap.toByteArray())
        }
    } catch (_: Exception) {
    } finally {
        out.close()
    }
}

fun duplicateFile(
    fileToDuplicate: File?,
    outputFolder: File?
) {
    if (isExternalStorageWritable().not()) return
    val duplicatedFile = File(
        /* parent = */ outputFolder,
        /* child = */ "${fileToDuplicate?.nameWithoutExtension}_${timeNow}.${fileToDuplicate?.extension}"
    ).also {
        if (it.exists().not()) it.createNewFile()
    }
    FileOutputStream(duplicatedFile).use {
        it.write(fileToDuplicate?.toByteArray())
    }
}

// https://gist.github.com/fiftyonemoon/433b563f652039e32c07d1d629f913fb
//fun Context.duplicateFile(fileUri: Uri): Uri? {
//    val contentResolver = this.contentResolver
//    val output = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
//    val input = getPathFrom(fileUri)
//    try {
//        FileInputStream(input).use { inputStream ->  //input stream
//            val out = contentResolver.openOutputStream(output!!) //output stream
//            val buf = ByteArray(1024)
//            var len: Int
//            while (inputStream.read(buf).also { len = it } > 0) {
//                out!!.write(buf, 0, len) //write input file data to output file
//            }
//        }
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//    return output
//}

// https://stackoverflow.com/questions/13352972/convert-file-to-byte-array-and-vice-versa
fun File.toByteArray(): ByteArray? {
    return try {
        val fileBytes = ByteArray(this.length().toInt())
        FileInputStream(this).use { inputStream -> inputStream.read(fileBytes) }
        fileBytes
    } catch (_: Exception) {
        null
    }
}

// https://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap
fun Drawable.isTooLargeImage(): Boolean {
    val sumPixels = this.intrinsicWidth * this.intrinsicHeight
    val maxPixels = 2048 * 2048
    return sumPixels > maxPixels
}

fun Bitmap.isTooLargeImage(): Boolean {
    val sumPixels = this.width * this.height
    val maxPixels = 2048 * 2048
    return sumPixels > maxPixels
}

// https://github.com/plateaukao/einkbro
fun Context.showFileExecChooser(
    uri: Uri,
    resultLauncher: ActivityResultLauncher<Intent>? = null
) {
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = uri
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    try {
        startActivity(Intent.createChooser(intent, "Open file with"))
    } catch (exception: SecurityException) {
        showToast("open file failed, re-select the file again.")
        val openDocIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MimeType.ANY.value
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("android.provider.extra.INITIAL_URI", uri)
        }
        resultLauncher?.launch(openDocIntent)
    }
}

// https://stackoverflow.com/questions/3004713/get-content-uri-from-file-path-in-android
// https://stackoverflow.com/questions/27602986/convert-a-file-path-to-uri-in-android/53349110#53349110
fun Context.getUsableUri(file: File, callback: ((path: String?, uri: Uri) -> Unit)? = null) {
    MediaScannerConnection.scanFile(
        /* context = */ this,
        /* paths = */ arrayOf<String>(file.absolutePath),
        /* mimeTypes = */ null,
        /* callback = */ MediaScannerConnection.OnScanCompletedListener { path: String?, uri: Uri ->
            callback?.invoke(path, uri)
        }
    )
}

// https://mobikul.com/zip-unzip-file-folder-android-programmatically/
fun zip(
    filesListToZip: Array<String>,
    zippedFile: String?
) {
    if (isExternalStorageWritable().not()) return
    val BUFFER_SIZE = 6 * 1024
    var origin: BufferedInputStream? = null
    val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zippedFile)))
    try {
        val data = ByteArray(BUFFER_SIZE)
        for (i in filesListToZip.indices) {
            val fi = FileInputStream(filesListToZip[i])
            origin = BufferedInputStream(fi, BUFFER_SIZE)
            try {
                val entry = ZipEntry(filesListToZip[i].substring(filesListToZip[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER_SIZE).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
            } finally {
                origin.close()
            }
        }
    } finally {
        out.close()
    }
}

// https://mobikul.com/zip-unzip-file-folder-android-programmatically/
fun unzip(
    zippedFile: String?,
    outputLocation: String
) {
    try {
        if (isExternalStorageWritable().not()) return
        val file = File(outputLocation)
        if (file.isDirectory.not()) file.mkdirs()
        val zin = ZipInputStream(FileInputStream(zippedFile))
        try {
            var ze: ZipEntry? = null
            while (zin.nextEntry.also { ze = it } != null) {
                val path = outputLocation + File.separator + ze?.name
                if (ze?.isDirectory == true) {
                    val unzipFile = File(path)
                    if (!unzipFile.isDirectory) {
                        unzipFile.mkdirs()
                    }
                } else {
                    val fout = FileOutputStream(path, false)
                    try {
                        var c = zin.read()
                        while (c != -1) {
                            fout.write(c)
                            c = zin.read()
                        }
                        zin.closeEntry()
                    } finally {
                        fout.close()
                    }
                }
            }
        } finally {
            zin.close()
        }
    } catch (_: Exception) {
    }
}

// https://stackoverflow.com/questions/13119582/immutable-bitmap-crash-error
fun Bitmap.toMutableBitmap(): Bitmap {
    return this.copy(Bitmap.Config.ARGB_8888, true)
}

// https://gist.github.com/samsonjs/3693545 - Scale a bitmap preserving the aspect ratio.
fun Bitmap.scale(maxWidth: Int, maxHeight: Int): Bitmap? {
    // Determine the constrained dimension, which determines both dimensions.
    val width: Int
    val height: Int
    val widthRatio = this.width.toFloat() / maxWidth
    val heightRatio = this.height.toFloat() / maxHeight
    // Width constrained.
    if (widthRatio >= heightRatio) {
        width = maxWidth
        height = (width.toFloat() / this.width * this.height).toInt()
    } else {
        height = maxHeight
        width = (height.toFloat() / this.height * this.width).toInt()
    }
    val scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val ratioX = width.toFloat() / this.width
    val ratioY = height.toFloat() / this.height
    val middleX = width / 2.0f
    val middleY = height / 2.0f
    val scaleMatrix = Matrix().apply {
        setScale(ratioX, ratioY, middleX, middleY)
    }
    Canvas(scaledBitmap).apply {
        setMatrix(scaleMatrix)
        drawBitmap(
            /* bitmap = */ this@scale,
            /* left = */ middleX - this@scale.width / 2,
            /* top = */ middleY - this@scale.height / 2,
            /* paint = */ Paint(Paint.FILTER_BITMAP_FLAG)
        )
    }
    return scaledBitmap
}

// https://stackoverflow.com/questions/10630373/android-image-view-pinch-zooming
fun pinchToZoom() {

}

// https://www.youtube.com/watch?v=tFdTNANwgcw&ab_channel=edureka%21
fun writeToTextFile(
    outputPath: String,
    text: String
) = try {
    FileWriter(outputPath).apply {
        write(text)
        close()
    }
} catch (_: Exception) {
}

// https://www.youtube.com/watch?v=tFdTNANwgcw&ab_channel=edureka%21
fun readFromTextFile(
    inputFile: File,
): String {
    var text = ""
    try {
        val scanner = Scanner(inputFile)
        while (scanner.hasNextLine()) {
            text += scanner.nextLine()
        }
        scanner.close()
    } catch (_: Exception) {
    }
    return text
}

var rootPath = File(Environment.getExternalStorageDirectory(), "Librera").toString()

fun File.getText(
    withSeparator: Boolean = false
): String? = try {
    val stringBuilder = StringBuilder()
    var aux: String? = ""
    val reader = BufferedReader(FileReader(this))
    val separator = System.getProperty("line.separator")
    while (reader.readLine().also { aux = it } != null) {
        stringBuilder.append(aux)
        if (withSeparator) {
            stringBuilder.append(separator)
        }
    }
    reader.close()
    stringBuilder.toString()
} catch (_: Exception) {
    null
}

// https://stackoverflow.com/questions/58750885/how-can-i-convert-pdf-file-to-text
fun File.getTextFromPdf(): String? = try {
    var parsedText = ""
    val reader = PdfReader(this.absolutePath)
    for (i in 0 until reader.numberOfPages) {
        // Extracting the content from different pages
        parsedText = "$parsedText${PdfTextExtractor.getTextFromPage(reader, i + 1).trim { it <= ' ' }}\n\n\n\n"
    }
    reader.close()
    parsedText
} catch (_: Exception) {
    null
}

fun File?.getLinkedJsonObject(): LinkedJSONObject {
    val fileText = this?.getText()
    return if (fileText.isNullOrBlank()) {
        LinkedJSONObject()
    } else try {
        LinkedJSONObject(fileText)
    } catch (_: JSONException) {
        LinkedJSONObject()
    }
}
