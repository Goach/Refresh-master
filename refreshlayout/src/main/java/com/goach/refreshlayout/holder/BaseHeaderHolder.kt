package com.goach.refreshlayout.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * Created by iGoach on 2017/7/22.
 * 下拉刷新滑动处理
 *
 */
abstract class BaseHeaderHolder{
    open fun initView(headerView:View){}
    abstract fun createHeaderView(context: Context, parent: ViewGroup): View
    abstract fun handlerRefreshState(headerView:View,scrollY:Int)
    abstract fun refreshStatusIDIE(headerView:View)
    abstract fun refreshStatusPullDown(headerView:View)
    abstract fun refreshBorderTop():Float
    abstract fun refreshStatusRefreshing(headerView:View)
    abstract fun refreshStatusRelease(headerView:View)

}