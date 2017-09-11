package com.goach.refreshlayout.api

import android.view.MotionEvent

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/9/2 0002
 *Time: 8:59
 */
interface RefreshContent{
    fun onActionDown(ev:MotionEvent)
    fun  onActionUpOrCancel(ev:MotionEvent)
}