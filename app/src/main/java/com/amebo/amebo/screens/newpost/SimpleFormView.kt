package com.amebo.amebo.screens.newpost

import android.annotation.SuppressLint
import android.graphics.Rect
import android.text.InputType
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.EditText
import android.widget.FrameLayout.LayoutParams
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.setMargins
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.TouchEventDispatcher
import com.amebo.amebo.common.extensions.*
import com.amebo.amebo.common.widgets.OuchView
import com.amebo.amebo.common.widgets.StateLayout
import com.amebo.amebo.screens.imagepicker.ImageItem
import com.amebo.amebo.screens.newpost.editor.EditActionMenu
import com.amebo.amebo.screens.newpost.editor.PostEditor
import com.amebo.core.domain.PostListDataPage
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

open class SimpleFormView(
    fragment: Fragment,
    pref: Pref,
    editActionMenu: EditActionMenu,
    editMessage: EditText,
    editTitle: EditText,
    toolbar: Toolbar,
    ouchView: OuchView,
    stateLayout: StateLayout,
    val listener: IFormView.Listener
) : IFormView {

    private val editActionMenuRef = WeakReference(editActionMenu)
    private val editMessageRef = WeakReference(editMessage)
    private val editTitleRef = WeakReference(editTitle)
    private val toolbarRef = WeakReference(toolbar)
    private val ouchViewRef = WeakReference(ouchView)
    private val stateLayoutRef = WeakReference(stateLayout)

    private val editActionMenu get() = editActionMenuRef.get()!!
    private val editMessage get() = editMessageRef.get()!!
    private val editTitle get() = editTitleRef.get()!!
    private val toolbar get() = toolbarRef.get()!!
    private val ouchView get() = ouchViewRef.get()!!
    private val stateLayout get() = stateLayoutRef.get()!!
    private val postEditor: PostEditor

    private val context get() = editMessage.context
    private var snackBar: WeakReference<Snackbar>? = null

    init {
        editTitle.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        editTitle.doOnTextChanged { text, _, _, _ ->
            listener.setPostTitle(text.toString())
            invalidateMenu()
        }

        editMessage.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        editMessage.cursorAtEnd()
        editMessage.doOnTextChanged { text, _, _, _ ->
            listener.setPostBody(text.toString())
            invalidateMenu()
        }
        postEditor = PostEditor(fragment, pref, editMessage, editActionMenu, listener)

        ouchView.setButtonClickListener { listener.retryLastRequest() }

        toolbar.setMenu(R.menu.menu_new_post)
        invalidateMenu()
        toolbar.setOnMenuItemClickListener(::onOptionsItemSelected)
        toolbar.setNavigationOnClickListener { listener.goBack() }

        fragment.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onCreate() {
                val dispatcher = fragment.activity as? TouchEventDispatcher
                dispatcher?.register(::onActivityTouchEvent)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                val dispatcher = fragment.activity as? TouchEventDispatcher
                dispatcher?.unRegister(::onActivityTouchEvent)
            }
        })
    }

    fun invalidateMenu() {
        val menu = toolbar.menu
        with(menu.findItem(R.id.submit)) {
            isEnabled = listener.canSubmit
            applyEnableTint(context)
        }

        with(menu.findItem(R.id.redo)) {
            isEnabled = canRedo()
            applyEnableTint(context)
        }

        with(menu.findItem(R.id.undo)) {
            isEnabled = canUndo()
            applyEnableTint(context)
        }
    }

    private fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.submit -> listener.submit()
            R.id.show_rules -> listener.showRules()
            R.id.undo -> undo()
            R.id.redo -> redo()
            else -> return false
        }
        return true
    }

    override fun onFormSuccess(success: Resource.Success<FormData>) {
        setFormData(success.content)
        invalidateMenu()
    }

    override fun onFormError(error: Resource.Error<FormData>) {
        if (error.content == null) {
            ouchView.setState(error.cause)
            stateLayout.failure()
        } else {
            setFormData(error.content)
            showSnackBar(error.cause.getMessage(context))
        }
        invalidateMenu()
    }

    override fun onFormLoading(loading: Resource.Loading<FormData>) {
        if (loading.content == null) {
            stateLayout.loading()
        } else {
            setFormData(loading.content)
        }
        invalidateMenu()
    }

    override fun onSubmissionSuccess(success: Resource.Success<PostListDataPage>) {
        invalidateMenu()
    }


    override fun onSubmissionError(error: Resource.Error<PostListDataPage>) {
        editTitle.isEnabled = true
        editMessage.isEnabled = true
        showSnackBar(error.cause.getMessage(context))
        invalidateMenu()
    }

    @SuppressLint("ShowToast")
    override fun onSubmissionLoading(loading: Resource.Loading<PostListDataPage>) {
        editTitle.isEnabled = false
        editMessage.isEnabled = false

        snackBar = WeakReference(
            Snackbar.make(
                stateLayout,
                R.string.submitting,
                Snackbar.LENGTH_INDEFINITE
            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    snackBar = null
                }
            }).apply {
                val layout = view as Snackbar.SnackbarLayout
                val progress = ProgressBar(context).apply {
                    isIndeterminate = true
                    val params = LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT,
                        GravityCompat.END
                    )
                    params.setMargins(8)
                    layoutParams = params
                }

                layout.addView(progress)
                show()
            }
        )

        invalidateMenu()
    }

    override fun onExistingImageRemovalError(error: Resource.Error<ImageItem.Existing>) {
        showSnackBar(context.getString(R.string.error_removing_image))
    }

    override fun onExistingImageRemovalLoading(loading: Resource.Loading<ImageItem.Existing>) {

    }

    override fun onExistingImageRemovalSuccess(success: Resource.Success<ImageItem.Existing>) {

    }

    override fun setFileCount(count: Int) {
        postEditor.setFilesCount(count)
    }

    override fun showKeyboard(onBody: Boolean) {
        if (!onBody && editTitle.text.isBlank()) {
            editTitle.showKeyboard()
        } else {
            editMessage.showKeyboard()
        }
    }

    override fun refreshEditMenu() = editActionMenu.refresh()

    override fun insertText(content: String) {
        postEditor.insert(content)
    }

    override fun undo() {
        postEditor.undo()
        invalidateMenu()
    }

    override fun redo() {
        postEditor.redo()
        invalidateMenu()
    }

    override fun canRedo(): Boolean {
        return postEditor.canRedo
    }

    override fun canUndo(): Boolean {
        return postEditor.canUndo
    }

    protected open fun setFormData(formData: FormData) {
        editTitle.setText(formData.title)
        editMessage.setText(formData.body)
        postEditor.clearHistory()
        stateLayout.content()
        showKeyboard()
    }

    private fun showSnackBar(message: String) {
        snackBar?.get()?.dismiss()
        Snackbar.make(
            stateLayout,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun onActivityTouchEvent(e: MotionEvent) {
        if (e.action == MotionEvent.ACTION_DOWN) {
            val snackBar = snackBar?.get() ?: return
            val rect = Rect()
            snackBar.view.getHitRect(rect)
            if (!rect.contains(e.x.toInt(), e.y.toInt())) {
                snackBar.dismiss()
            }
        }
    }

}