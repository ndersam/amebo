package com.amebo.amebo.common.extensions

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

fun EditText.showKeyboard(cursorAtEnd: Boolean = true) {
    focusAndShowKeyboard()
    if (cursorAtEnd) {
        cursorAtEnd()
    }
}

fun EditText.cursorAtEnd() {
    setSelection(text.length)
}

fun EditText.disableCopyPaste() {
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onDestroyActionMode(mode: ActionMode?) = Unit
    }
}
