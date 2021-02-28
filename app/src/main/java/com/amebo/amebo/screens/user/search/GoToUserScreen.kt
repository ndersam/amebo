package com.amebo.amebo.screens.user.search

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.hideKeyboard
import com.amebo.amebo.common.extensions.showKeyboard
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectableBaseDialogFragment
import com.amebo.amebo.databinding.SearchUserScreenBinding
import com.amebo.core.domain.User

class GoToUserScreen : InjectableBaseDialogFragment(R.layout.search_user_screen) {

    private val binding by viewBinding(SearchUserScreenBinding::bind)

    private val searchTerm get() = binding.searchBox.text.toString().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnGo.setOnClickListener {
            gotoUserProfile(searchTerm)
        }
        binding.btnCancel.setOnClickListener {
            hideKeyboard()
            dismiss()
        }

        binding.searchBox.doOnTextChanged { _, _, _, _ ->
            binding.btnGo.isEnabled = isValidUsername(searchTerm)
        }

        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (isValidUsername(searchTerm) && actionId == EditorInfo.IME_ACTION_GO) {
                gotoUserProfile(searchTerm)
                true
            } else {
                false
            }
        }
        binding.searchBox.showKeyboard()
    }

    private fun gotoUserProfile(username: String) {
        hideKeyboard()
        router.toUser(User(username))
        dismiss()
    }

    private fun isValidUsername(text: String) = VALID_USER_NAME_REGEX.matches(text)


    companion object {
        // Valid username for new nairaland username
        //        private val VALID_USER_NAME_REGEX = Regex("^(?=.*[a-zA-Z])[\\da-zA-Z]{4,15}$")

        // Old Nairaland users have urls like http://www.nairaland.com/profile/175
        private val VALID_USER_NAME_REGEX = Regex("[a-zA-Z0-9]+")
    }
}