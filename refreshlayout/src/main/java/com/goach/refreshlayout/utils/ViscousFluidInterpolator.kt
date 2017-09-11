package com.goach.refreshlayout.utils

import android.view.animation.Interpolator

/**
 * Goach All Rights Reserved
 *User: Goach
 *Date: 2017/9/7 0007
 *Time: 16:38
 */
class ViscousFluidInterpolator : Interpolator {

    override fun getInterpolation(input: Float): Float {
        val interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input)
        if (interpolated > 0) {
            return interpolated + VISCOUS_FLUID_OFFSET
        }
        return interpolated
    }

    companion object {
        /** Controls the viscous fluid effect (how much of it).  */
        private val VISCOUS_FLUID_SCALE = 8.0f

        private val VISCOUS_FLUID_NORMALIZE: Float
        private val VISCOUS_FLUID_OFFSET: Float

        init {

            // must be set to 1.0 (used in viscousFluid())
            VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f)
            // account for very small floating-point error
            VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f)
        }

        private fun viscousFluid(x: Float): Float {
            var x = x
            x *= VISCOUS_FLUID_SCALE
            if (x < 1.0f) {
                x -= 1.0f - Math.exp((-x).toDouble()).toFloat()
            } else {
                val start = 0.36787944117f   // 1/e == exp(-1)
                x = 1.0f - Math.exp((1.0f - x).toDouble()).toFloat()
                x = start + x * (1.0f - start)
            }
            return x
        }
    }
}