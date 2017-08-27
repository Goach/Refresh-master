package com.simple.refresh

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scrollview.*
import org.jetbrains.anko.onClick

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/8/16 0016
 *Time: 15:14
 */
class ScrollViewActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrollview)
        ttRefreshLayout.setRefreshing()
        btnStart.onClick{
            ttRefreshLayout.setRefreshing()
        }
        btnStop.onClick{
            ttRefreshLayout.endRefresh()
        }
    }
}