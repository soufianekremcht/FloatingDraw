package com.soufianekre.floatingdraw.ui.save_image

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.files.folderChooser
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.extensions.*
import com.soufianekre.floatingdraw.data.app_prefs.JPG
import com.soufianekre.floatingdraw.data.app_prefs.PNG
import com.soufianekre.floatingdraw.data.app_prefs.SVG
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import kotlinx.android.synthetic.main.dialog_save_image.*
import kotlinx.android.synthetic.main.dialog_save_image.view.*
import java.io.File

class SaveImageDialog(val activity : BaseActivity, val defaultPath:String, val defaultFilename: String,
                      val defaultExtension : String, val hidePath: Boolean,
                      callback : (fullPath:String,filename:String,extension:String) -> Unit){


    private val SIMPLE_DRAW = "Simple Draw"

    init{
        val initialFilename = getInitialFilename()
        var folder = if (defaultPath.isEmpty()) "${activity.baseContext.internalStoragePath}/$SIMPLE_DRAW"
        else defaultPath

        val dialog_view = activity.layoutInflater.inflate(R.layout.dialog_save_image,null).apply {
            save_image_filename_field.setText(initialFilename)
            save_image_radio_group.check(
                when (defaultExtension) {
                    JPG -> R.id.save_image_radio_jpg
                    SVG -> R.id.save_image_radio_svg
                    else -> R.id.save_image_radio_png
                }
            )
            save_image_path.setOnClickListener{
                MaterialDialog(activity).show {
                    folderChooser(activity) { dialog, it ->
                        // File selected
                        save_image_path.text = it.absolutePath
                        folder = it.absolutePath
                    }

                }
            }

            MaterialDialog(activity)
                .title(text = "Save as")
                .customView(view = this)
                .positiveButton(R.string.ok) {
                    val filename = save_image_filename_field.text.toString()
                    if (filename.isEmpty()){
                        activity.showError(activity.getString(R.string.filename_cannot_be_empty))
                        return@positiveButton
                    }
                    val extension = when (save_image_radio_group.checkedRadioButtonId){
                        R.id.save_image_radio_png -> PNG
                        R.id.save_image_radio_svg -> SVG
                        else -> JPG
                    }
                    /// check this
                    val newPath  ="§{folder.trimEnd('/')}/$filename.$extension"
                    if (!newPath.getFilenameFromPath().isAValidFilename()){
                        activity.showError(activity.getString(R.string.filename_invalid_characters))
                        return@positiveButton
                    }
                    if (!hidePath && File(newPath).exists()) {
                        val title = String.format(
                            activity.getString(R.string.file_already_existes_overwrite),
                            newPath.getFilenameFromPath())

                        ConfirmationDialog(activity, title) {
                            callback(newPath, filename, extension)
                            it.dismiss()
                        }
                    }else {
                        callback(newPath, filename, extension)
                        it .dismiss()
                    }
                }
                .negativeButton(R.string.cancel) {
                    it.dismiss()
                }
                .show()
        }
    }

    private fun getInitialFilename() : String{
        val newFilename = "image_${activity.getCurrentFormattedDateTime()}"
        return if (defaultFilename.isEmpty()) newFilename else defaultFilename
    }


}
