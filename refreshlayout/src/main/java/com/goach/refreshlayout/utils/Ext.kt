package com.goach.refreshlayout.utils

import android.content.Context
import android.graphics.Paint

/**
 * Created by iGoach on 2017/7/22.
 *
 */

fun Paint.initPaint(color:Int,strokeWidth:Float = 2f,init:(Paint.() -> Unit)? = null):Paint{
    this.flags = Paint.ANTI_ALIAS_FLAG
    this.isAntiAlias = true
    this.color = color
    this.strokeWidth = strokeWidth
    if(init!=null)
        this.init()
    return this
}

fun Context.cling(min:Float,max:Float,value:Float):Float{
    return Math.max(min,Math.min(max,value))
}
fun Context.cling(min:Int,max:Int,value:Int):Int{
    return Math.max(min,Math.min(max,value))
}
