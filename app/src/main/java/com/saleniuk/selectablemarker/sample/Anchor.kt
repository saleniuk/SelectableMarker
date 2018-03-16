package com.saleniuk.selectablemarker.sample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.util.TypedValue


class Anchor : View {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var desiredWidth = 1
        var desiredHeight = 1

        desiredWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, desiredWidth.toFloat(), resources.displayMetrics).toInt()
        desiredHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, desiredHeight.toFloat(), resources.displayMetrics).toInt()

        super.onMeasure(desiredWidth, desiredHeight)
        setMeasuredDimension(desiredWidth, desiredHeight)
    }
}