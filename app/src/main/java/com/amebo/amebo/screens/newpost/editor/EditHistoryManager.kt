package com.amebo.amebo.screens.newpost.editor

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.EditText
import kotlinx.parcelize.Parcelize
import java.lang.ref.WeakReference
import java.util.*


/**
 * Copied from https://github.com/jtbuaa/iReaderHome/blob/master/app/src/main/java/fi/iki/asb/android/logo/TextViewUndoRedo.java
 * with slight modifications
 */
class EditHistoryManager(editText: EditText) {

    private val editTextRef = WeakReference(editText)
    private val editText: EditText get() = editTextRef.get()!!

    /**
     * Is undo/redo being performed? This signals if an undo/redo operation is currently being
     * performed. Changes in the text during undo/redo are not recorded because it would mess
     * up the history.
     */
    private var isUndoOrRedo = false
    private val history = EditHistory()
    private val listener = TextChangeListener().apply { editText.addTextChangedListener(this) }

    val canUndo get() = history.position > 0
    val canRedo get() = history.position < history.history.size

    fun disconnect() {
        editText.removeTextChangedListener(listener)
    }

    /**
     * If size is negative, then history size is only limited by the device memory.
     */
    fun setMaxHistory(maxHistorySize: Int) {
        history.maxHistorySize = maxHistorySize
    }

    fun clearHistory() {
        history.clear()
    }

    fun undo() {
        val edit = history.previous ?: return
        val text = editText.editableText
        val start = edit.start
        val end = start + (edit.after?.length ?: 0)

        isUndoOrRedo = true
        text.replace(start, end, edit.before)
        isUndoOrRedo = false

        removeSuggestions(text)
        Selection.setSelection(text, (edit.before?.length ?: 0) + start)
    }

    fun redo() {
        val edit = history.next ?: return
        val text = editText.editableText
        val start = edit.start
        val end = start + (edit.before?.length ?: 0)

        isUndoOrRedo = true
        text.replace(start, end, edit.after)
        isUndoOrRedo = false

        removeSuggestions(text)
        Selection.setSelection(text, (edit.after?.length ?: 0) + start)
    }

    /**
     * Get rid of underlines inserted when editor tries to come up
     * with a suggestion.
     */
    private fun removeSuggestions(text: Editable) {
        text.getSpans(0, text.length, UnderlineSpan::class.java)
            .forEach { text.removeSpan(it) }
    }

    fun onSaveInstanceState(outState: Bundle) {
        val bundle = Bundle()
        val historyItems = arrayListOf<EditItem>().apply {
            addAll(history.history)
        }
        bundle.putParcelableArrayList("historyItems", historyItems)
        bundle.putInt("historySize", history.history.size)
        bundle.putInt("maxSize", history.maxHistorySize)
        bundle.putString("hash", editText.text.toString().hashCode().toString())
        bundle.putInt("position", history.position)
        outState.putBundle(BUNDLE_EDIT_HISTORY_MANAGER, bundle)
    }

    fun onRestoreInstanceState(savedState: Bundle?) {
        val bundle = savedState?.getBundle(BUNDLE_EDIT_HISTORY_MANAGER) ?: return

        val hash = bundle.getString("hash", null) ?: return
        if (hash.toInt() != editText.text.toString().hashCode()) return

        clearHistory()
        setMaxHistory(bundle.getInt("maxSize"))

        val historySize = bundle.getInt("historySize")
        if (historySize == -1) return

        val historyBundle = bundle.getParcelableArrayList<EditItem>("historyItems")
        historyBundle?.forEach {
            history.add(it)
        }

        val position = bundle.getInt("position")
        history.position = position
    }

    private class EditHistory {
        /**
         * The position of next [EditItem] to be retrieved.
         * If [previous] [EditItem] has not been retrieved, this equals
         * the size of [history].
         */
        var position: Int = 0

        val history: LinkedList<EditItem> = LinkedList()

        var maxHistorySize: Int = -1
            set(value) {
                field = value
                if (field >= 0) {
                    trimHistory()
                }
            }

        val previous: EditItem?
            get() {
                if (position == 0) return null
                position--
                return history[position]
            }

        val next: EditItem?
            get() {
                if (position >= history.size) {
                    return null
                }
                val item = history[position]
                position++
                return item
            }


        /**
         * Clear history
         */
        fun clear() {
            position = 0
            history.clear()
        }

        /**
         * Adds a new edit operation to the history at the current
         * position. If executed after a call to [previous] removes
         * all the future history (elements with positions >= current
         * history position).
         */
        fun add(item: EditItem) {
            while (history.size > position) {
                history.removeLast()
            }
            history.add(item)
            position++

            if (maxHistorySize >= 0) {
                trimHistory()
            }
        }

        /**
         * Trim history when it exceeds max history size.
         */
        private fun trimHistory() {
            while (history.size > maxHistorySize) {
                history.removeFirst()
                position--
            }
            position = position.coerceAtLeast(0)
        }
    }


    /**
     * Models the changes performed by a single edit operation.
     * Modification is applied at [start] position and replaces [CharSequence]
     * [before] with [CharSequence] [after]
     */
    @Parcelize
    class EditItem(val start: Int, val before: CharSequence?, val after: CharSequence?) : Parcelable


    private inner class TextChangeListener : TextWatcher {
        private var before: CharSequence? = null
        private var after: CharSequence? = null
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (isUndoOrRedo) return
            before = s?.subSequence(start, start + count)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isUndoOrRedo) return
            after = s?.subSequence(start, start + count)
            history.add(EditItem(start, this.before, this.after))
        }
    }

    companion object {
        private val BUNDLE_EDIT_HISTORY_MANAGER = EditHistoryManager::class.java.name
    }
}