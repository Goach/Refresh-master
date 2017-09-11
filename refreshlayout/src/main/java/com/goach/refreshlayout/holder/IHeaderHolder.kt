package com.goach.refreshlayout.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.goach.refreshlayout.const.RefreshState

/**
 * Created by iGoach on 2017/7/22.
 * 下拉刷新滑动处理
 *
 */
interface IHeaderHolder {
     fun initView(headerView:View){}
     fun createHeaderView(context: Context, parent: ViewGroup): View
     fun moveSpinner(headerView:View,scrollY:Int)
     fun onStateChanged(headerView:View,oldState:RefreshState,newState:RefreshState)

}