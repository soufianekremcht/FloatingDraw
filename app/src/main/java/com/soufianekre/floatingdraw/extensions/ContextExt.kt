package com.soufianekre.floatingdraw.extensions

import android.content.Context
import com.soufianekre.floatingdraw.data.app_prefs.AppPreferences
import es.dmoral.toasty.Toasty

fun Context.appPrefs() : AppPreferences = AppPreferences.newInstance(this)

val Context.internalStoragePath: String get() = appPrefs().internalStoragePath
val Context.sdCardPath :String get() = appPrefs().sdCardPath

fun Context.showInfo(msg : String){
    Toasty.info(this,msg,Toasty.LENGTH_SHORT).show()
}


fun Context.showSuccess(msg : String){
    Toasty.success(this,msg,Toasty.LENGTH_SHORT).show()
}

fun Context.showError(msg:String){
    Toasty.error(this,msg,Toasty.LENGTH_SHORT).show()
}