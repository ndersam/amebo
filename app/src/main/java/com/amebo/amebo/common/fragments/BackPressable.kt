package com.amebo.amebo.common.fragments

interface BackPressable {
    /**
     * @return true if back press has been handled
     */
    fun handledBackPress(): Boolean
}