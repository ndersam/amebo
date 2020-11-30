package com.amebo.amebo.screens.signin

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import com.amebo.amebo.R
import com.amebo.amebo.application.MainActivity
import com.amebo.amebo.common.EventObserver
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Resource
import com.amebo.amebo.common.extensions.*
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.databinding.SignInScreenBinding
import com.amebo.amebo.screens.accounts.UserManagementViewModel
import com.google.android.material.snackbar.Snackbar

class SignInScreen : BaseFragment(R.layout.sign_in_screen) {


    companion object {
        private const val USER_NAME = "user"
        fun newBundle(userName: String) = bundleOf(USER_NAME to userName)
    }

    private val binding: SignInScreenBinding by viewBinding(SignInScreenBinding::bind)
    private val bundledUserName get() = arguments?.getString(USER_NAME)
    private val viewModel by activityViewModels<UserManagementViewModel>()

    private var username: String = ""
    private var password: String = ""
    private var loginSuccessful = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundledUserName = this.bundledUserName
        if (bundledUserName != null) {
            username = bundledUserName
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.username.setText(username)
        binding.username.doOnTextChanged { text, _, _, _ ->
            onUserNameChanged(text!!)
        }
        binding.password.doOnTextChanged { text, _, _, _ ->
            onPassWordChanged(text!!)
        }
        if (binding.username.text.isNullOrBlank()) {
            binding.username.showKeyboard()
        } else {
            binding.password.showKeyboard()
        }
        notifyChanged()

        binding.btnSignIn.setOnClickListener { login() }
        binding.toolbar.setNavigationOnClickListener { router.back() }

        viewModel.loginEvent.observe(viewLifecycleOwner, EventObserver(::onLoginEventContent))
    }


    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        // reshow account list
        if (!loginSuccessful) {
            setFragmentResult(FragKeys.RESULT_RESHOW_ACCOUNT_LIST, bundleOf())
        }
        super.onDestroy()
    }


    private fun onUserNameChanged(s: CharSequence) {
        username = s.toString().trim()
        notifyChanged()
    }

    private fun onPassWordChanged(s: CharSequence) {
        password = s.toString().trim()
        notifyChanged()
    }

    private fun login() {
        viewModel.login(username, password)
    }

    private fun notifyChanged() {
        when (username.isNotBlank() && password.isNotBlank()) {
            true -> {
                binding.btnSignIn.isEnabled = true
                binding.password.isEnabled = true
                binding.username.isEnabled = true
            }
            false -> {
                binding.btnSignIn.isEnabled = false
                binding.password.isEnabled = true
                binding.username.isEnabled = true
            }
        }
    }

    private fun onLoginEventContent(resource: Resource<Unit>) {
        when (resource) {
            is Resource.Success -> onLoginSuccess()
            is Resource.Loading -> onLoginLoading()
            is Resource.Error -> onLoginError(resource)
        }
    }

    private fun onLoginSuccess() {
        binding.progress.isVisible = false

        loginSuccessful = true
        viewModel.setUser(username)

        router.back()

        val activity = requireActivity()
        activity.intent = activity.intent ?: Intent()
        activity.intent.action = MainActivity.INTENT_ACTION_GREET_USER
        activity.intent.putExtra(MainActivity.INTENT_EXTRA_USERNAME, username)
        activity.restart()
    }

    private fun onLoginLoading() {
        binding.btnSignIn.isEnabled = false
        binding.password.isEnabled = false
        binding.username.isEnabled = false
        binding.progress.isVisible = true
    }

    private fun onLoginError(error: Resource.Error<Unit>) {
        binding.btnSignIn.isEnabled = true
        binding.password.isEnabled = true
        binding.username.isEnabled = true
        binding.progress.isVisible = false

        binding.password.showKeyboard()

        Snackbar.make(
            requireView(), error.cause.getMessage(requireContext()),
            Snackbar.LENGTH_LONG
        ).show()
    }
}
