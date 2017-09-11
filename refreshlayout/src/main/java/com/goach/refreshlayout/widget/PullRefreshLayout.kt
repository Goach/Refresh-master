package com.goach.refreshlayout.widget

import android.content.Context
import android.support.v4.view.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import com.goach.refreshlayout.R
import com.goach.refreshlayout.holder.IHeaderHolder
import com.goach.refreshlayout.holder.TTHeaderHolder
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewConfiguration
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Interpolator
import com.goach.refreshlayout.api.RefreshContent
import com.goach.refreshlayout.const.RefreshState
import com.goach.refreshlayout.utils.ScrollBoundaryUtil
import com.goach.refreshlayout.utils.ViscousFluidInterpolator
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import org.jetbrains.anko.dip

/**
 * Created by iGoach on 2017/8/31.
 * 事件处理
 * 1、创建默认头部，添加头部，同时也可以设置头部，以及头部的位置onLayout()
 * 2、下拉刷新事件处理 ,dispatchTouchEvent,onInterceptTouchEvent,onTouchEvent,NestScrollingParent,NestScrollingChild
 */
class PullRefreshLayout : ViewGroup,NestedScrollingParent,NestedScrollingChild{
    //头部的默认高度
    private val DEFAULT_HEADER_HEIGHT = context.dip(100)
    //头部的一些基本设置
    private var mHeaderViewHolder:IHeaderHolder? = null
    //头部控件
    private var mHeaderRefreshLayout: View? = null
    //头部高度
    private var mHeaderHeight:Int = 0
    private var mHeaderExtendHeight:Int = 0
    private var mHeaderMaxDragRate = 2.0f
    private var mScreenHeightPixels:Int = 0
    private var mDragRate = .5f
    //需要滚动的控件
    private var mTargetLayout:View? = null

    //滚动事件处理
    //是否在拖动
    private var mIsBeingDragged:Boolean = false
    //手指按下
    private var mTouchX:Float = 0f
    private var mTouchY:Float = 0f
    //记录最后手机按下的位置坐标
    private var mLastTouchX:Float = 0f
    private var mLastTouchY:Float = 0f
    private var mSpinner:Int  = 0
    private var mLastSpinner:Int  = 0
    private var mTouchSpinner:Int  = 0
    private var mTouchSlop:Int = 0
    //滑动判断
    private var mFalsifyEvent:MotionEvent? = null
    //刷新状态
    private var mRefreshState = RefreshState.None
    //副状态(主状态刷新的时候的滚动状态)
    private var mViceState = RefreshState.None
    //处理滚动控件的MotionEvent
    private var mMotionEvent:MotionEvent? = null
    private var mNestedScrollInProgress:Boolean = false
    protected var mParentScrollConsumed = IntArray(2)
    protected var mParentOffsetInWindow = IntArray(2)
    //嵌套滚动总共消耗的距离
    private var mTotalUnconsumed:Int = 0
    //动画监听的
    private lateinit var mReboundInterpolator: Interpolator
    private var reboundAnimator:ValueAnimator? = null
    private var mReboundDuration = 250

    //嵌套滚动处理
    private lateinit var mNestedScrollingChildHelper:NestedScrollingChildHelper
    private lateinit var mNestedScrollingParentHelper:NestedScrollingParentHelper

    private var reboundAnimatorEndListener: Animator.AnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            reboundAnimator = null
            if ((animation as ValueAnimator).animatedValue as Int == 0) {
                if (mRefreshState !== RefreshState.None && mRefreshState !== RefreshState.Refreshing ) {
                    notifyStateChanged(RefreshState.None)
                }
            }
        }
    }
    private var reboundUpdateListener: ValueAnimator.AnimatorUpdateListener
            = ValueAnimator.AnimatorUpdateListener { animation -> moveSpinner(animation.animatedValue as Int, true) }
    constructor(context: Context):this(context,null)
    constructor(context: Context, attributeSet: AttributeSet?):this(context,attributeSet,0)
    constructor(context: Context, attributeSet: AttributeSet?, defaultStyle:Int):super(context,
            attributeSet,defaultStyle) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        initView()
        initStyle(attributeSet)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(isInEditMode) return
        //设置默认头部
        if(mHeaderViewHolder==null){
            mHeaderViewHolder = TTHeaderHolder(context)
        }
        mHeaderRefreshLayout = getHeaderView()
        if(mHeaderRefreshLayout!=null)
        mHeaderViewHolder!!.initView(mHeaderRefreshLayout!!)
        addView(mHeaderRefreshLayout,MATCH_PARENT,WRAP_CONTENT)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val childCount = childCount
        for(index in 0..childCount-1){
            val childView = getChildAt(index)
            if(childView != mHeaderRefreshLayout){
                mTargetLayout = childView
                return
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var minimumHeight = 0
        if(mHeaderRefreshLayout!=null){
            val headerLp = mHeaderRefreshLayout!!.layoutParams as PullRefreshLayout.LayoutParams
            val widthSpec = getChildMeasureSpec(widthMeasureSpec,headerLp.leftMargin + headerLp.rightMargin,headerLp.width)
            var heightSpec = heightMeasureSpec
            if(headerLp.height > 0){
                heightSpec  = makeMeasureSpec(headerLp.height, EXACTLY)
                mHeaderRefreshLayout!!.measure(widthSpec,heightSpec)
                mHeaderHeight = headerLp.height + headerLp.bottomMargin
                mHeaderExtendHeight = Math.max(mHeaderHeight * (mHeaderMaxDragRate - 1), 0f).toInt()
            }else if(headerLp.height == WRAP_CONTENT){
                heightSpec  = makeMeasureSpec(Math.max(MeasureSpec.getSize(heightMeasureSpec) - headerLp.bottomMargin, 0), MeasureSpec.AT_MOST)
                mHeaderRefreshLayout!!.measure(widthSpec, heightSpec)
                mHeaderHeight = mHeaderRefreshLayout!!.measuredHeight + headerLp.bottomMargin
                mHeaderExtendHeight = Math.max(mHeaderHeight * (mHeaderMaxDragRate - 1), 0f).toInt()
            }else if(headerLp.height == MATCH_PARENT){
                heightSpec  = makeMeasureSpec(Math.max(mHeaderHeight - headerLp.bottomMargin, 0), EXACTLY)
                mHeaderRefreshLayout!!.measure(widthSpec, heightSpec)
            } else {
                mHeaderRefreshLayout!!.measure(widthSpec,heightSpec)
            }
        }
        if(mTargetLayout!=null){
            val targetLp = mTargetLayout!!.layoutParams as PullRefreshLayout.LayoutParams
            val widthSpec = getChildMeasureSpec(widthMeasureSpec,targetLp.leftMargin+targetLp.rightMargin,targetLp.width)
            val heightSpec = getChildMeasureSpec(heightMeasureSpec,paddingTop + paddingBottom
                    + targetLp.topMargin + targetLp.bottomMargin,targetLp.height)
            mTargetLayout!!.measure(widthSpec,heightSpec)
            minimumHeight += mTargetLayout!!.measuredHeight
        }
        setMeasuredDimension(View.resolveSize(suggestedMinimumWidth,widthMeasureSpec),
                View.resolveSize(minimumHeight,heightMeasureSpec))
    }
    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val isInEditMode = isInEditMode
        if(mHeaderRefreshLayout != null){
            val headerLp = mHeaderRefreshLayout!!.layoutParams as PullRefreshLayout.LayoutParams
            val left = headerLp.leftMargin
            var top = headerLp.topMargin
            val right = left + mHeaderRefreshLayout!!.measuredWidth
            var bottom = top + mHeaderRefreshLayout!!.measuredHeight
            if(!isInEditMode){
                top -= mHeaderHeight
                bottom = top + mHeaderHeight
            }
            //Log.d("zgx","size====mHeaderRefreshLayout====left===$left top===$top right====$right bottom====$bottom")
            mHeaderRefreshLayout!!.layout(left,top,right,bottom)
        }
        if(mTargetLayout!=null){
            val targetLp = mTargetLayout!!.layoutParams as PullRefreshLayout.LayoutParams
            val left = paddingLeft + targetLp.leftMargin
            val top = paddingTop + targetLp.topMargin
            val right = left + mTargetLayout!!.measuredWidth
            val bottom = top + mTargetLayout!!.measuredHeight
            mTargetLayout!!.layout(left,top,right,bottom)
           // Log.d("zgx","size====mTargetLayout=====left===$left top===$top right====$right bottom====$bottom")
        }
    }
    //是否在拖动的过程isBeingDragged 大于mTouchSlop 允许最大滑动角度为45°，dy>0 控件是否支持滑动
    //保存一个副状态，表示在刷新状态
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //加起所有的手指，然后取平均值
        val action = ev.actionMasked
        //ACTION_POINTER_UP 当其中一个手指松开的时候触发，并且不是最后一个手指
        val pointerUp = action == MotionEvent.ACTION_POINTER_UP
        val skipIndex = if(pointerUp) ev.actionIndex else -1
        var sumX = 0f
        var sumY = 0f
        val count = ev.pointerCount
        for(i in 0..count-1){
            if(skipIndex == i) continue
            sumX += ev.getX(i)
            sumY += ev.getY(i)
        }
        val div = if(pointerUp) count - 1 else  count
        val touchX = sumX/div
        val touchY = sumY/div
        //touchY 记录当前多指均值  mLastTouchY上一次多指均值  mTouchY和偏移量平行变动保持dy不变从而保持手指松开的时候的稳定
        if((action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_POINTER_DOWN)&&mIsBeingDragged){
            mTouchY += touchY - mLastTouchY
        }
        mLastTouchX = touchX
        mLastTouchY = touchY
        //初始化滚动控件的触摸位置
        if(mTargetLayout!=null){
            when(action){
                MotionEvent.ACTION_DOWN ->{
                    mMotionEvent = MotionEvent.obtain(ev)
                    mMotionEvent!!.offsetLocation(-mTargetLayout!!.left.toFloat(),-mTargetLayout!!.top.toFloat())
                }
                (MotionEvent.ACTION_CANCEL or MotionEvent.ACTION_UP)-> {
                    mMotionEvent = null
                }
            }
        }
        //处理动画执行的时候，正在刷新的时候
        if(reboundAnimator!=null&&!interceptAnimator(action)){
            return false
        }
        when(action){
            MotionEvent.ACTION_DOWN -> {
                mTouchX = touchX
                mTouchY = touchY
                mLastTouchY = touchY
                mLastSpinner = 0
                mTouchSpinner = mSpinner
                mIsBeingDragged = false
                super.dispatchTouchEvent(ev)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = touchX - mTouchX
                var dy = touchY  - mTouchY
                mLastTouchY = touchY
                if(!mIsBeingDragged){
                    if(Math.abs(dy) >= mTouchSlop && Math.abs(dx) < Math.abs(dy)){
                        if(dy > 0 && (mSpinner < 0 || ScrollBoundaryUtil.canRefresh(mTargetLayout!!,ev))){
                            if (mSpinner >= 0) {
                                setStatePullDownToRefresh()
                            }
                            mIsBeingDragged = true
                            mTouchY = touchY - mTouchSlop
                            dy = touchY - mTouchY
                            ev.action = MotionEvent.ACTION_CANCEL
                            super.dispatchTouchEvent(ev)
                        }else {
                            return super.dispatchTouchEvent(ev)
                        }
                    } else run { return super.dispatchTouchEvent(ev) }
                }else{
                    val spinner = dy + mTouchSpinner
                    if(mTargetLayout!=null&&getViceState().isHeader()&&(spinner < 0 || mLastSpinner < 0)){
                        val time = ev.eventTime
                        if(mFalsifyEvent==null){
                            mFalsifyEvent = MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,mTouchX + dx ,mTouchY,0)
                            super.dispatchTouchEvent(mFalsifyEvent)
                        }
                        val em = MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, mTouchX + dx, mTouchY + spinner, 0)
                        super.dispatchTouchEvent(em)
                        if(getViceState().isHeader()&&spinner < 0){
                            mLastSpinner = spinner.toInt()
                            if(mSpinner!=0){
                                moveSpinnerInfinitely(0f)
                            }
                            return true
                        }
                        mLastSpinner = spinner.toInt()
                        mFalsifyEvent = null
                        val ec = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, mTouchX, mTouchY + spinner, 0)
                        super.dispatchTouchEvent(ec)
                    }
                    if (getViceState().isDragging()) {
                        moveSpinnerInfinitely(spinner)
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mIsBeingDragged = false
                if (mFalsifyEvent != null) {
                    mFalsifyEvent = null
                    val time = ev.eventTime
                    val ec = MotionEvent.obtain(time, time, if (mSpinner == 0) MotionEvent.ACTION_UP else MotionEvent.ACTION_CANCEL, mTouchX, touchY, 0)
                    super.dispatchTouchEvent(ec)
                }
                if (overSpinner()) {
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                if (mFalsifyEvent != null) {
                    mFalsifyEvent = null
                    val time = ev.eventTime
                    val ec = MotionEvent.obtain(time, time, if (mSpinner == 0) MotionEvent.ACTION_UP else MotionEvent.ACTION_CANCEL, mTouchX, touchY, 0)
                    super.dispatchTouchEvent(ec)
                }
                if (overSpinner()) {
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }
    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        val accepted = isEnabled && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        return accepted
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        mNestedScrollingParentHelper.onNestedScrollAccepted(child,target,axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mTotalUnconsumed = 0
        mTouchSpinner = mSpinner
        mNestedScrollInProgress = true
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
        val parentConsumed = mParentScrollConsumed
        var newDy = dy
        if(mRefreshState == RefreshState.Refreshing){
            if(dispatchNestedPreScroll(dx,newDy,mParentScrollConsumed,null)){
                newDy  -= parentConsumed[1]
            }
            if(mRefreshState == RefreshState.Refreshing && (newDy * mTotalUnconsumed > 0 || mTouchSlop > 0)){
                consumed[1] = 0
                if(Math.abs(newDy) > Math.abs(mTotalUnconsumed)){
                    consumed[1] += mTotalUnconsumed
                    mTotalUnconsumed = 0
                    newDy -= mTotalUnconsumed
                    if(mTouchSpinner <= 0){
                        moveSpinnerInfinitely(0f)
                    }
                } else {
                    mTotalUnconsumed -= newDy
                    consumed[1] += newDy
                    newDy = 0
                    moveSpinnerInfinitely(mTotalUnconsumed + mTouchSpinner*1.0f)
                }
                if(newDy > 0 && mTouchSpinner >0 ){
                    if(dy > mTouchSpinner){
                        consumed[1] += mTouchSpinner
                        mTouchSpinner = 0
                    }else{
                        mTouchSpinner -= newDy
                        consumed[1] += newDy
                    }
                    moveSpinnerInfinitely(mTouchSpinner*1.0f)
                }
            }
        }else {
            if (dy > 0 && mTotalUnconsumed > 0) {
                if (dy > mTotalUnconsumed) {
                    consumed[1] = dy - mTotalUnconsumed
                    mTotalUnconsumed = 0
                } else {
                    mTotalUnconsumed -= dy
                    consumed[1] = dy
                }
                moveSpinnerInfinitely(mTotalUnconsumed.toFloat())
            }
            if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
                consumed[0] += parentConsumed[0]
                consumed[1] += parentConsumed[1]
            }
        }
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(child: View) {
        super.onStopNestedScroll(child)
        mNestedScrollingParentHelper.onStopNestedScroll(child)
        mNestedScrollInProgress = false
        mTotalUnconsumed = 0
        overSpinner()
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow)
        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (mRefreshState === RefreshState.Refreshing) {
            if (dy < 0 && (mTargetLayout == null || ScrollBoundaryUtil.canRefresh(mTargetLayout!!))) {
                mTotalUnconsumed += Math.abs(dy)
                moveSpinnerInfinitely((mTotalUnconsumed + mTouchSpinner).toFloat())
            }
        } else {
            if (dy < 0 && (mTargetLayout == null || ScrollBoundaryUtil.canRefresh(mTargetLayout!!))) {
                if (mRefreshState === RefreshState.None) {
                    setStatePullDownToRefresh()
                }
                mTotalUnconsumed += Math.abs(dy)
                moveSpinnerInfinitely(mTotalUnconsumed.toFloat())
            }
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return reboundAnimator != null ||
                mRefreshState == RefreshState.ReleaseToRefresh ||
                mRefreshState == RefreshState.PullDownToRefresh && mSpinner > 0 ||
                mSpinner > 0 || mRefreshState == RefreshState.Refreshing && mSpinner != 0 ||
                mSpinner != 0 || dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        super.setNestedScrollingEnabled(enabled)
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
                                      dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    fun initView(){
        mReboundInterpolator = ViscousFluidInterpolator()
        mHeaderExtendHeight = Math.max(mHeaderHeight * (mHeaderMaxDragRate - 1), 0f).toInt()
        mScreenHeightPixels = context.resources.displayMetrics.heightPixels

        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
    }
    fun initStyle(attributeSet: AttributeSet?){
        val ta = context.obtainStyledAttributes(attributeSet,R.styleable.PullRefreshLayout)
        mHeaderHeight = ta.getDimensionPixelSize(R.styleable.PullRefreshLayout_prlHeaderHeight,DEFAULT_HEADER_HEIGHT)
        ta.recycle()
    }
    class LayoutParams : ViewGroup.MarginLayoutParams {

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.PullRefreshLayout_Layout)
            backgroundColor = ta.getColor(R.styleable.PullRefreshLayout_Layout_layout_PullBackgroundColor, backgroundColor)
            ta.recycle()
        }
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.MarginLayoutParams) : super(source)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        var backgroundColor = 0
    }
    fun setHeaderViewHolder(headerHolder: IHeaderHolder){
        this.mHeaderViewHolder = headerHolder
        if(mHeaderRefreshLayout!=null){
            removeView(mHeaderRefreshLayout)
        }
        mHeaderRefreshLayout = getHeaderView()
        addView(mHeaderRefreshLayout)
        requestLayout()

    }
    fun getHeaderView():View?{
        if(mHeaderViewHolder == null) return null
        return mHeaderViewHolder!!.createHeaderView(context,this)
    }

    fun notifyStateChanged(state:RefreshState){
        val oldState = mRefreshState
        if(oldState!=state){
            mRefreshState = state
            mViceState = state
            //改变头部状态，改变加载更多状态，改变滚动控件状态
            if(mHeaderRefreshLayout!=null)
            mHeaderViewHolder?.onStateChanged(mHeaderRefreshLayout!!,oldState,mRefreshState)
        }
    }
    fun interceptAnimator(action:Int):Boolean {
        if(reboundAnimator!=null&&action == MotionEvent.ACTION_DOWN){
            if(mRefreshState == RefreshState.RefreshFinish){
                return false
            }
            if(mRefreshState == RefreshState.PullDownCanceled){
                setStatePullDownToRefresh()
            }
            reboundAnimator!!.cancel()
            reboundAnimator = null
            return true
        }
        return false
    }
    fun setStatePullDownToRefresh(){
        if(mRefreshState != RefreshState.Refreshing){
            notifyStateChanged(RefreshState.PullDownToRefresh)
        }else{
            setViceState(RefreshState.PullDownToRefresh)
        }
    }
    fun setStateReleaseToRefresh(){
        if(mRefreshState != RefreshState.Refreshing){
            notifyStateChanged(RefreshState.ReleaseToRefresh)
        }else{
            setViceState(RefreshState.ReleaseToRefresh)
        }
    }

    fun setStatePullDownCanceled() {
        if (mRefreshState !== RefreshState.Refreshing) {
            notifyStateChanged(RefreshState.PullDownCanceled)
            resetStatus()
        } else {
            setViceState(RefreshState.PullDownCanceled)
        }
    }

    fun setStateRefresing() {
        notifyStateChanged(RefreshState.Refreshing)
        animSpinner(mHeaderHeight)
    }
    fun resetStatus() {
        if (mRefreshState !== RefreshState.None) {
            if (mSpinner == 0) {
                notifyStateChanged(RefreshState.None)
            }
        }
        if (mSpinner != 0) {
            animSpinner(0)
        }
    }
    fun setViceState(state: RefreshState){
        if(mRefreshState == RefreshState.Refreshing){
            if(mViceState!=state){
                mViceState = state
            }
        }
    }
    fun getViceState():RefreshState{
        return if(mRefreshState == RefreshState.Refreshing) mViceState else mRefreshState
    }

    /**
     * 手势拖动结束
     * 开始执行回弹动画
     */
    fun overSpinner(): Boolean {
        Log.d("zgx", "mState===" + mRefreshState)
       if (mRefreshState === RefreshState.Refreshing) {
            if (mSpinner > mHeaderHeight) {
                mTotalUnconsumed = mHeaderHeight
                animSpinner(mHeaderHeight)
            } else if (mSpinner < 0) {
                mTotalUnconsumed = 0
                animSpinner(0)
            } else {
                return false
            }
        } else if (mRefreshState == RefreshState.PullDownToRefresh && mRefreshState === RefreshState.ReleaseToRefresh) {
            setStatePullDownCanceled()
        } else if (mRefreshState == RefreshState.ReleaseToRefresh) {
            setStateRefresing()
        } else if (mSpinner != 0) {
            animSpinner(0)
        } else {
            return false
        }
        return true
    }

    fun animSpinner(endSpinner: Int):ValueAnimator {
        return animSpinner(endSpinner, 0)
    }

     fun animSpinner(endSpinner: Int, startDelay: Int): ValueAnimator {
        return animSpinner(endSpinner, startDelay, mReboundInterpolator)
    }

    fun animSpinner(endSpinner: Int, startDelay: Int, interpolator: Interpolator): ValueAnimator {
        if (mSpinner != endSpinner) {
            if (reboundAnimator != null) {
                reboundAnimator!!.cancel()
            }
            reboundAnimator = ValueAnimator.ofInt(mSpinner, endSpinner)
            reboundAnimator!!.duration = mReboundDuration.toLong()
            reboundAnimator!!.interpolator = interpolator
            reboundAnimator!!.addUpdateListener(reboundUpdateListener)
            reboundAnimator!!.addListener(reboundAnimatorEndListener)
            reboundAnimator!!.startDelay = startDelay.toLong()
            reboundAnimator!!.start()
        }
        return reboundAnimator!!
    }
    fun moveSpinnerInfinitely(dy: Float) {
        Log.d("zgx","mRefreshState====$mRefreshState")
        if (mRefreshState == RefreshState.Refreshing && dy >= 0) {
            if (dy < mHeaderHeight) {
                moveSpinner(dy.toInt(), false)
            } else {
                val M = mHeaderExtendHeight.toDouble()
                val H = (Math.max(mScreenHeightPixels * 4 / 3, height) - mHeaderHeight).toDouble()
                val x = Math.max(0f, (dy - mHeaderHeight) * mDragRate).toDouble()
                val y = Math.min(M * (1 - Math.pow(100.0, -x / H)), x)// 公式 y = M(1-40^(-x/H))
                moveSpinner(y.toInt() + mHeaderHeight, false)
            }
        }else if (dy >= 0) {
            val M = (mHeaderExtendHeight + mHeaderHeight).toDouble()
            val H = Math.max(mScreenHeightPixels / 2, height).toDouble()
            val x = Math.max(0f, dy * mDragRate).toDouble()
            val y = Math.min(M * (1 - Math.pow(100.0, -x / H)), x)// 公式 y = M(1-40^(-x/H))
            moveSpinner(y.toInt(), false)
        } else run {
            val H = Math.max(mScreenHeightPixels / 2, height).toDouble()
            val x = (-Math.min(0f, dy * mDragRate)).toDouble()
            val y = -Math.min( (1 - Math.pow(100.0, -x / H)), x)// 公式 y = M(1-40^(-x/H))
            moveSpinner(y.toInt(), false)
        }
    }

    fun moveSpinner(spinner:Int,isAnimator:Boolean){
        if(mSpinner == spinner && mHeaderRefreshLayout==null)
            return
        val oldSpinner = mSpinner
        this.mSpinner = spinner
        if(!isAnimator && getViceState().isDragging()){
            if(mSpinner > mHeaderHeight){
                setStateReleaseToRefresh()
            }else{
                setStatePullDownToRefresh()
            }
        }
        if(mTargetLayout != null){
            if(spinner > 0){
                mTargetLayout!!.translationY = spinner.toFloat()
            }
        }
        if(mHeaderRefreshLayout != null){
            if(spinner > 0){
                mHeaderViewHolder!!.moveSpinner(mHeaderRefreshLayout!!,spinner)
                mHeaderRefreshLayout!!.translationY = spinner.toFloat()
            }
        }
    }


}