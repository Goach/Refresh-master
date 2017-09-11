package com.goach.refreshlayout.holder

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goach.refreshlayout.R
import com.goach.refreshlayout.const.RefreshState
import com.goach.refreshlayout.utils.cling
import com.goach.refreshlayout.widget.BoxView
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.topPadding

/**
 * Created by iGoach on 2017/7/22.
 * 今日头条头部
 */
class TTHeaderHolder(private val context: Context): IHeaderHolder {
    private var DEFAULT_RADIUS = context.dip(10)
    private var DEFAULT_PADDING = context.dip(3)
    private var DEFAULT_BORDER_COLOR = Color.parseColor("#ADAEAE")
    private var DEFAULT_BOX_COLOR = Color.parseColor("#E2E2E2")
    private var DEFAULT_PAINT_W = 2
    private var DEFAULT_SHORT_LINE_WEIGHT = 0.5f
    private var DEFAULT_ANIM_TIME:Int = 500
    private var DEFAULT_TIP_TEXT_COLOR= Color.parseColor("#A1A1A1")
    private var DEFAULT_TIP_TEXT_SIZE= 10f
    private var DEFAULT_TIP_TEXT_PULL= "下拉推荐"
    private var DEFAULT_TIP_TEXT_RELEASE= "松开推荐"
    private var DEFAULT_TIP_TEXT_REFRESHING= "推荐中"

    private var mRadius:Int = DEFAULT_RADIUS
    private var mPadding:Int = DEFAULT_PADDING
    private var mBorderColor:Int = DEFAULT_BORDER_COLOR
    private var mBoxColor:Int = DEFAULT_BOX_COLOR
    private var mPaintWidth:Int = DEFAULT_PAINT_W
    private var mShortLineWeight:Float = DEFAULT_SHORT_LINE_WEIGHT
    private var mAnimTime:Long = DEFAULT_ANIM_TIME.toLong()
    private var mTipTextColor:Int = DEFAULT_TIP_TEXT_COLOR
    private var mTipTextSize:Float = DEFAULT_TIP_TEXT_SIZE
    private var mTipTextPull:CharSequence = DEFAULT_TIP_TEXT_PULL
    private var mTipTextRelease:CharSequence = DEFAULT_TIP_TEXT_RELEASE
    private var mTipTextRefreshing:CharSequence = DEFAULT_TIP_TEXT_REFRESHING
    private var mProgress:Float = 0f
    private var mRefreshBorderHeight:Int = context.dip(15)


    private lateinit var mBoxView: BoxView
    private lateinit var mTipTextView: TextView


    override fun createHeaderView(context:Context,parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.tt_refresh_header_layout,parent,false)
    }
    override fun initView(headerView:View) {
        super.initView(headerView)
        Log.d("zgx","scrollY===initView")
        mBoxView = headerView.find<BoxView>(R.id.boxView)
        mTipTextView = headerView.find<TextView>(R.id.tvRefreshTip)
        mTipTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,mTipTextSize)
        mTipTextView.setTextColor(mTipTextColor)
        mBoxView.setRadius(mRadius)
                .setBoxPadding(mPadding)
                .setBorderColor(mBorderColor)
                .setBoxColor(mBoxColor)
                .setPaintWidth(mPaintWidth)
                .setShortLineWeight(mShortLineWeight)
                .setAnimTime(mAnimTime)
                .setProgress(mProgress)
    }

    override fun moveSpinner(headerView: View, scrollY: Int){
        val paddingTop = headerView.paddingTop*1.0f
        val minHeight = headerView.measuredHeight - headerView.paddingTop*1.0f
        val newScrollY = scrollY - minHeight*1.0f
        mBoxView.setProgress(context.cling(0f,paddingTop,newScrollY)/paddingTop)
    }

    override fun onStateChanged(headerView: View, oldState: RefreshState, newState: RefreshState) {
        when(newState){
            RefreshState.None -> {
                mBoxView.setProgress(0f)
                mBoxView.stopAnim()
                mTipTextView.text =  mTipTextPull
            }
            RefreshState.PullDownToRefresh -> {
                mTipTextView.text =  mTipTextPull
            }
            RefreshState.Refreshing -> {
                mBoxView.setProgress(1.0f)
                mBoxView.startAnim()
                mTipTextView.text = mTipTextRefreshing
            }
            RefreshState.ReleaseToRefresh ->{
                mTipTextView.text =  mTipTextRelease
            }
            else -> {
            }
        }
    }

    fun setRadius(radius:Int){
        this.mRadius = radius
        mBoxView.setRadius(mRadius)
    }
    fun setPadding(padding:Int){
        this.mPadding = padding
        mBoxView.setBoxPadding(padding)
    }
    fun setBoxColor(boxColor:Int){
        mBoxColor = boxColor
        mBoxView.setBoxColor(mBoxColor)
    }
    fun setBorderColor(borderColor:Int){
        this.mBorderColor = borderColor
        mBoxView.setBorderColor(borderColor)
    }
    fun setPaintWidth(paintWidth:Int){
        this.mPaintWidth = paintWidth
        mBoxView.setPaintWidth(mPaintWidth)
    }

    fun setShortLineWight(shortLineWeight:Float){
        this.mShortLineWeight = shortLineWeight
        mBoxView.setShortLineWeight(shortLineWeight)
    }
    fun setAnimTime(animTime:Long){
        this.mAnimTime = animTime
        mBoxView.setAnimTime(this.mAnimTime)
    }

    fun setProgress(progress:Float){
        this.mProgress = progress
        this.mBoxView.setProgress(mProgress)
    }

}