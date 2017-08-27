package com.goach.refreshlayout.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import com.goach.refreshlayout.R
import com.goach.refreshlayout.utils.PathMeasureHelper
import com.goach.refreshlayout.utils.cling
import com.goach.refreshlayout.utils.initPaint
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.AnimatorSet
import com.nineoldandroids.animation.ValueAnimator
import org.jetbrains.anko.dip
import rx.Observable

/**
 * Created by iGoach on 2017/7/22.
 * 绘制今日头条外框边线圆角矩形以及内部View
 * PathMeasure的使用
 */
class BoxView : ViewGroup{
    private var DEFAULT_WIDTH = context.dip(25)
    private var DEFAULT_HEIGHT = context.dip(25)
    private var DEFAULT_RADIUS = context.dip(10)
    private var DEFAULT_PADDING = context.dip(3)
    private var DEFAULT_BORDER_COLOR = Color.BLACK
    private var DEFAULT_BOX_COLOR = Color.parseColor("#E2E2E2")
    private var DEFAULT_PAINT_W = 2
    private var DEFAULT_SHORT_LINE_WEIGHT = 0.5f
    private var mRadius:Int = DEFAULT_RADIUS
    private var mPadding:Int = DEFAULT_PADDING
    private var mBorderColor:Int = DEFAULT_BORDER_COLOR
    private var mBoxColor:Int = DEFAULT_BOX_COLOR
    private var mPaintWidth:Int = DEFAULT_PAINT_W
    private var mShortLineWeight:Float = DEFAULT_SHORT_LINE_WEIGHT
    private var mAnimTime:Long = 300

    private var mPathMeasureHelper:PathMeasureHelper
    private var mBorderPaint = Paint()
    private var mBorderPath = Path()
    private var mInsidePath = Path()
    private var mLine1Path = Path()
    private var mLine2Path = Path()
    private var mLine3Path = Path()

    private lateinit var squareView:SquareView
    private lateinit var shortLineView:LineView
    private lateinit var longLineView:LineView

    private var mSquareMoveX:Float = 0f
    private var mSquareMoveY:Float = 0f
    private var mShortLineMoveX:Float = 0f
    private var mShortLineMoveY:Float = 0f

    private lateinit var step1Anim:ValueAnimator
    private lateinit var step2Anim:ValueAnimator
    private lateinit var step3Anim:ValueAnimator
    private lateinit var step4Anim:ValueAnimator
    private lateinit var animatorSet:AnimatorSet
    private var isTurning = true

    constructor(context: Context):this(context,null)
    constructor(context: Context,attributeSet: AttributeSet?):this(context,attributeSet,0)
    constructor(context: Context,attributeSet: AttributeSet?,defaultStyle:Int):super(context,
            attributeSet,defaultStyle){
        setWillNotDraw(false)
        mPathMeasureHelper = PathMeasureHelper()
        initView()
        initAnim()

    }

    fun initView(){
        squareView = SquareView(context)
        addView(squareView,ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT))
        shortLineView = LineView(context)
        addView(shortLineView,ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT))
        longLineView = LineView(context)
        addView(longLineView,ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT))
    }
    fun initAnim(){
        step1Anim = createValueAnimation { animationValue ->
            squareView.setProgress(1f)
            shortLineView.setProgress(1f)
            longLineView.setProgress(1f)
            updateInsidePath(mSquareMoveX*animationValue,0f)
            updateShortLine(0f,mShortLineMoveY*animationValue,animationValue)
            updateLongLine(-mShortLineMoveX*animationValue,mShortLineMoveY*(1-animationValue),1-animationValue)
        }
        step2Anim = createValueAnimation { animationValue ->
            squareView.setProgress(1f)
            shortLineView.setProgress(1f)
            longLineView.setProgress(1f)
            updateInsidePath(mSquareMoveX,mSquareMoveY*animationValue)
            updateShortLine(-mShortLineMoveX*animationValue,mShortLineMoveY,1-animationValue)
            updateLongLine(-mShortLineMoveX*(1-animationValue),0f,animationValue)
        }
        step3Anim = createValueAnimation { animationValue ->
            squareView.setProgress(1f)
            shortLineView.setProgress(1f)
            longLineView.setProgress(1f)
            updateInsidePath(context.cling(0f,mSquareMoveX,mSquareMoveX*(1-animationValue)),mSquareMoveY)
            updateShortLine(-mShortLineMoveX*(1-animationValue),mShortLineMoveY*(1-animationValue), animationValue )
            updateLongLine(0f,mShortLineMoveY*animationValue,1-animationValue)
        }
        step4Anim = createValueAnimation{ animationValue ->
            squareView.setProgress(1f)
            shortLineView.setProgress(1f)
            longLineView.setProgress(1f)
            updateInsidePath(0f,mSquareMoveY*(1-animationValue))
            updateShortLine(0f,0f,1-animationValue)
            updateLongLine(0f,mShortLineMoveY,animationValue)
        }
        animatorSet = AnimatorSet()
        animatorSet.playSequentially(step1Anim,step2Anim,step3Anim,step4Anim)
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        var realWidth = DEFAULT_WIDTH
        var realHeight = DEFAULT_HEIGHT
        if(modeWidth == MeasureSpec.EXACTLY){
            realWidth = sizeWidth
        }
        if(modeHeight == MeasureSpec.EXACTLY){
            realHeight = sizeHeight
        }
        setMeasuredDimension(realWidth,realHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Observable.from(0..childCount-1)
                .map { getChildAt(it) }
                .forEach { it.layout(0, 0, width, height) }
        mPadding = context.cling(0,width/2,mPadding)
        mShortLineWeight = context.cling(0f,1f,mShortLineWeight)
        mPaintWidth = context.cling(0,width/6,mPaintWidth)
        mRadius = context.cling(0,width/6,mRadius)
        squareView.setPaint1(mBorderColor,mPaintWidth*1.0f)
        squareView.setPaint2(mBoxColor,mPaintWidth*1.0f)
        shortLineView.setPaint(mBorderColor,mPaintWidth*1.0f)
        longLineView.setPaint(mBorderColor,mPaintWidth*1.0f)
        setPaint(mBorderColor,mPaintWidth*1.0f)
        initBorderPath()
        updateInsidePath(0f,0f)
        updateShortLine(0f,0f,0f)
        updateLongLine(0f,mShortLineMoveY,1.0f)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        mPathMeasureHelper.resetPath()
        mPathMeasureHelper.drawPath(canvas)
    }

    fun initBorderPath(){
        val radius2Length = 2*mRadius*1.0f
        val paintWidth = mPaintWidth/2.0f
        mBorderPath.moveTo(width - paintWidth,mRadius+paintWidth)
        mBorderPath.arcTo(RectF(width - radius2Length - paintWidth, paintWidth,
                width*1.0f - paintWidth,radius2Length+paintWidth),0f,-90f)
        mBorderPath.lineTo(radius2Length+paintWidth,paintWidth)
        mBorderPath.arcTo(RectF(paintWidth,paintWidth,
                radius2Length+paintWidth,radius2Length+paintWidth),-90f,-90f)
        mBorderPath.lineTo(paintWidth,height - radius2Length - paintWidth)
        mBorderPath.arcTo(RectF(paintWidth,height - radius2Length - paintWidth,
                radius2Length +paintWidth,height - paintWidth),-180f,-90f)
        mBorderPath.lineTo(width - radius2Length - paintWidth,height*1.0f - paintWidth)
        mBorderPath.arcTo(RectF(width - radius2Length - paintWidth,height - radius2Length - paintWidth,
                width - paintWidth,height - paintWidth),-270f,-90f)
        mBorderPath.lineTo(width- paintWidth,mRadius+paintWidth)
        mPathMeasureHelper.initPath(mBorderPath)
    }
    fun updateInsidePath(translationX:Float,translationY:Float){
        val littlePadding = (height - 2*mPadding - mPaintWidth*8)/5.0f
        val squareWidth = (height - 2*mPadding - littlePadding - mPaintWidth*2)*(1 - mShortLineWeight)
        val squareHeight = 2*littlePadding+3*mPaintWidth
        val paintWidth = mPaintWidth/2.0f
        val left = mPadding+ mPaintWidth*1.0f + translationX
        val top =  mPadding+ mPaintWidth +translationY
        mSquareMoveX = (width - mPaintWidth - mPadding - squareWidth) - (mPadding+ mPaintWidth*1.0f)
        mSquareMoveY = (height - mPaintWidth - mPadding - squareHeight) - (mPadding+ mPaintWidth*1.0f)
        mInsidePath.reset()
        mInsidePath.moveTo(left+squareWidth,top+paintWidth)
        mInsidePath.lineTo(left,top+paintWidth)
        mInsidePath.lineTo(left,top + squareHeight-paintWidth)
        mInsidePath.lineTo(left+squareWidth,top + squareHeight-paintWidth)
        mInsidePath.lineTo(left+squareWidth,top*1.0f)
        squareView.setPath(mInsidePath)
    }
    fun updateShortLine(translationX:Float,translationY:Float,animWeight:Float){
        calculatePath(translationX,translationY,animWeight)
        shortLineView.path1Line(mLine1Path)
        shortLineView.path2Line(mLine2Path)
        shortLineView.path3Line(mLine3Path)
    }

    fun updateLongLine(translationX:Float,translationY:Float,animWeight:Float){
        calculatePath(translationX,translationY,animWeight)
        longLineView.path1Line(mLine1Path)
        longLineView.path2Line(mLine2Path)
        longLineView.path3Line(mLine3Path)
    }
    fun calculatePath(translationX:Float,translationY:Float,animWeight:Float){
        val littlePadding = (height - 2*mPadding - mPaintWidth*8)/5.0f
        val totalLength = width - 2*mPadding - mPaintWidth*2
        val lineWidth = (totalLength - littlePadding)*mShortLineWeight
        val realWidth = lineWidth+ (totalLength-lineWidth)*animWeight
        val squareHeight = 2*littlePadding+3*mPaintWidth
        val paintWidth = mPaintWidth/2.0f
        val right = width - mPaintWidth - mPadding*1.0f +translationX
        val top = mPaintWidth + mPadding + paintWidth + translationY
        mShortLineMoveX = lineWidth+littlePadding
        mShortLineMoveY = squareHeight+littlePadding
        mLine1Path.reset()
        mLine1Path.moveTo(right - realWidth , top)
        mLine1Path.lineTo(right,top)
        mLine2Path.reset()
        mLine2Path.moveTo(right - realWidth, top+ mPaintWidth + littlePadding)
        mLine2Path.lineTo(right, top+mPaintWidth+ littlePadding)
        mLine3Path.reset()
        mLine3Path.moveTo(right - realWidth, top+ 2*mPaintWidth  + 2*littlePadding)
        mLine3Path.lineTo(right, top+ 2*mPaintWidth  + 2*littlePadding)
    }
    fun setPaint(color:Int = DEFAULT_BORDER_COLOR,strokeWidth:Float = DEFAULT_PAINT_W*1.0f){
        mBorderPaint.initPaint(color,strokeWidth){this.style = Paint.Style.STROKE}
        mPathMeasureHelper.initPaint(mBorderPaint)
    }
    fun setProgress(progress:Float){
        this.mPathMeasureHelper.progress(progress)
        this.squareView.setProgress(progress)
        this.shortLineView.setProgress(context.cling(0f,1f, progress*2.0f))
        this.longLineView.setProgress(context.cling(0f,1f, progress*2.0f - 1.0f))
        invalidate()
    }
    fun startAnim(){
        if(!animatorSet.isStarted){
            animatorSet.cancel()
            animatorSet.addListener(object :AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    if(isTurning)
                        animation?.start()
                    else{
                        squareView.setProgress(0f)
                        shortLineView.setProgress(0f)
                        longLineView.setProgress(0f)
                    }
                }
            })
            animatorSet.start()
            isTurning = true
        }
    }
    fun stopAnim(){
        animatorSet.removeAllListeners()
        animatorSet.cancel()
        isTurning = false
    }
    fun createValueAnimation(onAnimationUpdate:((animatedValue:Float) -> Unit)? = null):ValueAnimator{
        val stepAnim = ValueAnimator.ofFloat(0f,1f)
        stepAnim.addUpdateListener { animation ->
            if(onAnimationUpdate!=null)
                onAnimationUpdate(animation.animatedValue as Float)
        }
        stepAnim.startDelay = 300
        stepAnim.duration = mAnimTime
        return stepAnim
    }
    fun setRadius(radius:Int):BoxView{
        this.mRadius = radius
        return this
    }
    fun setBoxPadding(padding:Int):BoxView{
        this.mPadding = padding
        return this
    }
    fun setBorderColor(borderColor:Int):BoxView{
        this.mBorderColor = borderColor
        setPaint(mBorderColor)
        return this
    }
    fun setBoxColor(boxColor:Int):BoxView{
        this.mBoxColor = boxColor
        return this
    }
    fun setPaintWidth(paintWidth:Int):BoxView{
        this.mPaintWidth = paintWidth
        return this
    }
    fun setShortLineWeight(weight:Float):BoxView{
        this.mShortLineWeight = weight
        return this
    }
    fun setAnimTime(animTime:Long):BoxView{
        this.mAnimTime = animTime
        return this
    }
}