package com.goach.refreshlayout.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

import com.goach.refreshlayout.R

/**
 * 来源Titanic
 */
class TitanicTextView : AppCompatTextView {

    interface AnimationSetupCallback {
        fun onSetupAnimation(titanicTextView: TitanicTextView)
    }

    var animationSetupCallback: AnimationSetupCallback? = null
    var maskX: Float = 0.toFloat()
        set(maskX) {
            field = maskX
            invalidate()
        }
    var maskY: Float = 0.toFloat()
        set(maskY) {
            field = maskY
            invalidate()
        }
    var isSinking: Boolean = false
    var isSetUp: Boolean = false
        private set

    private var shader: BitmapShader? = null
    private var shaderMatrix: Matrix? = null
    private var wave: Drawable? = null
    private var offsetY: Float = 0.toFloat()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        shaderMatrix = Matrix()
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        createShader()
    }

    override fun setTextColor(colors: ColorStateList) {
        super.setTextColor(colors)
        createShader()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        createShader()

        if (!isSetUp) {
            isSetUp = true
            if (animationSetupCallback != null) {
                animationSetupCallback!!.onSetupAnimation(this@TitanicTextView)
            }
        }
    }

    private fun createShader() {

        if (wave == null) {
            wave = ContextCompat.getDrawable(context,R.drawable.wave)
        }

        val waveW = wave!!.intrinsicWidth
        val waveH = wave!!.intrinsicHeight

        val b = Bitmap.createBitmap(waveW, waveH, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)

        c.drawColor(currentTextColor)

        wave!!.setBounds(0, 0, waveW, waveH)
        wave!!.draw(c)

        shader = BitmapShader(b, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        paint.shader = shader

        offsetY = ((height - waveH) / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {

        // modify text paint shader according to sinking state
        if (isSinking && shader != null) {

            // first call after sinking, assign it to our paint
            if (paint.shader == null) {
                paint.shader = shader
            }

            // translate shader accordingly to maskX maskY positions
            // maskY is affected by the offset to vertically center the wave
            shaderMatrix!!.setTranslate(this.maskX, this.maskY + offsetY)

            // assign matrix to invalidate the shader
            shader!!.setLocalMatrix(shaderMatrix)
        } else {
            paint.shader = null
        }

        super.onDraw(canvas)
    }
}
