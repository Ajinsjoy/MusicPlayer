package com.wac.mangoplayerpoc.util

open class Event<out T>(private val data: T) {
    var hasBeenHandled = false
        private set

    fun getContentHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data
}