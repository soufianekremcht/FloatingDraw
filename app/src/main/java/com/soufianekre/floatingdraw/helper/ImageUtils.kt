package com.soufianekre.floatingdraw.helper

import android.graphics.Bitmap

object ImageUtils {

    private fun getImageFormat(type : String): Bitmap.CompressFormat{
        return when (type) {
            Bitmap.CompressFormat.PNG.name -> {
                Bitmap.CompressFormat.PNG
            }
            Bitmap.CompressFormat.JPEG.name -> {
                Bitmap.CompressFormat.JPEG
            }
            Bitmap.CompressFormat.WEBP.name -> {
                Bitmap.CompressFormat.WEBP
            }
            else -> {
                Bitmap.CompressFormat.JPEG
            }
        }
    }

    fun getImageExtension(format: Bitmap.CompressFormat): String {
        return ".png"
    }
}