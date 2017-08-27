package com.goach.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import org.jetbrains.anko.find

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/8/16 0016
 *Time: 10:57
 * 基类，主要实现toolBar的适配
 */
abstract class BaseActivity :AppCompatActivity(){
    lateinit var mContextView:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(layoutRes()>0)
            setContentView(layoutRes())
        if(layoutView()!=null)
            setContentView(layoutView())
        if(isFinishing){
            handleView()
            handleData()
        }
        mContextView  = find(android.R.id.content)
    }

    open fun layoutRes(): Int = 0
    open fun layoutView():View? = null
    open fun handleView(){}
    open fun handleData(){}
}