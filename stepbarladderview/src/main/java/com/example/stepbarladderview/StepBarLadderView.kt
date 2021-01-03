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
        drawEndingLine(0f, size, 1, scale, paint)
        for (k in 0..(steps - 1)) {
            val sck : Float = sc2.divideScale(k, steps).sinify()
            save()
            translate(size / 2, gap * k)
            for (i in 0..1) {
                save()
                scale(1f - 2 * i, 1f)
                translate(size / 2 * (1 - sck), 0f)
                drawLine(0f, 0f, 0f, gap, paint)
                restore()
            }
            restore()
        }
        restore()
    }
    restore()
}

fun Canvas.drawSBLNode(i : Int, scale : Float, paint : Paint) {
    val w: Float = width.toFloat()
    val h: Float = height.toFloat()
    paint.color = colors[i]
    drawStepBarLadder(scale, w, h, paint)
}

class StepBarLadderView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}