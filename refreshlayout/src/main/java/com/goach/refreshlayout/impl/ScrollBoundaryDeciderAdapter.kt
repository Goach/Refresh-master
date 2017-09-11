package com.goach.refreshlayout.impl

import android.view.MotionEvent
import android.view.View
import com.goach.refreshlayout.api.ScrollBoundaryDecider

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/9/1 0001
 *Time: 16:43
 */
class ScrollBoundaryDeciderAdapter:ScrollBoundaryDecider {
    private var mMotionEvent: MotionEvent? = null
    private var mBoundary:ScrollBoundaryDecider? = null

    fun setMotionEvent(motionEvent:MotionEvent){
        this.mMotionEvent = motionEvent
    }

    fun setBoundary(boundary:ScrollBoundaryDecider){
        this.mBoundary = boundary
    }

    override fun canRefresh(content: View) {
      //  if(mBoundary!=null) return mBoundary?.canRefresh(content)

    }
}