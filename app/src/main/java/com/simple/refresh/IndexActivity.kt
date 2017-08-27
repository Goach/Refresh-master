package com.simple.refresh

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_index.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/8/16 0016
 *Time: 10:40
 */
class IndexActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        btnScroll.onClick {
            startActivity<ScrollViewActivity>()
        }
        btnRecyclerView.onClick {
            startActivity<RecyclerViewActivity>()
        }
        btnCoordinationLayout.onClick {
            startActivity<CoordinationLayoutActivity>()
        }
    }
}