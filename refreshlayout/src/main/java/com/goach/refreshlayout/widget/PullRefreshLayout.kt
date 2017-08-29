package com.goach.refreshlayout.widget

import android.content.Context
import android.support.v4.view.*
import android.support.v4.widget.ListViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.*
import com.goach.refreshlayout.holder.BaseHeaderHolder
import com.goach.refreshlayout.holder.TTHeaderHolder
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import org.jetbrains.anko.dip

/**
 * Created by iGoach on 2017/7/22.
 * 事件处理
 */
class PullRefreshLayout : LinearLayout,NestedScrollingParent,NestedScrollingChild{

    private lateinit var mHeaderView: View
    private var INVALID_POINTER = -1
    private var mHeaderViewHeight:Int = 0
    private var mTargetView:View? = null
    private var mRefreshing:Boolean = false
    private var mChildCanScrollUpCallBack:ChildScrollUpCallBack? = null
    private var mTTRefreshLayoutListener:TTRefreshLayoutListener? = null
    private var mActivePointerId:Int = INVALID_POINTER
    private var mIsBeingDragged:Boolean = false
    private val DRAG_RATE = .5f
    private var mInitialMotionY: Float = 0.toFloat()
    private var mInitialDownY:Float = 0f
    private var mTouchSlop:Int = 0
    private var mCurrentRefreshState = RefreshStatus.IDLE
    private lateinit var mHeaderHolder:BaseHeaderHolder
    private var mNestedScrollingParentHelper:NestedScrollingParentHelper
    private var mNestedScrollingChildHelper:NestedScrollingChildHelper
    private var mTotalUnconsumed: Float = 0f
    private var mNestedScrollInProgress: Boolean = false
    private val mParentScrollConsumed = IntArray(2)
    private val mParentOffsetInWindow = IntArray(2)

    constructor(context: Context):this(context,null)
    constructor(context: Context, attributeSet: AttributeSet?):this(context,attributeSet,0)
    constructor(context: Context, attributeSet: AttributeSet?, defaultStyle:Int):super(context,
            attributeSet,defaultStyle) {
        isEnabled = true
        orientation = LinearLayout.VERTICAL
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
        initHeaderView()
    }
    fun initHeaderView(){
        mHeaderHolder = TTHeaderHolder(context)
        mHeaderView = mHeaderHolder.createHeaderView(context,this)
        mHeaderHolder.initView(mHeaderView)
        addView(mHeaderView)
        mHeaderView.post {
            mHeaderViewHeight = mHeaderView.height
            mHeaderView.setPadding(0,-mHeaderView.height,0,0)
        }
    }
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        ensureTarget()
        if(!isEnabled||mRefreshing||mIsBeingDragged||canChildScrollUp()||mNestedScrollInProgress)
            return false
        val action = ev.actionMasked
        var pointerIndex = INVALID_POINTER
        when(action){
            MotionEvent.ACTION_DOWN ->{
                mIsBeingDragged = false
                mActivePointerId = ev.getPointerId(0)
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if(pointerIndex<0) return false
                mInitialDownY = ev.getY(pointerIndex)
            }
            MotionEvent.ACTION_MOVE->{
                if(mActivePointerId == INVALID_POINTER) return false
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if(pointerIndex<0) return false
                startDragging(ev.getY(pointerIndex))
            }
            MotionEvent.ACTION_POINTER_UP ->  onSecondaryPointerUp(ev)
            MotionEvent.ACTION_CANCEL ->{
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
            MotionEvent.ACTION_UP ->{
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }

        }
        return mIsBeingDragged
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!isEnabled||mRefreshing||canChildScrollUp())
            return false

        val action = event.actionMasked
        var pointerIndex = INVALID_POINTER
        when(action){
            MotionEvent.ACTION_DOWN ->{
                mIsBeingDragged = false
                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE->{
                if(mActivePointerId == INVALID_POINTER) return false
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if(pointerIndex<0) return false
                val y = event.getY(pointerIndex)
                startDragging(y)
                if (mIsBeingDragged) {
                    val overScrollTop = (y - mInitialMotionY) * DRAG_RATE
                    if (overScrollTop > 0) {
                        moveSpinner(overScrollTop)
                    } else {
                        return false
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIndex = event.actionIndex
                if (pointerIndex < 0) {
                    Log.e("zgx", "Got ACTION_POINTER_DOWN event but have an invalid action index.")
                    return false
                }
                mActivePointerId = event.getPointerId(pointerIndex)
            }
            MotionEvent.ACTION_POINTER_UP ->{
                onSecondaryPointerUp(event)
            }
            MotionEvent.ACTION_CANCEL ->{
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
                mRefreshing = false
                mCurrentRefreshState = RefreshStatus.IDLE
                handlerRefreshChange()
                return false
            }
            MotionEvent.ACTION_UP ->{
                handlerActionUp()
                return false
            }

        }
        return true
    }
    fun handlerActionUp(){
        if(mCurrentRefreshState==RefreshStatus.RELEASE_REFRESH)
            mCurrentRefreshState = RefreshStatus.REFRESHING
        else mCurrentRefreshState = RefreshStatus.IDLE
        handlerRefreshChange()
    }
    fun startDragging(y:Float){
        val diffY = y - mInitialDownY
        if(diffY>mTouchSlop&&!mIsBeingDragged||mRefreshing){
            mIsBeingDragged = true
            mInitialMotionY = mInitialDownY + mTouchSlop
            ViewCompat.offsetTopAndBottom(mHeaderView, (diffY).toInt())
        }
    }
    private fun moveSpinner(overScrollTop: Float) {
        handlerRefreshState(mHeaderView.paddingTop)
        mHeaderView.setPadding(0,(overScrollTop).toInt()-mHeaderViewHeight-mTouchSlop,0,0)
    }
    private fun handlerRefreshState(scrollY:Int){
        val mBorderHeight = mHeaderHolder.refreshBorderTop()
        mHeaderHolder.handlerRefreshState(mHeaderView,scrollY)
        mCurrentRefreshState = if(scrollY<=mBorderHeight) RefreshStatus.PULL_DOWN
        else RefreshStatus.RELEASE_REFRESH
        handlerRefreshChange()
    }
    private fun handlerRefreshChange(){
        when(mCurrentRefreshState){
            RefreshStatus.IDLE ->{
                handlerChangeToHide()
            }
            RefreshStatus.PULL_DOWN ->{
               mHeaderHolder.refreshStatusPullDown(mHeaderView)
            }
            RefreshStatus.REFRESHING ->{
                handlerChangeToRefreshing()
            }
            RefreshStatus.RELEASE_REFRESH ->{
                mHeaderHolder.refreshStatusRelease(mHeaderView)
            }
        }
    }


    fun ensureTarget(){
        if(mTargetView==null){
            for (i in 0..childCount - 1) {
                val child = getChildAt(i)
                if (child != mHeaderView) {
                    mTargetView = child
                    break
                }
            }
        }
    }

    fun canChildScrollUp(): Boolean {
        if (mChildCanScrollUpCallBack != null) {
            return mChildCanScrollUpCallBack!!.canChildScrollUp(this, mTargetView!!)
        }
        if (mTargetView is ListView) {
            return ListViewCompat.canScrollList(mTargetView as ListView, -1)
        }
        return ViewCompat.canScrollVertically(mTargetView, -1)
    }


    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }
    // NestedScrollingParent
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return isEnabled&&!mRefreshing&&!mIsBeingDragged&&!canChildScrollUp()
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mTotalUnconsumed = 0f
        mNestedScrollInProgress = true
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - mTotalUnconsumed.toInt()
                mTotalUnconsumed = 0f
            } else {
                mTotalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
        }
        val parentConsumed = mParentScrollConsumed
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }
    //最后停止滚动的时候调用
    override fun onStopNestedScroll(target: View) {
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        mNestedScrollInProgress = false
        if (mTotalUnconsumed > 0) {
            handlerActionUp()
            mTotalUnconsumed = 0f
        }
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow)

        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (dy < 0 && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy).toFloat()
            moveSpinner(mTotalUnconsumed)
        }
    }
    // NestedScrollingChild
    //设置控件为嵌套滚动
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }
    //是否可以滚动
    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }
    //开启嵌套滚动流程，嵌套滚动前的准备工作
    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }
    //停止嵌套滚动
    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }
    //判断父View是否有嵌套滚动
    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }
    //子View自己滚动之前调用此方法，询问父View是否需要在子View之前进行滚动
    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow)
    }
    //子View自己处理滚动的时候调用此方法，询问父View是否还要进行余下的滚动
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
                                      dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun onNestedPreFling(target: View, velocityX: Float,
                                  velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float,
                               consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }
    //滑动时调用
    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }
    //滑动前调用
    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }
    private fun handlerChangeToRefreshing(){
        val refreshAnimator = ValueAnimator.ofFloat(mHeaderView.paddingTop.toFloat(),mHeaderHolder.refreshBorderTop())
        refreshAnimator.addUpdateListener { animation ->
            val animationValue = animation.animatedValue as Float
            mHeaderView.setPadding(0,animationValue.toInt(),0,0)
        }
        refreshAnimator.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                mTTRefreshLayoutListener?.onTTRefresh()
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
                mRefreshing = true
                mHeaderHolder.refreshStatusRefreshing(mHeaderView)
            }
        })
        refreshAnimator.duration = 200
        refreshAnimator.start()
    }
    private fun handlerChangeToHide(){
        val refreshAnimator = ValueAnimator.ofFloat(mHeaderView.paddingTop.toFloat(),-mHeaderViewHeight*1.0f)
        refreshAnimator.addUpdateListener { animation ->
            val animationValue = animation.animatedValue as Float
            mHeaderView.setPadding(0,animationValue.toInt(),0,0)
        }
        refreshAnimator.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
                mRefreshing = false
                mHeaderHolder.refreshStatusIDIE(mHeaderView)
            }
        })
        refreshAnimator.duration = 200
        refreshAnimator.start()
    }
    fun setRefreshing(){
        mCurrentRefreshState = RefreshStatus.REFRESHING
        handlerRefreshChange()
    }
    fun endRefresh(){
        mCurrentRefreshState = RefreshStatus.IDLE
        handlerRefreshChange()
    }
    enum class RefreshStatus{
        IDLE, PULL_DOWN, RELEASE_REFRESH, REFRESHING
    }
    fun setChildScrollUpCallBack(childScrollUpCallBack:ChildScrollUpCallBack){
        this.mChildCanScrollUpCallBack = childScrollUpCallBack
    }
    interface ChildScrollUpCallBack{
        fun canChildScrollUp(parent: PullRefreshLayout, mTargetView:View):Boolean
    }
    fun setTTRefreshLayoutListener(ttRefreshLayoutListener: TTRefreshLayoutListener){
        this.mTTRefreshLayoutListener = ttRefreshLayoutListener
    }
    interface TTRefreshLayoutListener{
        fun onTTRefresh()
    }

}