package com.soufianekre.floatingdraw.ui.save

import android.os.Environment
import android.view.View.GONE
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.data.app_prefs.JPG
import com.soufianekre.floatingdraw.data.app_prefs.PNG
import com.soufianekre.floatingdraw.data.app_prefs.SVG
import com.soufianekre.floatingdraw.extensions.getCurrentFormattedDateTime
import com.soufianekre.floatingdraw.extensions.getFilenameFromPath
import com.soufianekre.floatingdraw.extensions.isAValidFilename
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import kotlinx.android.synthetic.main.dialog_save_image.view.*
import java.io.File

class SaveImageBottomSheet(val activity: BaseActivity, var defaultPath: String, var defaultFilename: String,
                           var defaultExtension: String, var hidePath: Boolean,
                           callback: (fullPath: String, filename: String, extension: String) -> Unit) {


    private val SIMPLE_DRAW = "Simple Draw"

    init {
        val initialFilename = getInitialFilename()
        var current_folder : File = Environment.getExternalStorageDirectory();


        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_save_image, null)
        dialogView.apply {
            save_dialog_file_name_field.setText(initialFilename)
            save_image_radio_group.check(
                    when (defaultExtension) {
                        JPG -> R.id.save_image_radio_jpg
                        SVG -> R.id.save_image_radio_svg
                        else -> R.id.save_image_radio_png
                    }
            )
            save_image_path.text = current_folder.path
        }

        if (hidePath) {
            dialogView.save_image_path.visibility = GONE
        }else {
            dialogView.save_image_path.setOnClickListener {
                MaterialDialog(activity).show {
                    folderChooser(
                            this.windowContext,
                            current_folder,
                            allowFolderCreation = true,
                            folderCreationLabel = R.string.new_folder)
                    { dialog, file ->
                        // File selected
                        dialogView.save_image_path.text = file.absolutePath
                        current_folder = file
                    }
                }
            }
        }

        MaterialDialog(activity,BottomSheet())
                .title(text = "Save as")
                .cornerRadius(res = R.dimen.my_corner_radius)
                .lifecycleOwner(activity)
                .customView(view = dialogView)
                .positiveButton(R.string.ok) {
                    val filename = it.view.save_dialog_file_name_field.text.toString()
                    if (filename.isEmpty()) {
                        activity.showError(activity.getString(R.string.filename_cannot_be_empty))
                        return@positiveButton
                    }
                    val extension = when (it.view.save_image_radio_group.checkedRadioButtonId) {
                        R.id.save_image_radio_png -> PNG
                        R.id.save_image_radio_svg -> SVG
                        else -> JPG
                    }
                    /// check if New Forged Path is Valid as a File name
                    val newPath = "${current_folder.path.trimEnd('/')}/$filename.$extension"
                    if (!newPath.getFilenameFromPath().isAValidFilename()) {
                        activity.showError(activity.getString(R.string.filename_invalid_characters))
                        return@positiveButton
                    }
                    if (!hidePath && File(newPath).exists()) {
                        val title = String.format(
                                activity.getString(R.string.file_already_existes_overwrite),
                                newPath.getFilenameFromPath())
                        activity.showError(title)
                        return@positiveButton
                    } else {
                        callback(newPath, filename, extension)
                        it.dismiss()
                    }
                }
                .negativeButton(R.string.cancel) {
                    it.dismiss()
                }
                .show()
    }

    private fun getInitialFilename(): String {
        val newFilename = "image_${activity.getCurrentFormattedDateTime()}"
        return if (defaultFilename.isEmpty()) newFilename else defaultFilename
    }


}
