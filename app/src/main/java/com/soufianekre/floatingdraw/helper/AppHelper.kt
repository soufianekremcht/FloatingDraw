package com.soufianekre.floatingdraw.helper

import android.os.Build

object AppHelper {

    // basic utils

    const val EXTRA_IMAGE = "extra.image"

    private const val ANDROID_R = "R"
    private const val DATA_PATTERN = "MMM d, yyyy  •  HH:mm"
    private const val MEGABYTE = 1000.0

    private const val IMAGE_EXTENSION_JPG = "jpg"
    private const val IMAGE_EXTENSION_PNG = "png"

    fun hasSdkHigherThan(sdk: Int): Boolean {
        return Build.VERSION.SDK_INT >= sdk
    }

    fun hasAndroid11(): Boolean {
        return Build.VERSION.CODENAME == ANDROID_R
    }

}