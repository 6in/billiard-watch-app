package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*


/**
 * TODO: document your custom view class.
 */
class DirectionView : View {

    private var thread: Thread? = null
    private var direction = 90.0f

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var w  = this.width * 1.0f
        var h  = this.height * 1.0f

        var cx = w / 2.0f
        var cy = h / 2.0f

        var color = Paint()
        color.textSize = h / 2.0f * 0.6f
        color.setTextAlign(Paint.Align.CENTER)
        color.color = Color.WHITE
        // 横線
        canvas.drawLine(0f,h / 2, w, h / 2, color)
        // 縦線
        canvas.drawLine(w / 2.0f,0f, w / 2.0f, h, color)

        var dirctionLocal = abs(this.direction - 90.0f)

        // 角度計算
        var text = String.format("%2.0f", dirctionLocal) + "°"

        // 厚みを計算
        var cbWidth = this.calcWidth()
        var text2 = String.format("%2.0f", cbWidth * 100.0) + "%"

        //
        var thicknessPaint = Paint()
        thicknessPaint.color = Color.LTGRAY

//        thicknessPaint.alpha = 90
        var thicknessWidth = (cbWidth * w).toFloat()

        var backGround = Paint()
        backGround.color = Color.rgb(255,255,255)
        var startAlpha = 0
        var startAngle = 255f
        for ( i in  0..4) {
            startAlpha = if ( i % 2 == 0) { 20 } else {40}
            startAngle += 15f
            backGround.alpha = startAlpha
            canvas.drawArc( RectF(0f,0f,w,h) , startAngle  , 15f , true , backGround)
        }
        startAngle = 180f
        startAlpha = 120
        for ( i in  0..4) {
            startAlpha = if ( i % 2 == 1) { 20 } else {40}
            startAngle += 15f
            backGround.alpha = startAlpha
            canvas.drawArc( RectF(0f,0f,w,h) , startAngle  , 15f , true , backGround)
        }

        var underDeg = Paint()
        underDeg.color = Color.rgb(128,128,128)
        underDeg.alpha = 90
        if (this.direction > 90) {
            canvas.drawArc( RectF(0f,0f,w,h) , 270 - dirctionLocal  , dirctionLocal , true , thicknessPaint)
            canvas.drawRect(0.0f,h / 2.0f, thicknessWidth, h , thicknessPaint)
            canvas.drawArc( RectF(0f,0f,w,h) , 90 - dirctionLocal  , dirctionLocal , true , underDeg)
        } else {
            canvas.drawArc( RectF(0f,0f,w,h) , 270f , dirctionLocal , true , thicknessPaint)
            canvas.drawRect(w - thicknessWidth,h / 2.0f, w, h , thicknessPaint)
            canvas.drawArc( RectF(0f,0f,w,h) , 90f ,    dirctionLocal , true , underDeg)
        }

        // 角度
        canvas.drawText(text, w / 2, h / 4 + h / 8, color)

        // 厚み
        canvas.drawText(text2, w / 2,h / 2 + h / 4 + h / 8, color)
    }

    private fun calcWidth() : Double {
        var r = 10.0
        var width = 0.0

        var deg = this.direction + 180.0
        // CPの位置を取得
        var gbCx = r * cos( Math.toRadians(deg) )

        if (gbCx > 0.0) {
            width = abs(r - gbCx) * 2.0
        } else {
            width = abs(-r - gbCx) * 2.0
        }

        if (width > 0.0) {
            width /= (r * 2.0)
        }

        return width
    }

    private var stopThread = false

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var me = this

        var eventX = event!!.x
        var eventY = event!!.y
        var w = this.width * 1.0f
        var h = this.height * 1.0f
        var directionDelta = 0.0f


        // 左 (0-33%)
        // リセット (34-66%)
        // 右 (67-100%)

        var pushed = eventX / w * 100

        if (pushed in 0.0..33.0) {
            directionDelta = 1.0f
        }
        if (pushed in 34.0..66.0) {
            me.stopThread = true
            if (me.thread != null) {
                me.thread?.join()
                me.thread = null
            }
            me.direction = 90.0f
            me.postInvalidate()
            return true
        }
        if (pushed in 66.0..100.0) {
            directionDelta = -1.0f
        }

        if (event.action == MotionEvent.ACTION_DOWN && me.thread == null) {

            me.stopThread = false
            var timeout : Long = 100
            me.thread = Thread {
                while(!me.stopThread) {
                    me.direction += directionDelta

                    if (me.direction <= 10.0f) {
                        me.direction = 10.0f
                        me.stopThread = true
                    }

                    if (me.direction >= 170.0f) {
                        me.direction = 170.0f
                        me.stopThread = true
                    }
                    me.postInvalidate()
                    Thread.sleep(timeout)
                    timeout = 20
                }
                me.thread = null
                me.stopThread = false
            }
            me.thread?.start()
        }

        if (event.action == MotionEvent.ACTION_UP) {
            me.stopThread = true
            me.thread?.join()
            me.thread = null
            me.postInvalidate()
        }

        return true
    }
}
