package com.amebo.amebo.screens.newpost.newpost

import android.text.InputType
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.showKeyboard
import com.amebo.amebo.databinding.NewPostScreenBinding
import com.amebo.amebo.screens.newpost.FormData
import com.amebo.amebo.screens.newpost.IFormView
import com.amebo.amebo.screens.newpost.NewPostFormData
import com.amebo.amebo.screens.newpost.SimpleFormView
import com.amebo.core.domain.Topic
import java.lang.ref.WeakReference

class NewPostView(
    fragment: Fragment,
    pref: Pref,
    binding: NewPostScreenBinding,
    topic: Topic,
    listener: Listener
) :
    SimpleFormView(
        fragment = fragment,
        pref = pref,
        editActionMenu = binding.editActionMenu,
        editMessage = binding.editMessage,
        editTitle = binding.editTitle,
        listener = listener,
        ouchView = binding.ouchView,
        stateLayout = binding.stateLayout,
        toolbar = binding.toolbar
    ) {

    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!

    init {
        binding.background.setOnClickListener {
            binding.editMessage.showKeyboard()
        }

        binding.editTitle.isEnabled = false
        binding.editTitle.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        binding.editTitle.setText(topic.title, TextView.BufferType.EDITABLE)
        binding.followTopic.isVisible = true
        binding.followTopic.setOnCheckedChangeListener { _, isChecked ->
            listener.setFollowTopic(isChecked)
            invalidateMenu()
        }
    }

    override fun setFormData(formData: FormData) {
        super.setFormData(formData)
        val data = formData as NewPostFormData
        binding.followTopic.isChecked = data.followTopic
    }

    interface Listener : IFormView.Listener {
        fun setFollowTopic(followTopic: Boolean)
    }
}