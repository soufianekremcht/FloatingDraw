package com.soufianekre.floatingdraw.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.security.Permission


object PermissionsHelper {
    const val STORAGE_CODE : Int = 23

    fun checkStoragePermissions(context : Context) : Boolean {
        return (ActivityCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestStoragePermissions(context: Activity?){
        ActivityCompat.requestPermissions(context!!,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
        ,STORAGE_CODE)
    }


}