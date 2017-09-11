package com.goach.refreshlayout.api

import android.view.View

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/9/1 0001
 *Time: 16:40
 */
interface ScrollBoundaryDecider{
    fun canRefresh(content: View)
}