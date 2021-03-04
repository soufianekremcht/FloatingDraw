package com.soufianekre.floatingdraw.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class DrawingImage(val id: Long,
                         val uri: Uri,
                         val path: String,
                         val name: String,
                         val size: String,
                         val width: String?,
                         val height: String?,
                         val date: String) :
        Parcelable