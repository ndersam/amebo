package com.amebo.amebo.common

import android.view.MotionEvent

interface TouchEventDispatcher {
    fun register(listener: (MotionEvent) -> Unit)
    fun unRegister(listener: (MotionEvent) -> Unit)
}