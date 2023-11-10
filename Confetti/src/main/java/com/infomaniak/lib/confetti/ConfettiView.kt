/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
package com.infomaniak.lib.confetti

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.infomaniak.lib.confetti.confetto.Confetto

/**
 * A helper temporary view that helps render the confetti. This view will attach itself to the
 * view root, perform the animation, and then once all of the confetti has completed its animation,
 * it will automatically remove itself from the parent.
 */
class ConfettiView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), OnLayoutChangeListener {

    private var confetti: List<Confetto>? = null
    private var terminated = false
    private var touchEnabled = false
    private var draggedConfetto: Confetto? = null

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        elevation = context.resources.getDimensionPixelOffset(R.dimen.confetti_elevation).toFloat()
    }

    /**
     * Sets the list of confetti to be animated by this view.
     *
     * @param confetti the list of confetti to be animated.
     */
    fun bind(confetti: List<Confetto>?) {
        this.confetti = confetti
    }

    /**
     * @param touchEnabled whether or not to enable touch
     * @see ConfettiManager.setTouchEnabled
     */
    fun setTouchEnabled(touchEnabled: Boolean) {
        this.touchEnabled = touchEnabled
    }

    /**
     * Terminate the current running animation (if any) and remove this view from the parent.
     */
    fun terminate() {
        if (!terminated) {
            terminated = true
            parent.requestLayout()
        }
    }

    /**
     * Reset the internal state of this view to allow for a new confetti animation.
     */
    fun reset() {
        terminated = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        (parent as ViewGroup).apply {
            removeOnLayoutChangeListener(this@ConfettiView)
            addOnLayoutChangeListener(this@ConfettiView)
        }

        // If we did not bind before attaching to the window, that means this ConfettiView no longer
        // has a ConfettiManager backing it and should just be terminated.
        if (confetti == null) terminate()
    }

    override fun onLayoutChange(view: View, l: Int, t: Int, r: Int, b: Int, oldL: Int, oldT: Int, oldR: Int, oldB: Int) {
        if (terminated) {
            (parent as ViewGroup).apply {
                removeViewInLayout(this@ConfettiView)
                removeOnLayoutChangeListener(this@ConfettiView)
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!terminated) {
            canvas.save()
            for (confetto in confetti!!) confetto.draw(canvas)
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        if (!touchEnabled) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (confetto in confetti!!) {
                    if (confetto.onTouchDown(event)) {
                        draggedConfetto = confetto
                        handled = true
                        break
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggedConfetto != null) {
                    draggedConfetto!!.onTouchMove(event)
                    handled = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (draggedConfetto != null) {
                    draggedConfetto!!.onTouchUp(event)
                    draggedConfetto = null
                    handled = true
                }
            }
        }

        return handled || super.onTouchEvent(event)
    }
}
