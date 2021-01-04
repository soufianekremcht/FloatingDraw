package com.soufianekre.floatingdraw.ui.save_image

import android.app.Activity
import com.afollestad.materialdialogs.MaterialDialog
import com.soufianekre.floatingdraw.R

class ConfirmationDialog (val activity : Activity,val title :String,callback : () -> Unit){

    init {
        MaterialDialog(activity.baseContext).show{
            title(text = title)
            message(text = "Do you want to replace the existed File with this one ?")
            positiveButton (R.string.confirm){
                callback()
                it.dismiss()
            }
            negativeButton {
                it.dismiss()
            }
        }
    }
}
