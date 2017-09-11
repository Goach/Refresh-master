package com.goach.refreshlayout.holder

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goach.refreshlayout.R
import com.goach.refreshlayout.const.RefreshState
import com.goach.refreshlayout.widget.Titanic
import com.goach.refreshlayout.widget.TitanicTextView
import org.jetbrains.anko.find

/**
 * Created by iGoach on 2017/7/22.
 * 今日头条头部
 */
class WaveHeaderHolder: IHeaderHolder {
    private lateinit var tvTitanic:TitanicTextView
    private lateinit var titanic:Titanic
    private lateinit var tvTip:TextView
    private var DEFAULT_TIP_TEXT_COLOR= Color.parseColor("#A1A1A1")
    private var DEFAULT_TIP_TEXT_SIZE= 14f
    private var DEFAULT_TIP_TEXT_PULL= "下拉推荐"
    private var DEFAULT_TIP_TEXT_RELEASE= "松开推荐"
    private var DEFAULT_TIP_TEXT_REFRESHING= "推荐中..."
    private var mTipTextColor:Int = DEFAULT_TIP_TEXT_COLOR
    private var mTipTextSize:Float = DEFAULT_TIP_TEXT_SIZE
    private var mTipTextPull:CharSequence = DEFAULT_TIP_TEXT_PULL
    private var mTipTextRelease:CharSequence = DEFAULT_TIP_TEXT_RELEASE
    private var mTipTextRefreshing:CharSequence = DEFAULT_TIP_TEXT_REFRESHING

    override fun createHeaderView(context:Context,parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.wave_refresh_header_layout,parent,false)
    }
    override fun initView(headerView:View) {
        super.initView(headerView)
        titanic = Titanic()
        tvTitanic = headerView.find<TitanicTextView>(R.id.tvTitanic)
        tvTip = headerView.find<TextView>(R.id.tvTip)
        tvTip.setTextSize(TypedValue.COMPLEX_UNIT_SP,mTipTextSize)
        tvTip.setTextColor(mTipTextColor)
    }

    override fun moveSpinner(headerView: View, scrollY: Int) {

    }

    override fun onStateChanged(headerView: View, oldState: RefreshState, newState: RefreshState) {

    }

}