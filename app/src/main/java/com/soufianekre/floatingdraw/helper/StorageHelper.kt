package com.soufianekre.floatingdraw.helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.extensions.getCompressionFormat
import com.soufianekre.floatingdraw.models.FileDirItem
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object StorageHelper {

    private const val QUALITY: Int = 70

    @Suppress("deprecation")
    suspend fun saveImage(
            context: Context,
            bitmap: Bitmap,
            fileDirItem:FileDirItem): Boolean {
        return if (AppHelper.hasSdkHigherThan(Build.VERSION_CODES.Q)) {
            //saveImageInAndroidQ(context, bitmap, format)
            true
        } else {

            val externalDir = Environment.getExternalStorageDirectory().path
            val dir = File(externalDir, context.getString(R.string.app_name))
            ensureDirExists(dir)
            val format = fileDirItem.path.getCompressionFormat()
            val extension = ImageUtils.getImageExtension(format)
            val file = File(fileDirItem.path)
            saveImageInOreo(context, file, bitmap, format)
        }
    }

    private suspend fun saveImageInOreo(
            context: Context, file: File, bitmap: Bitmap,
            format: Bitmap.CompressFormat): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            try {
                result = FileOutputStream(file).use {
                    bitmap.compress(
                            format,
                            QUALITY, it
                    )
                    it.close()
                    return@use true
                }

            } catch (e: Exception) {
                Timber.e("Unable to save image. Reason: ${e.message}")
            }
            sendScanFileBroadcast(context, file)
        }


        return result
    }

    private fun ensureDirExists(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    /**
     * Due to the developments on scoped storage [Intent.ACTION_MEDIA_SCANNER_SCAN_FILE] was
     * deprecated on API 29. This broadcast will no longer be needed to call after an image
     * creation/ update once the code is migrated to use [MediaStore].
     */
    @Suppress("deprecation")
    private fun sendScanFileBroadcast(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        context.sendBroadcast(intent)
    }

    // saving image in DCIM Dir for API 29+
    /*
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveImageInAndroidQ(context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat) {
        withContext(Dispatchers.IO) {
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val dirDest = File(Environment.DIRECTORY_PICTURES, context.getString(R.string.app_name))
            val date = System.currentTimeMillis()
            val extension = ImageUtils.getImageExtension(format)
            val newImage = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$date.$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension")
                put(MediaStore.MediaColumns.DATE_ADDED, date)
                put(MediaStore.MediaColumns.DATE_MODIFIED, date)
                put(MediaStore.MediaColumns.SIZE, bitmap.byteCount)
                put(MediaStore.MediaColumns.WIDTH, bitmap.width)
                put(MediaStore.MediaColumns.HEIGHT, bitmap.height)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirDest${File.separator}")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val newImageUri = context.contentResolver.insert(collection, newImage)

            context.contentResolver.openOutputStream(newImageUri!!, "w").use {
                bitmap.compress(format, QUALITY, it)
            }
            newImage.clear()
            newImage.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(newImageUri, newImage, null, null)
        }
    }

     */

    suspend fun getFileOutputStreamForAndroidO(activity: BaseActivity, fileDirItem: FileDirItem, b: Boolean,
                                               callback: (OutputStream?) -> Unit) {
        var out: FileOutputStream? = null
        withContext(Dispatchers.IO) {
            try {
                out = FileOutputStream(fileDirItem.name)
                callback(out)
            } catch (e: Exception) {
                Timber.e("Unable to get File OutputStream. Reason: ${e.message}")
                callback(out)
            }

        }
    }


}