package com.soufianekre.floatingdraw.helper

import android.Manifest
import android.R
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import timber.log.Timber


// Use Dexter Permissions

object PermissionsHelper {
    const val STORAGE_CODE : Int = 23

    fun isStoragePermissionsGranted(context : Context) : Boolean {
        return (ActivityCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestStoragePermissions(context: Activity?){
        ActivityCompat.requestPermissions(context!!,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ,STORAGE_CODE)
    }

    fun handleStoragePermissions(context: Context, callback: (granted : Boolean) -> Unit){

        val listener: MultiplePermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) {
                    Timber.e("Permission Granted")
                    callback(true)
                } else {
                    Timber.e("Permission Denied")
                    callback(false)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                token.continuePermissionRequest()
            }
        }


        val dialogMultiplePermissionsListener: MultiplePermissionsListener = DialogOnAnyDeniedMultiplePermissionsListener.Builder
                .withContext(context)
                .withTitle("Storage permission")
                .withMessage("Storage permission are needed to use backup / restore")
                .withButtonText(R.string.ok)
                .build()

        val compositePermissionsListener: MultiplePermissionsListener = CompositeMultiplePermissionsListener(listener,
                dialogMultiplePermissionsListener)

        Dexter.withContext(context).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(compositePermissionsListener)
                .withErrorListener { error: DexterError -> Timber.e(error.name) }
                .check()
    }


}
