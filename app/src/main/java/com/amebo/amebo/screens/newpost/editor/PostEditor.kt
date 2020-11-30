package com.amebo.amebo.screens.newpost.editor

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.amebo.amebo.common.EmojiGetter
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.popup.Popup
import java.lang.ref.WeakReference


class PostEditor(
    private val fragment: Fragment,
    private val pref: Pref,
    editText: EditText,
    actionMenu: EditActionMenu,
    private val listener: Listener,
) : EditActionMenu.Listener, LifecycleObserver {

    private val editTextRef = WeakReference(editText)
    private val editText get() = editTextRef.get()!!

    private val actionMenuRef = WeakReference(actionMenu)
    private val actionMenu get() = actionMenuRef.get()!!

    val canUndo get() = historyManager.canUndo
    val canRedo get() = historyManager.canRedo


    private val historyManager = EditHistoryManager(editText)


    init {
        actionMenu.listener = this
        editText.customSelectionActionModeCallback = StyleCallback(this)
        fragment.viewLifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy() {
        historyManager.disconnect()
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    fun onLifecyclePause() {

    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
    fun onLifecycleResume() {

    }

    fun setFilesCount(count: Int) {
        actionMenu.setEditActionBadgeCount(EditAction.AttachFile, count)
    }

    @SuppressLint("SetTextI18n")
    fun insert(text: String) {
        val end = editText.selectionEnd
        if (end == -1) {
            val new = editText.text.toString() + " " + text
            editText.setText(new)
            editText.setSelection(new.length)
        } else {
            val rawText = editText.text.toString()
            val begin = rawText.substring(0, end)
            editText.setText(rawText.substring(0, end) + " " + text + " " + rawText.substring(end))
            editText.setSelection(begin.length + text.length + 2) // +2 for spaces
        }
    }

    fun onSaveInstanceState(bundle: Bundle) {
        historyManager.onSaveInstanceState(bundle)
    }

    fun onRestoreInstanceState(bundle: Bundle) {
        historyManager.onRestoreInstanceState(bundle)
    }

    override fun onEmoticonClicked(emoticon: EmojiGetter.Emoticon) {
        addTag(emoticon.ascii, "")
    }

    override fun onEditActionClicked(view: View?, action: EditAction) {
        when {
            action is EditAction.Preview -> listener.preview(editText.text.toString())
            action is EditAction.Settings -> listener.openSettings()
            action is EditAction.AttachFile -> attachFile()
            action is EditAction.Redo -> historyManager.redo()
            action is EditAction.Undo -> historyManager.undo()
            action is EditAction.Font -> fontFamily(view!!)
            action is EditAction.QuotePost -> quotePost()
            action.isDoubleTag -> {
                if (editText.hasFocus() && editText.isEnabled) addRegularTag(action)
            }
            !action.isDoubleTag && action.requiresFocus -> {
                if (!(editText.hasFocus() && editText.isEnabled)) return
                when (action) {
                    is EditAction.TextColor -> {
                        showColorPicker(view!!)
                    }
                    is EditAction.HR -> {
                        horizontalRule()
                    }
                    else -> {
                        throw IllegalStateException("You shouldn't be here")
                    }
                }
            }
        }
    }

    // TODO
    override fun allVisibleEditActions() = pref.allVisibleEditActions()
        .filter { it !is EditAction.QuotePost || listener.showQuotedPostAction }


    fun redo() {
        onEditActionClicked(null, EditAction.Redo)
    }

    fun undo() {
        onEditActionClicked(null, EditAction.Undo)
    }

    fun clearHistory() {
        historyManager.clearHistory()
    }


    private fun addRegularTag(action: EditAction) {
        addTag("[${action.start}]", "[/${action.end}]")
    }

    private fun addTag(startTag: String, endTag: String) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        var rawText = editText.text.toString()
        val begin = rawText.substring(0, start)
        val highlight = rawText.substring(start, end)
        val close = rawText.substring(end)
        rawText = begin + startTag + highlight + endTag + close
        val offset = if (start == end) rawText.length - (close.length + endTag.length)
        else  // set selection to middle of tag
            rawText.length - close.length // set selection to end of closingTag
        editText.setText(rawText)
        editText.setSelection(offset)
    }


    private fun attachFile() {
        listener.onDisplayDialog()
        listener.attachFile()
    }

    private fun quotePost() {
        listener.onDisplayDialog()
        listener.quotePost()
    }

    private fun showColorPicker(view: View) {
        Popup.colorPicker(fragment.requireActivity()) {
            val hexString = String.format("#%06X", (0xFFFFFF and it.value))
            addTag("[color=${hexString}]", "[/color]")
        }.showAtLocation(view)
    }

    private fun fontFamily(view: View) {
        Popup.fontPicker(fragment.requireActivity()) {
            addTag("[font=${it.name(view.context)}]", "[/font]")
        }.showAtLocation(view)
    }

    private fun horizontalRule() {
        addTag("[hr]", "")
    }

    interface Listener {
        val showQuotedPostAction: Boolean get() = false
        fun preview(text: String)
        fun attachFile()
        fun onDismissDialog()
        fun onDisplayDialog()
        fun openSettings()
        fun quotePost() {

        }
    }

    private fun PopupWindow.showAtLocation(view: View) {
        if (Build.VERSION.SDK_INT >= 24) {
            val a = IntArray(2) //getLocationInWindow required array of size 2
            view.getLocationInWindow(a)
            showAtLocation(
                fragment.requireActivity().window.decorView,
                Gravity.NO_GRAVITY,
                a[0] - view.width,
                a[1] + view.height
            )
        } else {
            showAsDropDown(view)
        }
        update()
    }

    companion object {
        val EditAction.isDoubleTag: Boolean
            get() = !(this is EditAction.TextColor ||
                    this is EditAction.HR ||
                    this is EditAction.Preview ||
                    this is EditAction.AttachFile ||
                    this is EditAction.Emoticon ||
                    this is EditAction.Settings ||
                    this is EditAction.Undo ||
                    this is EditAction.Redo ||
                    this is EditAction.Font)

        val EditAction.isSingleTag: Boolean
            get() = this is EditAction.HR

        val EditAction.requiresFocus: Boolean
            get() = !(this is EditAction.Preview ||
                    this is EditAction.AttachFile ||
                    this is EditAction.Settings ||
                    this is EditAction.Undo ||
                    this is EditAction.Redo)
    }
}