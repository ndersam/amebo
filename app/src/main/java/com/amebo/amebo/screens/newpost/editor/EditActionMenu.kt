package com.amebo.amebo.screens.newpost.editor

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.amebo.amebo.R
import com.amebo.amebo.common.EmojiGetter
import com.amebo.amebo.databinding.LayoutEditActionMenuBinding

class EditActionMenu : LinearLayout {
    private lateinit var actionsAdapter: EditActionsAdapter
    private lateinit var emoticonsAdapter: EmoticonAdapter
    private var isEmoticonsShown = false
    private lateinit var binding: LayoutEditActionMenuBinding
    var listener: Listener? = null
        set(value) {
            field = value
            if (value != null) {
                setEditActions(value.allVisibleEditActions())
            }
        }

    constructor(context: Context?) : super(
        context,
        null,
        R.attr.editActionMenuStyle
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs,
        R.attr.editActionMenuStyle
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, R.attr.editActionMenuStyle) {
        init()
    }


    private fun init() {
        orientation = HORIZONTAL
        val view = inflate(context, R.layout.layout_edit_action_menu, this)
        binding = LayoutEditActionMenuBinding.bind(view)
        val actions = listener?.allVisibleEditActions() ?: return
        setEditActions(actions)
    }

    private fun setEditActions(editActions: List<EditAction>) {
        actionsAdapter = EditActionsAdapter(editActions,
            listener = object :
                EditActionsAdapter.Listener {
                override fun onItemClicked(view: View, type: EditAction) {
                    listener?.onEditActionClicked(view, type)
                }
            })
        binding.actionsRV.minimumWidth = (binding.actionsRV.parent as View).width
        binding.actionsRV.adapter = actionsAdapter

        emoticonsAdapter =
            EmoticonAdapter(
                EmojiGetter.EMOTICONS,
                object :
                    EmoticonAdapter.EmoticonListener {
                    override fun onEmoticonClicked(emoticon: EmojiGetter.Emoticon) {
                        listener?.onEmoticonClicked(emoticon)
                    }
                })
        binding.emoticonsRV.adapter = emoticonsAdapter
        binding.toggleEmoticon.setOnClickListener { showEmoticonPicker(!isEmoticonsShown) }
    }

    fun refresh() {
        val editActions = listener?.allVisibleEditActions() ?: return
        actionsAdapter.setActions(editActions)
    }


    interface Listener {
        fun onEmoticonClicked(emoticon: EmojiGetter.Emoticon)
        fun onEditActionClicked(view: View?, action: EditAction)
        fun allVisibleEditActions(): List<EditAction>
    }

    fun showEmoticons() {
        showEmoticonPicker(true)
    }

    fun hideEmoticons() {
        showEmoticonPicker(false)
    }

    fun setEditActionBadgeCount(editAction: EditAction, count: Int) {
        actionsAdapter.setBadgeCount(count, editAction)
    }

    private fun showEmoticonPicker(show: Boolean) {
        isEmoticonsShown = show
        val duration = 200
        val view1: View?
        val view2: View?
        if (show) {
            view1 = binding.emoticonsRV
            view2 = binding.actionsRV
        } else {
            view1 = binding.actionsRV
            view2 = binding.emoticonsRV
        }
        val anim1 = ObjectAnimator.ofFloat(view1, "alpha", 0.0f, 1.0f)
            .setDuration(duration.toLong())
        anim1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view1.visibility = View.VISIBLE
            }
        })
        val anim2 = ObjectAnimator.ofFloat(view2, "alpha", 1.0f, 0.0f)
            .setDuration(duration.toLong())
        anim2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view2.visibility = View.GONE
            }
        })
        val set = AnimatorSet()
        set.playTogether(anim1, anim2)
        set.start()
    }
}