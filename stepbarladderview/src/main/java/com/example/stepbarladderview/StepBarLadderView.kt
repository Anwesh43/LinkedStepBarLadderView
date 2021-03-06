package com.example.stepbarladderview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas

val parts : Int = 5
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 4.9f
val delay : Long = 20
val colors : Array<Int> = arrayOf(
    "#F44336",
    "#3F51B5",
    "#4CAF50",
    "#03A9F4",
    "#795548"
).map {
    Color.parseColor(it)
}.toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
val steps : Int = 4

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n: Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
fun Float.growEnd(i : Int, j : Int) : Float = j * divideScale(i, parts) + (1 - j) * divideScale(parts - 1 - i, parts)

fun Canvas.drawEndingLine(x : Float, y : Float, i : Int, scale : Float, paint : Paint) {
    val xEnd : Float = x * scale.divideScale(i, parts)
    val yEnd : Float = y * scale.divideScale(i, parts)
    val xStart : Float = x * scale.divideScale(parts - 1 - i, parts)
    val yStart : Float = y * scale.divideScale(parts - 1 - i, parts)
    drawLine(xStart, yStart, xEnd, yEnd, paint)
}

fun Canvas.drawStepBarLadder(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sc2 : Float = scale.divideScale(2, parts)
    val gap : Float = size / steps
    save()
    translate(w / 2 - size / 2, paint.strokeWidth)
    for (j in 0..1) {
        save()
        translate(0f, size * scale.growEnd(1, j))
        drawEndingLine(size, 0f, 0, scale, paint)
        restore()
        save()
        translate(size * j, 0f)
        drawEndingLine(0f, size, 1, scale, paint)
        restore()
    }
    if (sc2 > 0f && sc2 < 1f) {
        for (k in 0..(steps - 1)) {
            val sck: Float = sc2.divideScale(k, steps).sinify()
            save()
            translate(size / 2, gap * k)
            for (i in 0..1) {
                save()
                scale(1f - 2 * i, 1f)
                translate(size / 2 * (1 - sck), 0f)
                drawLine(
                    0f,
                    0f,
                    0f,
                    gap * Math.floor(scale.divideScale(1, parts).toDouble()).toFloat()
                            - gap * Math.floor(scale.divideScale(2, parts).toDouble()).toFloat(),
                    paint
                )
                restore()
            }
            restore()
        }
    }
    restore()
}

fun Canvas.drawSBLNode(i : Int, scale : Float, paint : Paint) {
    val w: Float = width.toFloat()
    val h: Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawStepBarLadder(scale, w, h, paint)
}

class StepBarLadderView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }


        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SBLNode(var i : Int, val state : State = State()) {

        private var next : SBLNode? = null
        private var prev : SBLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SBLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSBLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SBLNode {
            var curr : SBLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class StepBarLadder(var i : Int) {

        private var curr : SBLNode = SBLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : StepBarLadderView) {

        private var sb : StepBarLadder = StepBarLadder(0)
        private var animator : Animator = Animator(view)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            sb.draw(canvas, paint)
            animator.animate {
                sb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : StepBarLadderView {
            val view : StepBarLadderView = StepBarLadderView(activity)
            activity.setContentView(view)
            return view
        }
    }
}