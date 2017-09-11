package com.goach.refreshlayout.const

import android.util.Log

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/9/2 0002
 *Time: 14:08
 */
enum class RefreshState {
    None,
    PullDownToRefresh,
    PullDownCanceled,PullUpCanceled,
    ReleaseToRefresh,
    Refreshing,
    RefreshFinish,;

    fun isHeader(): Boolean {
        return ordinal and 1 == 1
    }

    fun isDragging(): Boolean {
        return ordinal >= PullDownToRefresh.ordinal
                && ordinal <= ReleaseToRefresh.ordinal
                && this != PullDownCanceled
                && this != PullUpCanceled
    }
}