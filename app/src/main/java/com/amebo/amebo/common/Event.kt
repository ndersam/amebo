package com.amebo.amebo.common

import androidx.lifecycle.Observer

class Event<out T : Any>(private val content: T) {

    private var hasHasBeenHandled = false

    fun peekContent() = content


    fun getContentIfNotHandled(): T? = if (hasHasBeenHandled) null else {
        hasHasBeenHandled = true
        content
    }
}

class EventObserver<T : Any>(private val onEventUnhandledContent: (T) -> Unit) :
    Observer<Event<T>> {
    override fun onChanged(event: Event<T>) {
        val content = event.getContentIfNotHandled() ?: return
        onEventUnhandledContent(content)
    }
}