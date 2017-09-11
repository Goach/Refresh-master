package com.goach.refreshlayout.utils

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Build.VERSION.SDK_INT
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/9/1 0001
 *Time: 16:50
 */
object ScrollBoundaryUtil {
    fun canRefresh(targetView: View,event:MotionEvent?):Boolean{
        if(canRefresh(targetView)){
            return false
        }
        if(targetView is ViewGroup && event!=null){
            val childCount = targetView.childCount
            val pointF = PointF()
            for(i in childCount -1 .. 0){
                val childView = targetView.getChildAt(i)
                if(isTransformedTouchPointInView(targetView,childView,event.x,event.y,pointF)){
                   val  newEvent = MotionEvent.obtain(event)
                    newEvent.offsetLocation(pointF.x,pointF.y)
                    return canRefresh(targetView, newEvent)
                }
            }
        }
        return true
    }

    @SuppressLint("ObsoleteSdkInt")
    fun canRefresh(targetView: View):Boolean{
        if(SDK_INT < 14){
            if(targetView is AbsListView){
                return targetView.childCount > 0 &&( targetView.firstVisiblePosition > 0 ||
                        targetView.getChildAt(0).top < targetView.paddingTop)
            }else {
                return targetView.scrollY > 0
            }
        }else{
            return targetView.canScrollVertically(-1)
        }
    }
    fun pointInView(view:View,localX:Float, localY: Float,slop:Float):Boolean{
        val left = - slop
        val top = - slop
        val width = view.width
        val height = view.height
        return localX >= left && localY >= top && localX <= (width + slop) && localY <= (height + top)
    }
    fun isTransformedTouchPointInView(group: ViewGroup,child:View,x:Float,y:Float,outLocalPoint:PointF?):Boolean{
        val point = FloatArray(2)
        point[0] = x
        point[1] = y
        transformPointToViewLocal(group,child,point)
        val isInView = pointInView(child,point[0],point[1],0f)
        if(isInView && outLocalPoint != null){
            outLocalPoint.set(point[0] - x ,point[1] - y)
        }
        return isInView
    }

    fun transformPointToViewLocal(group: ViewGroup, child: View, point: FloatArray) {
        point[0] += (group.scrollX - child.left).toFloat()
        point[1] += (group.scrollY - child.top).toFloat()
    }
}