package com.soufianekre.floatingdraw.data.app_prefs

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.helper.*

const val PREF_NAME: String = "drawing_pref"

class AppPreferences(var context: Context) {

    private var prefs : SharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);


    companion object {
        fun newInstance(context: Context) = AppPreferences(context)
    }

    var showBrushSize: Boolean
        get() = prefs.getBoolean(SHOW_BRUSH_SIZE, true)
        set(showBrushSize) = prefs.edit().putBoolean(SHOW_BRUSH_SIZE, showBrushSize).apply()

    var brushColor: Int
        get() = prefs.getInt(BRUSH_COLOR, ContextCompat.getColor(context,R.color.colorPrimary))
        set(color) = prefs.edit().putInt(BRUSH_COLOR, color).apply()

    var brushSize: Float
        get() = prefs.getFloat(BRUSH_SIZE, 50f)
        set(brushSize) = prefs.edit().putFloat(BRUSH_SIZE, brushSize).apply()

    var canvasBackgroundColor: Int
        get() = prefs.getInt(CANVAS_BACKGROUND_COLOR, Color.WHITE)
        set(canvasBackgroundColor) = prefs.edit().putInt(CANVAS_BACKGROUND_COLOR, canvasBackgroundColor).apply()

    var lastSaveFolder: String
        get() = prefs.getString(LAST_SAVE_FOLDER, "")!!
        set(lastSaveFolder) = prefs.edit().putString(LAST_SAVE_FOLDER, lastSaveFolder).apply()

    var lastSaveExtension: String
        get() = prefs.getString(LAST_SAVE_EXTENSION, "")!!
        set(lastSaveExtension) = prefs.edit().putString(LAST_SAVE_EXTENSION, lastSaveExtension).apply()

    var allowZoomingCanvas: Boolean
        get() = prefs.getBoolean(ALLOW_ZOOMING_CANVAS, false)
        set(allowZoomingCanvas) = prefs.edit().putBoolean(ALLOW_ZOOMING_CANVAS, allowZoomingCanvas).apply()

    var forcePortraitMode: Boolean
        get() = prefs.getBoolean(FORCE_PORTRAIT_MODE, false)
        set(forcePortraitMode) = prefs.edit().putBoolean(FORCE_PORTRAIT_MODE, forcePortraitMode).apply()
}