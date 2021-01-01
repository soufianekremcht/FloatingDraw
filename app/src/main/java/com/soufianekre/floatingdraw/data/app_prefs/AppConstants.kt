package com.soufianekre.floatingdraw.data.app_prefs

import android.os.Build

const val PNG = "png"
const val SVG = "svg"
const val JPG = "jpg"

const val BRUSH_COLOR = "brush_color"
const val CANVAS_BACKGROUND_COLOR = "canvas_background_color"
const val SHOW_BRUSH_SIZE = "show_brush_size"
const val BRUSH_SIZE = "brush_size_2"
const val LAST_SAVE_FOLDER = "last_save_folder"
const val LAST_SAVE_EXTENSION = "last_save_extension"
const val ALLOW_ZOOMING_CANVAS = "allow_zooming_canvas"
const val FORCE_PORTRAIT_MODE = "force_portrait_mode"


const val NOMEDIA :String = "no_media"
const val MD5 : String = "md5"


const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val SD_CARD_PATH = "sd_card_path"
const val TREE_URI = "tree_uri"

fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


val photoExtensions: Array<String> get() = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif")
val videoExtensions: Array<String> get() = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
val audioExtensions: Array<String> get() = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")
val rawExtensions: Array<String> get() = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")


// sorting
const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX = "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32
const val SORT_BY_NUMBER = 64
const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024
const val SORT_BY_TITLE = 2048
const val SORT_BY_ARTIST = 4096
const val SORT_BY_DURATION = 8192
const val SORT_BY_RANDOM = 16384
const val SORT_USE_NUMERIC_VALUE = 32768
const val SORT_BY_FULL_NAME = 65536
const val SORT_BY_CUSTOM = 131072


// String
val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()