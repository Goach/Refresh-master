package com.goach.refreshlayout.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.goach.refreshlayout.utils.PathMeasureHelper
import com.goach.refreshlayout.utils.initPaint

/**
 * Created by iGoach on 2017/7/22.
 * 绘制今日头条内部矩形图形
 */
class SquareView: View{
    private var mBorderLinePaint:Paint
    private var mInsidePaint:Paint
    private var mPathMeasureHelper: PathMeasureHelper
    private var mProgress:Float = 0.0f

    constructor(context: Context):this(context,null)
    constructor(context: Context,attributeSet: AttributeSet?):this(context,attributeSet,0)
    constructor(context: Context,attributeSet: AttributeSet?,defaultStyle:Int):super(context,
            attributeSet,defaultStyle){
        mPathMeasureHelper = PathMeasureHelper()
        mBorderLinePaint = Paint()
        mInsidePaint = Paint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPathMeasureHelper.resetPath()
        mPathMeasureHelper.initPaint(mInsidePaint)
        mPathMeasureHelper.drawPath(canvas)
        mPathMeasureHelper.initPaint(mBorderLinePaint)
        mPathMeasureHelper.drawPath(canvas)
    }
    fun setPaint1(color:Int,strokeWidth:Float = 5f){
        mBorderLinePaint.initPaint(color,strokeWidth){this.style = Paint.Style.STROKE}
    }
    fun setPaint2(color:Int,strokeWidth:Float = 5f){
        mInsidePaint.initPaint(color,strokeWidth)
    }
    fun setProgress(progress:Float){
        this.mProgress = progress
        mPathMeasureHelper.progress(progress)
        invalidate()
    }
    fun setPath(path:Path){
        mPathMeasureHelper = mPathMeasureHelper.initPath(path)
    }

}