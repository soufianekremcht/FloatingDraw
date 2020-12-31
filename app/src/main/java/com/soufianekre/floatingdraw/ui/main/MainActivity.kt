package com.soufianekre.floatingdraw.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.extensions.*
import com.soufianekre.floatingdraw.extensions.file.FileDirItem
import com.soufianekre.floatingdraw.helper.*
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import com.soufianekre.floatingdraw.ui.save_image.SaveImageDialog
import com.soufianekre.floatingdraw.ui.settings.SettingsActivity
import com.soufianekre.floatingdraw.ui.views.CanvasListener
import kotlinx.android.synthetic.main.activity_main.*
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.lang.Exception


class MainActivity : BaseActivity(),CanvasListener {
    private val PICK_IMAGE_INTENT = 1
    private val SAVE_IMAGE_INTENT = 2

    private val FOLDER_NAME = "images"
    private val FILE_NAME = "simple-draw.png"
    private val BITMAP_PATH = "bitmap_path"
    private val URI_TO_LOAD = "uri_to_load"

    private var defaultPath = ""
    private var defaultFilename = ""
    private var defaultExtension = PNG
    private var intentUri: Uri? = null
    private var uriToLoad: Uri? = null
    private var color = 0
    private var brushSize = 0f
    private var savedPathsHash = 0L
    private var lastSavePromptTS = 0L
    private var isEraserOn = false
    private var isImageCaptureIntent = false
    private var isEditIntent = false
    private var lastBitmapPath = ""

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
        setContentView(R.layout.activity_main)

        setupCanvas()
        checkIntents()

    }
    override fun onResume() {
        super.onResume()

        val isShowBrushSizeEnabled = true
        if (isShowBrushSizeEnabled){
            stroke_width_bar.visibility = View.VISIBLE
            stroke_width_preview.visibility = View.VISIBLE
        }else{
            stroke_width_bar.visibility = View.GONE
            stroke_width_preview.visibility = View.GONE
        }

        drawing_canvas.setAllowZooming(true)
        //updateTextColors(main_layout)
        invalidateOptionsMenu()
    }

    override fun onPause() {
        // save brush color + size

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
        menuInflater.inflate(R.menu.main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_main_clear_canvas -> clearCanvas()
            R.id.menu_main_save -> saveDrawing()
            else ->  return super.onOptionsItemSelected(item)
        }
        return true
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_IMAGE_INTENT && resultCode == Activity.RESULT_OK &&
            resultData != null && resultData.data != null){
            tryOpenUri(resultData.data!!,resultData)

        } else if (requestCode == SAVE_IMAGE_INTENT && resultCode == Activity.RESULT_OK && resultData != null
        && resultData.data != null){
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            if (defaultExtension  == SVG){
                SvgHelper.saveToOutputStream(this,outputStream,drawing_canvas)
            }else{
                saveToOutputStream(outputStream,defaultPath.getCompressionFormat(),false)
            }
            savedPathsHash = drawing_canvas.getDrawingHashCode()
        }
    }


    fun showSettings(){
        startActivity(Intent(this, SettingsActivity::class.java))
    }


    fun setupCanvas(){
        setCanvasBackgroundColor(Color.WHITE)
        setBrushColor(ContextCompat.getColor(this,R.color.colorPrimary))
        defaultPath=""
        defaultExtension = ".png"
        brushSize = 30f
        updateBrushSize()
        stroke_width_bar.progress = brushSize.toInt()

        brush_color_preview.setOnClickListener{
            showColorPicker(it)
        }
        undo_img.setOnClickListener{ drawing_canvas.undo() }
        redo_img.setOnClickListener{ drawing_canvas.redo() }
        eraser_img.setOnClickListener{
            onEraserClicked()
        }

    }

    private fun showColorPicker(view : View) {
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(true) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(true)
            .build()
            .show(view, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    setCanvasBackgroundColor(color)
                }

                fun onColor(color: Int, fromUser: Boolean) {}
            })
    }

    private fun onEraserClicked() {
        isEraserOn = !isEraserOn
        updateEraserState()
    }
    private fun updateEraserState(){
        eraser_img.setImageDrawable(ContextCompat.getDrawable(this,if (isEraserOn)
            R.drawable.ic_eraser_black_24 else R.drawable.ic_eraser_off_black_24))
    }


    private fun updateBrushSize() {
        drawing_canvas.setBrushSize(brushSize)
        val scale = Math.max(0.03f,brushSize/100f)
        stroke_width_preview.scaleX =scale
        stroke_width_preview.scaleY = scale
    }

    private fun setBrushColor(color :Int) {
        TODO("Not yet implemented")
    }

    public fun setCanvasBackgroundColor(color :Int){
        val contrastColor = color.getContrastColor()
        undo_img.setColorFilter(contrastColor,PorterDuff.Mode.SRC_IN)
        eraser_img.setColorFilter(contrastColor,PorterDuff.Mode.SRC_IN)
        redo_img.setColorFilter(contrastColor,PorterDuff.Mode.SRC_IN)

        drawing_canvas.updateBackgroundColor(color)
        defaultExtension= PNG
        getBrushPreviewView().setStroke(getBrushStrokeSize(),contrastColor)


    }

    private fun getBrushPreviewView() = stroke_width_preview.background as GradientDrawable

    private fun getBrushStrokeSize() = resources.getDimension(R.dimen.preview_dot_stroke_size).toInt()

    private fun saveDrawing() :Boolean{

        return true
    }

    private fun clearCanvas() :Boolean{

        return false
    }



    private fun checkIntents(){
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true){
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            tryOpenFile(uri,intent)
        }
        if (intent?.action == Intent.ACTION_SEND_MULTIPLE && intent.type?.startsWith("image/") == true){
            val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            imageUris.any{ tryOpenUri(it,intent)}
        }
        if (intent?.action == Intent.ACTION_VIEW && intent.data !=null){
            tryOpenUri(intent.data!!,intent)
        }
        if (intent?.action == MediaStore.ACTION_IMAGE_CAPTURE){
            val output = intent.extras?.get(MediaStore.EXTRA_OUTPUT)
            if (output != null && output is Uri){
                isImageCaptureIntent = true
                intentUri = output
                defaultPath = output.path!!
                invalidateOptionsMenu()
            }
        }
        if (intent?.action == Intent.ACTION_EDIT){
            val data = intent.data
            val output = intent.extras?.get(MediaStore.EXTRA_OUTPUT)
            if (data != null && output != null && output is Uri){
                tryOpenUri(data,intent)
                isEditIntent = true
                intentUri = output
            }
        }
    }

    private fun tryOpenUri(uri : Uri,intent: Intent) = when{
        uri.scheme == "file" ->{
            uriToLoad = uri
            openPath(uri.path!!)
        }
        uri.scheme == "content" ->{
            uriToLoad = uri
            openUri(uri,intent)
        }
        else -> false
    }

    private fun openPath(path : String) = when {
        path.endsWith(".svg") -> {
            drawing_canvas.mBackgroundBitmap = null
            SvgHelper.loadSvg(this, File(path),drawing_canvas)
            defaultExtension = SVG
            true
        }
        File(path).isImageSlow() ->{
            lastBitmapPath = path
            drawing_canvas.drawBitmap(this,path)
            defaultExtension = JPG
            true
        }
        else ->{
            showInfo(getString(R.string.invalid_file_format))
            false
        }

    }

    private fun openUri(uri: Uri,intent:Intent) : Boolean{
        val mime = MimeTypeMap.getSingleton()
        val type = mime.getExtensionFromMimeType(contentResolver.getType(uri)) ?: intent.type ?: contentResolver.getType(uri)
        return when (type) {
            "svg","image/svg+xml" ->{
                drawing_canvas.mBackgroundBitmap = null
                SvgHelper.loadSvg(this,uri,drawing_canvas)
                defaultExtension = SVG
                true
            }
            "jpg","jpeg","png","gif","image/jpg","image/png","image/gif" ->{
                drawing_canvas.drawBitmap(this,uri)
                defaultExtension = JPG
                true
            }
            else -> {
                showInfo(getString(R.string.invalid_file_format))
                false
            }
        }

    }

    private fun writeToOutputStream(path:String,out:OutputStream){
        out.use {
            drawing_canvas.getBitmap().compress(path.getCompressionFormat(),70,out)
        }
    }


    private fun saveToOutputStream(outputStream: OutputStream?, format: Bitmap.CompressFormat,
                                   finishAfterSaving : Boolean){
        if (outputStream == null){
            showError(getString(R.string.unknown_error_occurred))
            return
        }
        outputStream.use{
            drawing_canvas.getBitmap().compress(format,70,it)
        }
        if (finishAfterSaving){
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    /***** Menu Actions *****/
    private fun confirmImage(){
        when{
            isEditIntent ->{
                try{
                    val outputStream = contentResolver.openOutputStream(intentUri!!)
                    saveToOutputStream(outputStream,defaultPath.getCompressionFormat(),true)

                }catch (e : Exception){
                    showError(e.localizedMessage)
                }
            }
            intentUri?.scheme == "content" ->{
                val outputStream = contentResolver.openOutputStream(intentUri!!)
                saveToOutputStream(outputStream,defaultPath.getCompressionFormat(),true)
            }
            else ->{
                PermissionsHelper.checkStoragePermissions(this);
                val fileDirItem = FileDirItem(defaultPath,defaultPath.getFilenameFromPath())
                getFileOutputStream(fileDirItem,true){
                    saveToOutputStream(it,defaultPath.getCompressionFormat(),true)
                }
            }
        }
    }

    private fun trySaveImage(){
        if (isQPlus()){
            SaveImageDialog(this,defaultPath,defaultFilename,defaultExtension,true){
                fullPath,filename,extension->
                val mimeType = if (extension == SVG )"svg+xml" else extension)
                defaultFilename = filename
                defaultExtension = extension
                appPrefs().lastSaveExtension = extension
                val intent = Intent(Intent.EXTRA_TITLE,"$filename.$extension")
                addCategory(Intent.CATEGORY_OPENABLE)

                startActivityForResult(intent,SAVE_IMAGE_INTENT)


            }
        }
    }

    private fun shareImage(){

    }

    private fun tryOpenFile(uri: Uri?, intent: Intent) {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type ="image/*"
            startActivityForResult(this,PICK_IMAGE_INTENT)
        }
    }

    private fun saveImage(){

    }
    private fun saveFile(path:String){

    }
    private fun saveImageFile(path:String){

    }


    private fun getImagePath(bitmap:Bitmap,callback:(path:String?)-> Unit){
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,0,bytes)

        val folder = File(cacheDir,FOLDER_NAME)
        if (!folder.exists()){
            if (!folder.mkdir()){
                callback(null)
                return
            }
        }
        val newPath = "$folder/$FILE_NAME"
        val fileDirItem = FileDirItem(newPath,FILE_NAME)
        getFileOutputStream(fileDirItem,true){
            if (it != null){
                try {
                    it.write(bytes.toByteArray())
                    callback(newPath)
                }catch(e :Exception){

                }finally {
                    it.close()
                }
            }else{
                callback("")
            }
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
}