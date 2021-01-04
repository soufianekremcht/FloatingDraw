package com.soufianekre.floatingdraw.ui.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.SeekBar

class MySeekBar : androidx.appcompat.widget.AppCompatSeekBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {

        progressDrawable.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        thumb.setColorFilter(accentColor,PorterDuff.Mode.SRC_IN)

    }
}