/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2025 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.legacy.confetti.confetto

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.animation.Interpolator
import kotlin.math.sqrt

/**
 * Abstract class that represents a single confetto on the screen. This class holds all of the
 * internal states for the confetto to help it animate.
 *
 * All of the configured states are in milliseconds, e.g. pixels per millisecond for velocity.
 */
abstract class Confetto {

    private val matrix = Matrix()
    private val workPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val workPairs = FloatArray(2)
    private var currentVelocityX = 0.0f
    private var currentVelocityY = 0.0f
    private var currentRotationalVelocity = 0.0f

    // Configured coordinate states
    private var bound: Rect? = null
    private var initialDelay = 0L
    private var initialX = 0.0f
    private var initialY = 0.0f
    private var initialVelocityX = 0.0f
    private var initialVelocityY = 0.0f
    private var accelerationX = 0.0f
    private var accelerationY = 0.0f
    private var targetVelocityX: Float? = null
    private var targetVelocityY: Float? = null
    private var millisToReachTargetVelocityX: Long? = null
    private var millisToReachTargetVelocityY: Long? = null

    // Configured rotation states
    private var initialRotation = 0.0f
    private var initialRotationalVelocity = 0.0f
    private var rotationalAcceleration = 0.0f
    private var targetRotationalVelocity: Float? = null
    private var millisToReachTargetRotationalVelocity: Long? = null

    // Configured animation states
    private var ttl = 0L
    private var fadeOutInterpolator: Interpolator? = null
    private var millisToReachBound = 0.0f
    private var percentageAnimated = 0.0f

    // Current draw states
    private var currentX = 0.0f
    private var currentY = 0.0f
    private var currentRotation = 0.0f
    private var alpha = 0 // alpha is [0, 255]
    private var startedAnimation = false
    private var terminated = false

    // Touch events
    private var touchOverride = false
    private var velocityTracker: VelocityTracker? = null
    private var overrideX = 0.0f
    private var overrideY = 0.0f
    private var overrideVelocityX = 0.0f
    private var overrideVelocityY = 0.0f
    private var overrideDeltaX = 0.0f
    private var overrideDeltaY = 0.0f

    /**
     * This method should be called after all of the confetto's state variables are configured
     * and before the confetto gets animated.
     *
     * @param bound the space in which the confetto can display in.
     */
    fun prepare(bound: Rect?) {
        this.bound = bound

        millisToReachTargetVelocityX = computeMillisToReachTarget(targetVelocityX, initialVelocityX, accelerationX)
        millisToReachTargetVelocityY = computeMillisToReachTarget(targetVelocityY, initialVelocityY, accelerationY)
        millisToReachTargetRotationalVelocity =
            computeMillisToReachTarget(targetRotationalVelocity, initialRotationalVelocity, rotationalAcceleration)

        // Compute how long it would take to reach x/y bounds or reach TTL.
        millisToReachBound = (if (ttl >= 0) ttl else Long.MAX_VALUE).toFloat()
        val timeToReachXBound = computeBound(
            initialPos = initialX,
            velocity = initialVelocityX,
            acceleration = accelerationX,
            targetTime = millisToReachTargetVelocityX,
            targetVelocity = targetVelocityX,
            minBound = bound!!.left - width,
            maxBound = bound.right,
        )

        millisToReachBound = timeToReachXBound.toFloat().coerceAtMost(millisToReachBound)
        val timeToReachYBound = computeBound(
            initialPos = initialY,
            velocity = initialVelocityY,
            acceleration = accelerationY,
            targetTime = millisToReachTargetVelocityY,
            targetVelocity = targetVelocityY,
            minBound = bound.top - height,
            maxBound = bound.bottom,
        )

        millisToReachBound = timeToReachYBound.toFloat().coerceAtMost(millisToReachBound)
        configurePaint(workPaint)
    }

    private fun doesLocationIntercept(x: Float, y: Float): Boolean {
        return currentX <= x && x <= currentX + width && currentY <= y && y <= currentY + height
    }

    fun onTouchDown(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        return if (doesLocationIntercept(x, y)) {
            touchOverride = true
            overrideX = x
            overrideY = y
            overrideDeltaX = currentX - x
            overrideDeltaY = currentY - y
            velocityTracker = VelocityTracker.obtain().also { it.addMovement(event) }
            true
        } else {
            false
        }
    }

    fun onTouchMove(event: MotionEvent) {
        overrideX = event.x
        overrideY = event.y
        velocityTracker!!.addMovement(event)
        velocityTracker!!.computeCurrentVelocity(1)
        overrideVelocityX = velocityTracker!!.xVelocity
        overrideVelocityY = velocityTracker!!.yVelocity
    }

    fun onTouchUp(event: MotionEvent) {
        velocityTracker!!.addMovement(event)
        velocityTracker!!.computeCurrentVelocity(1)
        initialDelay = RESET_ANIMATION_INITIAL_DELAY
        initialX = event.x + overrideDeltaX
        initialY = event.y + overrideDeltaY
        initialVelocityX = velocityTracker!!.xVelocity
        initialVelocityY = velocityTracker!!.yVelocity
        initialRotation = currentRotation
        velocityTracker!!.recycle()
        velocityTracker = null
        prepare(bound)
        touchOverride = false
    }

    /**
     * @return the width of the confetto.
     */
    abstract val width: Int

    /**
     * @return the height of the confetto.
     */
    abstract val height: Int

    /**
     * Reset this confetto object's internal states so that it can be re-used.
     */
    fun reset() {
        initialDelay = 0L
        initialY = 0.0f
        initialX = 0.0f
        initialVelocityY = 0.0f
        initialVelocityX = 0.0f
        accelerationY = 0.0f
        accelerationX = 0.0f
        targetVelocityY = null
        targetVelocityX = null
        millisToReachTargetVelocityY = null
        millisToReachTargetVelocityX = null
        initialRotation = 0.0f
        initialRotationalVelocity = 0.0f
        rotationalAcceleration = 0.0f
        targetRotationalVelocity = null
        millisToReachTargetRotationalVelocity = null
        ttl = 0L
        millisToReachBound = 0.0f
        percentageAnimated = 0.0f
        fadeOutInterpolator = null
        currentY = 0.0f
        currentX = currentY
        currentVelocityY = 0.0f
        currentVelocityX = currentVelocityY
        currentRotation = 0.0f
        alpha = MAX_ALPHA
        startedAnimation = false
        terminated = false
    }

    /**
     * Hook to configure the global paint states before any animation happens.
     *
     * @param paint the paint object that will be used to perform all draw operations.
     */
    open fun configurePaint(paint: Paint) {
        paint.alpha = alpha
    }

    /**
     * Update the confetto internal state based on the provided passed time.
     *
     * @param passedTime time since the beginning of the animation.
     * @return whether this particular confetto is still animating.
     */
    fun applyUpdate(passedTime: Long): Boolean {
        if (initialDelay == RESET_ANIMATION_INITIAL_DELAY) initialDelay = passedTime
        val animatedTime = passedTime - initialDelay
        startedAnimation = animatedTime >= 0
        if (startedAnimation && !terminated) {
            computeDistance(
                pair = workPairs,
                t = animatedTime,
                xi = initialX,
                vi = initialVelocityX,
                ai = accelerationX,
                targetTime = millisToReachTargetVelocityX,
                vTarget = targetVelocityX,
            )
            currentX = workPairs[0]
            currentVelocityX = workPairs[1]
            computeDistance(
                pair = workPairs,
                t = animatedTime,
                xi = initialY,
                vi = initialVelocityY,
                ai = accelerationY,
                targetTime = millisToReachTargetVelocityY,
                vTarget = targetVelocityY,
            )
            currentY = workPairs[0]
            currentVelocityY = workPairs[1]
            computeDistance(
                pair = workPairs,
                t = animatedTime,
                xi = initialRotation,
                vi = initialRotationalVelocity,
                ai = rotationalAcceleration,
                targetTime = millisToReachTargetRotationalVelocity,
                vTarget = targetRotationalVelocity,
            )
            currentRotation = workPairs[0]
            currentRotationalVelocity = workPairs[1]
            alpha = if (fadeOutInterpolator != null) {
                val interpolatedTime = fadeOutInterpolator!!.getInterpolation(animatedTime / millisToReachBound)
                (interpolatedTime * MAX_ALPHA).toInt()
            } else {
                MAX_ALPHA
            }
            terminated = !touchOverride && animatedTime >= millisToReachBound
            percentageAnimated = 1.0f.coerceAtMost(animatedTime / millisToReachBound)
        }
        return !terminated
    }

    private fun computeDistance(pair: FloatArray, t: Long, xi: Float, vi: Float, ai: Float, targetTime: Long?, vTarget: Float?) {
        // velocity with constant acceleration
        val vX = ai * t + vi
        pair[1] = vX
        val x = if (targetTime == null || t < targetTime) {
            // distance covered with constant acceleration
            xi + vi * t + 0.5f * ai * t * t
        } else {
            // distance covered with constant acceleration + distance covered with max velocity
            xi + vi * targetTime + 0.5f * ai * targetTime * targetTime + (t - targetTime) * vTarget!!
        }
        pair[0] = x
    }

    /**
     * Primary method for rendering this confetto on the canvas.
     *
     * @param canvas the canvas to draw on.
     */
    fun draw(canvas: Canvas) {
        if (touchOverride) {
            // Replace time-calculated velocities with touch-velocities
            currentVelocityX = overrideVelocityX
            currentVelocityY = overrideVelocityY
            draw(canvas, overrideX + overrideDeltaX, overrideY + overrideDeltaY, currentRotation, percentageAnimated)
        } else if (startedAnimation && !terminated) {
            draw(canvas, currentX, currentY, currentRotation, percentageAnimated)
        }
    }

    private fun draw(canvas: Canvas, x: Float, y: Float, rotation: Float, percentAnimated: Float) {
        canvas.save()
        canvas.clipRect(bound!!)
        matrix.reset()
        workPaint.alpha = alpha
        drawInternal(canvas, matrix, workPaint, x, y, rotation, percentAnimated)
        canvas.restore()
    }

    /**
     * Subclasses need to override this method to optimize for the way to draw the appropriate
     * confetto on the canvas.
     *
     * @param canvas          the canvas to draw on.
     * @param matrix          an identity matrix to use for draw manipulations.
     * @param paint           the paint to perform canvas draw operations on. This paint has already been
     * configured via [.configurePaint].
     * @param x               the x position of the confetto relative to the canvas.
     * @param y               the y position of the confetto relative to the canvas.
     * @param rotation        the rotation (in degrees) to draw the confetto.
     * @param percentAnimated the percentage [0.0f, 1f] of animation progress for this confetto.
     */
    abstract fun drawInternal(
        canvas: Canvas,
        matrix: Matrix,
        paint: Paint,
        x: Float,
        y: Float,
        rotation: Float,
        percentAnimated: Float,
    )

    //region Helper methods to set all of the necessary values for the confetto.
    fun setInitialDelay(value: Long) {
        initialDelay = value
    }

    fun setInitialX(value: Float) {
        initialX = value
    }

    fun setInitialY(value: Float) {
        initialY = value
    }

    fun setInitialVelocityX(value: Float) {
        initialVelocityX = value
    }

    fun setInitialVelocityY(value: Float) {
        initialVelocityY = value
    }

    fun setAccelerationX(value: Float) {
        accelerationX = value
    }

    fun setAccelerationY(value: Float) {
        accelerationY = value
    }

    fun setTargetVelocityX(value: Float?) {
        targetVelocityX = value
    }

    fun setTargetVelocityY(value: Float?) {
        targetVelocityY = value
    }

    fun setInitialRotation(value: Float) {
        initialRotation = value
    }

    fun setInitialRotationalVelocity(value: Float) {
        initialRotationalVelocity = value
    }

    fun setRotationalAcceleration(value: Float) {
        rotationalAcceleration = value
    }

    fun setTargetRotationalVelocity(value: Float?) {
        targetRotationalVelocity = value
    }

    fun setTTL(value: Long) {
        ttl = value
    }

    fun setFadeOut(fadeOutInterpolator: Interpolator?) {
        this.fadeOutInterpolator = fadeOutInterpolator
    }
    //endregion

    private companion object {
        const val MAX_ALPHA = 255
        const val RESET_ANIMATION_INITIAL_DELAY: Long = -1

        fun computeMillisToReachTarget(targetVelocity: Float?, initialVelocity: Float, acceleration: Float): Long? {
            return if (targetVelocity != null) {
                if (acceleration != 0.0f) {
                    val time = ((targetVelocity - initialVelocity) / acceleration).toLong()
                    if (time > 0) time else 0
                } else {
                    if (targetVelocity < initialVelocity) 0L else null
                }
            } else {
                null
            }
        }

        fun computeBound(
            initialPos: Float,
            velocity: Float,
            acceleration: Float,
            targetTime: Long?,
            targetVelocity: Float?,
            minBound: Int,
            maxBound: Int,
        ): Long {
            return if (acceleration == 0.0f) {
                computeBoundWithoutAcceleration(initialPos, velocity, targetTime, targetVelocity, minBound, maxBound)
            } else {
                // non-zero acceleration
                val bound = if (acceleration > 0) maxBound else minBound
                if (targetTime == null || targetTime < 0) {
                    computeBoundWithoutTargetTime(initialPos, velocity, acceleration, bound)
                } else {
                    computeBoundWithTargetTime(initialPos, velocity, acceleration, targetTime, targetVelocity, bound)
                }
            }
        }

        fun computeBoundWithoutAcceleration(
            initialPos: Float,
            velocity: Float,
            targetTime: Long?,
            targetVelocity: Float?,
            minBound: Int,
            maxBound: Int,
        ): Long {
            val actualVelocity = if (targetTime == null) velocity else targetVelocity!!
            val bound = if (actualVelocity > 0) maxBound else minBound
            return if (actualVelocity == 0.0f) {
                Long.MAX_VALUE
            } else {
                val time = ((bound - initialPos) / actualVelocity).toDouble()
                if (time > 0) time.toLong() else Long.MAX_VALUE
            }
        }

        fun computeBoundWithoutTargetTime(initialPos: Float, velocity: Float, acceleration: Float, bound: Int): Long {
            // https://www.wolframalpha.com/input/
            // ?i=solve+for+t+in+(d+%3D+x+%2B+v+*+t+%2B+0.5+*+a+*+t+*+t)
            val tmp = sqrt((2 * acceleration * bound - 2 * acceleration * initialPos + velocity * velocity).toDouble())
            val firstTime = (-tmp - velocity) / acceleration
            if (firstTime > 0) return firstTime.toLong()
            val secondTime = (tmp - velocity) / acceleration
            return if (secondTime > 0.0f) secondTime.toLong() else Long.MAX_VALUE
        }

        fun computeBoundWithTargetTime(
            initialPos: Float,
            velocity: Float,
            acceleration: Float,
            targetTime: Long,
            targetVelocity: Float?,
            bound: Int,
        ): Long {
            // d = x + v * tm + 0.5 * a * tm * tm + tv * (t - tm)
            // d - x - v * tm - 0.5 * a * tm * tm = tv * t - tv * tm
            // d - x - v * tm - 0.5 * a * tm * tm + tv * tm = tv * t
            // t = (d - x - v * tm - 0.5 * a * tm * tm + tv * tm) / tv
            val time = (bound - initialPos - velocity * targetTime - 0.5 * acceleration
                    * targetTime * targetTime + targetVelocity!! * targetTime) / targetVelocity
            return if (time > 0.0f) time.toLong() else Long.MAX_VALUE
        }
    }
}
