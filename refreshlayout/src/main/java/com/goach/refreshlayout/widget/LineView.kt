package com.goach.refreshlayout.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.goach.refreshlayout.utils.PathMeasureHelper
import com.goach.refreshlayout.utils.cling
import com.goach.refreshlayout.utils.initPaint

/**
 * Created by iGoach on 2017/7/22.
 * 绘制今日头条三根直线
 */
class LineView : View{
    private var mLinePaint:Paint
    private var mPath1MeasureHelper:PathMeasureHelper
    private var mPath2MeasureHelper:PathMeasureHelper
    private var mPath3MeasureHelper:PathMeasureHelper
    private var mProgress:Float = 0.0f
    private var mCount = 3

    constructor(context: Context):this(context,null)
    constructor(context: Context,attributeSet: AttributeSet?):this(context,attributeSet,0)
    constructor(context: Context,attributeSet: AttributeSet?,defaultStyle:Int):super(context,
            attributeSet,defaultStyle){
        mLinePaint = Paint().initPaint(Color.BLACK,5f){this.style = Paint.Style.STROKE}
        mPath1MeasureHelper = PathMeasureHelper().initPaint(mLinePaint)
        mPath2MeasureHelper = PathMeasureHelper().initPaint(mLinePaint)
        mPath3MeasureHelper = PathMeasureHelper().initPaint(mLinePaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPath1MeasureHelper.resetPath()
        mPath2MeasureHelper.resetPath()
        mPath3MeasureHelper.resetPath()
        mPath1MeasureHelper.drawPath(canvas)
        mPath2MeasureHelper.drawPath(canvas)
        mPath3MeasureHelper.drawPath(canvas)
    }
    fun setPaint(color:Int,strokeWidth:Float = 5f){
        mLinePaint.initPaint(color,strokeWidth){this.style = Paint.Style.STROKE}
    }
    fun setProgress(progress:Float){
        this.mProgress = progress
        this.mPath1MeasureHelper.progress(context.cling(0f,1f,progress*mCount))
        this.mPath2MeasureHelper.progress(context.cling(0f,1f, progress*mCount- 1.0f))
        this.mPath3MeasureHelper.progress(context.cling(0f,1f, progress*mCount- 2.0f))
        invalidate()
    }
    fun path1Line(path:Path){
        mPath1MeasureHelper.initPath(path)
    }
    fun path2Line(path:Path){
        mPath2MeasureHelper.initPath(path)
    }
    fun path3Line(path:Path){
        mPath3MeasureHelper.initPath(path)
    }
}