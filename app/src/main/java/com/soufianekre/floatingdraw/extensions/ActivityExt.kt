package com.soufianekre.floatingdraw.extensions

import com.soufianekre.floatingdraw.helper.PermissionsHelper
import com.soufianekre.floatingdraw.models.FileDirItem
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

fun BaseActivity.getCurrentFormattedDateTime() :String{
    val date = Date()
    var formatter =  SimpleDateFormat("dd-mm-yyyy")
    return formatter.format(date).toString()

}
