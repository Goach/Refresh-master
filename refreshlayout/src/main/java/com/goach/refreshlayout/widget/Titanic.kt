package com.goach.refreshlayout.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.view.animation.LinearInterpolator

/**
 * 来源Titanic
 */
class Titanic {

    private var animatorSet: AnimatorSet? = null
    var animatorListener: Animator.AnimatorListener? = null

    fun start(textView: TitanicTextView) {

        val animate = Runnable {
            textView.isSinking = true

            val maskXAnimator = ObjectAnimator.ofFloat(textView, "maskX", 0f, 200f)
            maskXAnimator.repeatCount = ValueAnimator.INFINITE
            maskXAnimator.duration = 1000
            maskXAnimator.startDelay = 0

            val h = textView.height

            val maskYAnimator = ObjectAnimator.ofFloat(textView, "maskY", h / 2.0f, -h / 2.0f)
            maskYAnimator.repeatCount = ValueAnimator.INFINITE
            maskYAnimator.repeatMode = ValueAnimator.REVERSE
            maskYAnimator.duration = 1000
            maskYAnimator.startDelay = 0

            animatorSet = AnimatorSet()
            animatorSet!!.playTogether(maskXAnimator, maskYAnimator)
            animatorSet!!.interpolator = LinearInterpolator()
            animatorSet!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    textView.isSinking = false

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        textView.postInvalidate()
                    } else {
                        textView.postInvalidateOnAnimation()
                    }

                    animatorSet = null
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })


            if (animatorListener != null) {
                animatorSet!!.addListener(animatorListener)
            }

            animatorSet!!.start()
        }

        if (!textView.isSetUp) {
            textView.animationSetupCallback = object : TitanicTextView.AnimationSetupCallback {
                override fun onSetupAnimation(target: TitanicTextView) {
                    animate.run()
                }
            }
        } else {
            animate.run()
        }
    }

    fun cancel() {
        if (animatorSet != null) {
            animatorSet!!.cancel()
        }
    }
}