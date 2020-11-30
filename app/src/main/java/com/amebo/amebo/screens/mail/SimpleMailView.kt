package com.amebo.amebo.screens.mail

import androidx.core.widget.doOnTextChanged
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.applyEnableTint
import com.amebo.amebo.common.extensions.context
import com.amebo.amebo.common.extensions.showKeyboard
import com.amebo.amebo.common.extensions.snack
import com.amebo.amebo.databinding.FragmentMailScreenBinding
import com.amebo.amebo.common.Resource
import java.lang.ref.WeakReference

class SimpleMailView(
    binding: FragmentMailScreenBinding,
    private val listener: MailView.Listener,
    title: String
) : MailView {
    private val bindingRef = WeakReference(binding)
    private val binding get() = bindingRef.get()!!

    init {
        binding.ouchView.setButtonClickListener { listener.retryLastRequest() }

        binding.toolbar.setNavigationOnClickListener { listener.goBack() }
        binding.toolbar.title = title
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.submit -> listener.submit()
                R.id.show_rules -> listener.showRules()
                else -> return@setOnMenuItemClickListener false
            }
            true
        }

        binding.editMessage.doOnTextChanged { text, _, _, _ ->
            listener.setBody(text.toString())
        }
        binding.editSubject.doOnTextChanged { text, _, _, _ ->
            listener.setSubject(text.toString())
        }
    }

    override fun onFormLoadEventSuccess(resource: Resource.Success<MailFormData>) {
        val data = resource.content

        binding.editSubject.setText(data.title)
        binding.editSubject.isEnabled = data.editable

        binding.editMessage.setText(data.body)
        binding.editMessage.isEnabled = data.editable

        binding.stateLayout.content()

        showKeyboard()
    }

    override fun onFormLoadEventLoading(resource: Resource.Loading<MailFormData>) {
        binding.stateLayout.loading()
    }

    override fun onFormLoadEventError(resource: Resource.Error<MailFormData>) {
        binding.ouchView.setState(resource.cause)
        binding.stateLayout.failure()
    }

    override fun onFormSubmissionEventSuccess(resource: Resource.Success<*>) {
        binding.snack(R.string.mail_sent)
    }

    override fun onFormSubmissionEventError(resource: Resource.Error<*>) {
        binding.snack(resource.cause)
        binding.stateLayout.failure()
    }

    override fun onFormSubmissionEventLoading(resource: Resource.Loading<*>) {

    }

    override fun setSubmissionEnabled(isEnabled: Boolean) {
        val submit = binding.toolbar.menu.findItem(R.id.submit)
        submit.isEnabled = isEnabled
        submit.applyEnableTint(binding.context)
    }

    override fun showKeyboard() {
        // show keyboard only if editing is enabled
        if (!listener.isEditingEnabled) return

        if (binding.editSubject.text.isNullOrBlank()) {
            binding.editSubject.showKeyboard()
        } else {
            binding.editMessage.showKeyboard()
        }
    }
}