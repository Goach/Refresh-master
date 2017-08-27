package com.goach.refreshlayout.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/7/27 0027
 *Time: 11:11
 */

class PathMeasureHelper{
    private var mPathMeasure = PathMeasure()
    private var mLength:Float = 0f
    private var progress:Float = 0f
    private var mDst = Path()
    private lateinit var mPaint:Paint
    fun initPath(path: Path):PathMeasureHelper{
        mPathMeasure.setPath(path,false)
        mLength = mPathMeasure.length
        return this
    }
    fun initPaint(mPaint:Paint):PathMeasureHelper{
        this.mPaint = mPaint
        return this
    }
    fun progress(progress:Float):PathMeasureHelper{
        this.progress = progress
        return this
    }
    fun resetPath(){
        mDst.reset()
        mDst.lineTo(0f,0f)
    }
    fun drawPath(canvas: Canvas){
        mPathMeasure.getSegment(0f,this.progress*this.mLength,mDst,true)
        canvas.drawPath(mDst,this.mPaint)
    }
    fun getLength():Float{
        return mLength
    }
    fun getProgress():Float{
        return this.progress
    }
}