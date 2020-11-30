package com.amebo.amebo.screens.settings

class BottomPreferenceData<T : Any>(
    val title: String,
    val key: String,
    val defaultValuePosition: Int,
    val entries: List<String>,
    val entryValues: List<T>
)