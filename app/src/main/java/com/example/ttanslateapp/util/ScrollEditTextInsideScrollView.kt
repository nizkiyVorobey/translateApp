package com.example.ttanslateapp.util

import android.annotation.SuppressLint
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.google.android.material.textfield.TextInputEditText


class ScrollEditTextInsideScrollView() {
    companion object {
        @SuppressLint("ClickableViewAccessibility")
        fun allowScroll(target: TextInputEditText) {
            target.setOnTouchListener(View.OnTouchListener { v, event ->
                if (target.hasFocus()) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_SCROLL -> {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                            return@OnTouchListener true
                        }
                    }
                }
                false
            })
        }
    }
}