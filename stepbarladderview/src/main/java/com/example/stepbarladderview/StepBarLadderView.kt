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
