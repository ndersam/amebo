package com.amebo.amebo.screens.mail

import android.os.Bundle
import androidx.lifecycle.Observer
import com.amebo.amebo.R
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.hideKeyboard
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.AuthenticationRequired
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.FragmentMailScreenBinding
import javax.inject.Inject
import javax.inject.Provider

/**
 * @param U - data returned from form submission. For mail to user, this could be User's data
 */
abstract class BaseMailScreen<U : Any> : BaseFragment(R.layout.fragment_mail_screen),
    AuthenticationRequired,
    MailView.Listener {
    val binding: FragmentMailScreenBinding by viewBinding(FragmentMailScreenBinding::bind)
    abstract val viewModel: BaseMailScreenViewModel<*, U>

    @Inject
    lateinit var viewProvider: Provider<MailView>
    private lateinit var view: MailView

    override fun onViewCreated(savedInstanceState: Bundle?) {
        view = viewProvider.get()
        router.setOnDialogDismissListener(viewLifecycleOwner) {
            view.showKeyboard()
        }

        viewModel.formLoadingEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFormLoadEventContent)
        )
        viewModel.formSubmissionEvent.observe(
            viewLifecycleOwner,
            EventObserver(::onFormSubmissionEventContent)
        )
        viewModel.submissionEnabledEvent.observe(
            viewLifecycleOwner,
            Observer(view::setSubmissionEnabled)
        )
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun submit() = viewModel.submit()

    override fun showRules() = router.toPostingRules()

    override val isEditingEnabled: Boolean get() = viewModel.editingEnabled

    override fun setSubject(subject: String) = viewModel.setSubject(subject)

    override fun setBody(text: String) = viewModel.setBody(text)

    override fun goBack() {
        hideKeyboard()
        router.back()
    }

    override fun retryLastRequest() {
        viewModel.retry()
    }

    private fun onFormLoadEventContent(resource: Resource<MailFormData>) {
        when (resource) {
            is Resource.Success -> view.onFormLoadEventSuccess(resource)
            is Resource.Error -> view.onFormLoadEventError(resource)
            is Resource.Loading -> view.onFormLoadEventLoading(resource)
        }
    }

    private fun onFormSubmissionEventContent(resource: Resource<U>) {
        when (resource) {
            is Resource.Success -> {
                view.onFormSubmissionEventSuccess(resource)
                onSubmissionSuccess(resource.content)
                router.back()
            }
            is Resource.Loading -> view.onFormSubmissionEventLoading(resource)
            is Resource.Error -> view.onFormSubmissionEventError(resource)
        }
    }


    open fun onSubmissionSuccess(content: U) {

    }
}