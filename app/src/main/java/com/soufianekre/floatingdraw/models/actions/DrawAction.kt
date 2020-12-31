package com.soufianekre.floatingdraw.models.actions

import android.graphics.Path
import java.io.Serializable
import java.io.Writer

interface DrawAction : Serializable {
    fun perform(path: Path)

    fun perform(writer: Writer)
}
