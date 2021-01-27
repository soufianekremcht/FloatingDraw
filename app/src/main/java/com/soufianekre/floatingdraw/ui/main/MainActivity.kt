package com.soufianekre.floatingdraw.ui.main


import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.data.app_prefs.JPG
import com.soufianekre.floatingdraw.data.app_prefs.PNG
import com.soufianekre.floatingdraw.data.app_prefs.SVG
import com.soufianekre.floatingdraw.extensions.*
import com.soufianekre.floatingdraw.helper.AppHelper
import com.soufianekre.floatingdraw.helper.PermissionsHelper
import com.soufianekre.floatingdraw.helper.StorageHelper
import com.soufianekre.floatingdraw.helper.SvgHelper
import com.soufianekre.floatingdraw.models.FileDirItem
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import com.soufianekre.floatingdraw.ui.save.SaveImageBottomSheet
import com.soufianekre.floatingdraw.ui.settings.SettingsActivity
import com.soufianekre.floatingdraw.ui.views.CanvasListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream


class MainActivity : BaseActivity(), CanvasListener {
    private val PICK_IMAGE_INTENT = 1
    private val SAVE_IMAGE_INTENT = 2

    private val FOLDER_NAME = "images"
    private val FILE_NAME = "simple-draw.png"
    private val BITMAP_PATH = "bitmap_path"
    private val URI_TO_LOAD = "uri_to_load"

    private var defaultPath = ""
    private var defaultFilename = ""
    private var defaultExtension =
            PNG
    private var intentUri: Uri? = null
    private var uriToLoad: Uri? = null
    private var brushColor = Color.BLACK
    private var brushSize = 0f
    private var savedPathsHash = 0L
    private var lastSavePromptTS = 0L
    private var isEraserOn = false
    private var isImageCaptureIntent = false
    private var isEditIntent = false
    private var lastBitmapPath = ""

    private var activity: MainActivity? = null;

    private var onStrokeWidthBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
            SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            brushSize = progress.toFloat()
            updateBrushSize()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this;
        setContentView(R.layout.activity_main)
        setupCanvas()
        checkIntents()
        PermissionsHelper.requestStoragePermissions(this)

    }

    override fun onResume() {
        super.onResume()

        val isShowBrushSizeEnabled = true
        if (isShowBrushSizeEnabled) {
            brush_size_seek_bar.visibility = View.VISIBLE
            brush_size_preview.visibility = View.VISIBLE
        } else {
            brush_size_seek_bar.visibility = View.GONE
            brush_size_preview.visibility = View.GONE
        }

        drawing_canvas.setAllowZooming(true)
        //updateTextColors(main_layout)
        invalidateOptionsMenu()
    }

    override fun onPause() {
        // save brush color + size
        appPrefs().brushSize = brushSize
        appPrefs().brushColor = brushColor

        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        drawing_canvas.mListener = null
    }


    override fun onBackPressed() {
        super.onBackPressed()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_settings -> showSettings()
            R.id.menu_main_clear_canvas -> clearCanvas()
            R.id.menu_main_save -> saveDrawing()
            R.id.menu_main_change_background -> showCanvasBackgroundColorPicker(drawing_canvas)
            R.id.menu_main_open_file -> tryOpenFile()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_IMAGE_INTENT && resultCode == Activity.RESULT_OK &&
                resultData != null && resultData.data != null
        ) {
            tryOpenUri(resultData.data!!, resultData)
            // For Android Q +

        } else if (requestCode == SAVE_IMAGE_INTENT && resultCode == Activity.RESULT_OK && resultData != null
                && resultData.data != null) {
            //
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            if (defaultExtension == SVG) {
                SvgHelper.saveToOutputStream(this, outputStream, drawing_canvas)
            } else {
                saveToOutputStream(outputStream, defaultPath.getCompressionFormat(), false)
            }
            savedPathsHash = drawing_canvas.getDrawingHashCode()
        }
    }

    fun setupCanvas() {

        setCanvasBackgroundColor(Color.WHITE)

        defaultPath = ""
        defaultExtension = ".png"
        brushSize = 30f
        updateBrushSize()

        brush_size_seek_bar.progress = brushSize.toInt()
        brush_size_seek_bar.setOnSeekBarChangeListener(onStrokeWidthBarChangeListener)


        brush_color_preview.setOnClickListener {
            showBrushColorPicker(it)
        }

        undo_img.setOnClickListener { drawing_canvas.undo() }
        redo_img.setOnClickListener { drawing_canvas.redo() }
        eraser_img.setOnClickListener {
            onEraserClicked()
        }

    }

    private fun showBrushColorPicker(view: View) {
        // seconde Picker
        ColorPickerDialog.Builder(this)
                .setTitle("Set your brush color :")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        object : ColorEnvelopeListener {
                            override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                                getBrushSizePreviewBackground().setColor(envelope!!.color)
                                getBrushColorPreviewBackground().setColor(envelope!!.color)
                                brush_size_preview.setColorFilter(envelope.color, PorterDuff.Mode.SRC_IN)
                                drawing_canvas.setColor(envelope.color)
                                //drawing_canvas.setColor(color)
                                /*
                                isEraserOn = false
                                updateEraserState()
                                 */
                            }

                        })
                .setNegativeButton(getString(R.string.cancel),
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .attachAlphaSlideBar(true) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
    }

    private fun showCanvasBackgroundColorPicker(view: View) {

        ColorPickerDialog.Builder(this)
                .setTitle("Set the Background Color :")

                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        object : ColorEnvelopeListener {
                            override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                                setCanvasBackgroundColor(envelope!!.color)
                                //drawing_canvas.setColor(color)
                                /*
                                isEraserOn = false
                                updateEraserState()

                                 */
                            }

                        })
                .setNegativeButton(getString(R.string.cancel),
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .attachAlphaSlideBar(true) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
    }

    public fun setCanvasBackgroundColor(color: Int) {
        val contrastColor = color.getContrastColor()
        undo_img.setColorFilter(contrastColor, PorterDuff.Mode.SRC_IN)
        eraser_img.setColorFilter(contrastColor, PorterDuff.Mode.SRC_IN)
        redo_img.setColorFilter(contrastColor, PorterDuff.Mode.SRC_IN)

        drawing_canvas.updateBackgroundColor(color)
        drawing_canvas.setColor(color.getContrastColor());
        defaultExtension = PNG
        getBrushSizePreviewBackground().setStroke(getBrushStrokeSize(), contrastColor)
    }


    // update eraser state
    private fun onEraserClicked() {
        isEraserOn = !isEraserOn
        drawing_canvas.toggleEraser(isEraserOn)
        updateEraserState()
    }

    private fun updateEraserState() {
        eraser_img.setImageDrawable(
                ContextCompat.getDrawable(
                        this, if (isEraserOn)
                    R.drawable.ic_eraser_off_black_24 else R.drawable.ic_eraser_black_24
                )
        )
    }


    private fun updateBrushSize() {
        drawing_canvas.setBrushSize(brushSize)
        val scale = Math.max(0.03f, brushSize / 100f)
        brush_size_preview.scaleX = scale
        brush_size_preview.scaleY = scale

    }


    private fun getBrushSizePreviewBackground() = brush_size_preview.background as GradientDrawable
    private fun getBrushColorPreviewBackground() = brush_color_preview.background as GradientDrawable

    private fun getBrushStrokeSize() = resources.getDimension(R.dimen.preview_dot_stroke_size).toInt()


    private fun getStoragePermission(callback: () -> Unit) {
        PermissionsHelper.handleStoragePermissions(this) {
            if (it) {
                callback()
            } else {
                showError(getString(R.string.no_storage_permissions))
            }
        }
    }


    private fun saveDrawing(): Boolean {
        trySaveImage()
        return true
    }

    private fun clearCanvas(): Boolean {
        drawing_canvas.clearCanvas()
        return true
    }


    private fun checkIntents() {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            tryOpenUri(uri!!, intent)
        }
        if (intent?.action == Intent.ACTION_SEND_MULTIPLE && intent.type?.startsWith("image/") == true) {
            val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            imageUris?.any { tryOpenUri(it, intent) }
        }
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            tryOpenUri(intent.data!!, intent)
        }
        if (intent?.action == MediaStore.ACTION_IMAGE_CAPTURE) {
            val output = intent.extras?.get(MediaStore.EXTRA_OUTPUT)
            if (output != null && output is Uri) {
                isImageCaptureIntent = true
                intentUri = output
                defaultPath = output.path!!
                invalidateOptionsMenu()
            }
        }
        if (intent?.action == Intent.ACTION_EDIT) {
            val data = intent.data
            val output = intent.extras?.get(MediaStore.EXTRA_OUTPUT)
            if (data != null && output != null && output is Uri) {
                tryOpenUri(data, intent)
                isEditIntent = true
                intentUri = output
            }
        }
    }

    private fun tryOpenFile() {
        // traditional way
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE_INTENT)
        }

        // using material dialogs

        /*MaterialDialog(this).show {
            fileChooser(activity!!.baseContext) { materialDialog: MaterialDialog, file: File ->
                tryOpenUri(file.toUri(),)
            }
        }*/
    }

    private fun tryOpenUri(uri: Uri, intent: Intent) = when {
        uri.scheme == "file" -> {
            uriToLoad = uri
            openPath(uri.path!!)
        }
        uri.scheme == "content" -> {
            uriToLoad = uri
            openUri(uri, intent)
        }
        else -> false
    }

    private fun openPath(path: String) = when {
        path.endsWith(".svg") -> {
            drawing_canvas.mBackgroundBitmap = null
            SvgHelper.loadSvg(this, File(path), drawing_canvas)
            defaultExtension = SVG
            true
        }
        File(path).isImageSlow() -> {
            lastBitmapPath = path
            drawing_canvas.drawBitmap(this, path)
            defaultExtension = JPG
            true
        }
        else -> {
            showInfo(getString(R.string.invalid_file_format))
            false
        }

    }

    private fun openUri(uri: Uri, intent: Intent): Boolean {
        val mime = MimeTypeMap.getSingleton()
        val type = mime.getExtensionFromMimeType(contentResolver.getType(uri)) ?: intent.type
        ?: contentResolver.getType(uri)
        return when (type) {
            "svg", "image/svg+xml" -> {
                drawing_canvas.mBackgroundBitmap = null
                SvgHelper.loadSvg(this, uri, drawing_canvas)
                defaultExtension = SVG
                true
            }
            "jpg", "jpeg", "png", "gif", "image/jpg", "image/png", "image/gif" -> {
                drawing_canvas.drawBitmap(this, uri)
                defaultExtension = JPG
                true
            }
            else -> {
                showInfo(getString(R.string.invalid_file_format))
                false
            }
        }

    }


    private fun saveToOutputStream(
            outputStream: OutputStream?, format: Bitmap.CompressFormat,
            finishAfterSaving: Boolean) {
        if (outputStream == null) {
            showError(getString(R.string.unknown_error_occurred))
            return
        }
        outputStream.use {
            drawing_canvas.getBitmap().compress(format, 70, it)
        }
        if (finishAfterSaving) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun trySaveImage() {
        getStoragePermission {
            if (AppHelper.hasSdkHigherThan(Build.VERSION_CODES.Q)) {
                SaveImageBottomSheet(
                        this,
                        defaultPath,
                        defaultFilename,
                        defaultExtension,
                        false
                ) { _, filename, extension ->
                    val mimeType = if (extension == SVG) "svg+xml" else extension
                    defaultFilename = filename
                    defaultExtension = extension
                    appPrefs().lastSaveExtension = extension

                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = "image/$mimeType"
                        putExtra(Intent.EXTRA_TITLE, "$filename.$extension")
                        addCategory(Intent.CATEGORY_OPENABLE)
                        startActivityForResult(this, SAVE_IMAGE_INTENT)
                    }
                    startActivityForResult(intent, SAVE_IMAGE_INTENT)
                }
            } else {
                openSaveImageDialog()
            }
        }
    }


    private fun openSaveImageDialog() {
        SaveImageBottomSheet(this, defaultPath, defaultFilename, defaultExtension, false) { fullPath, filename, extension ->
            savedPathsHash = drawing_canvas.getDrawingHashCode()
            saveFile(fullPath)
            defaultPath = fullPath.getParentPath()
            defaultFilename = filename
            defaultExtension = extension
            appPrefs().lastSaveFolder = defaultPath
            appPrefs().lastSaveExtension = extension
        }
    }

    private fun saveFile(path: String) {
        when (path.getFilenameExtension()) {
            SVG -> saveAsSvgFile(path)
            else -> saveAsImageFile(path)
        }

    }

    private fun saveAsSvgFile(path: String) {
        var isImageSaved = SvgHelper.saveSvgForAndroidO(this, path, drawing_canvas)
        if (isImageSaved)
            showSuccess("Image has been saved in : " + path)
        else
            showError("Unable to save Image.")
    }


    private fun saveAsImageFile(path: String) {
        val fileDirItem = FileDirItem(path, path.getFilenameFromPath())
        GlobalScope.launch(Dispatchers.Main) {
            val isImageSaved: Boolean = StorageHelper.saveImage(baseContext, drawing_canvas.getBitmap(), fileDirItem)
            if (isImageSaved)
                showSuccess("Image has been saved in : " + path)
            else
                showError("Unable to Save Image. ")

        }

    }


    override fun toggleUndoVisibility(visible: Boolean) {
        if (visible) undo_img.visibility = View.VISIBLE
        else undo_img.visibility = View.GONE
    }

    override fun toggleRedoVisibility(visible: Boolean) {
        if (visible) redo_img.visibility = View.VISIBLE
        else redo_img.visibility = View.GONE
    }

    fun showSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BITMAP_PATH, lastBitmapPath)

        if (uriToLoad != null) {
            outState.putString(URI_TO_LOAD, uriToLoad.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        lastBitmapPath = savedInstanceState.getString(BITMAP_PATH)!!
        if (lastBitmapPath.isNotEmpty()) {
            openPath(lastBitmapPath)
        } else if (savedInstanceState.containsKey(URI_TO_LOAD)) {
            uriToLoad = Uri.parse(savedInstanceState.getString(URI_TO_LOAD))
            tryOpenUri(uriToLoad!!, intent)
        }
    }
}
