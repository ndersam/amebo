package com.amebo.amebo.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import com.amebo.amebo.BuildConfig
import com.amebo.amebo.R
import com.amebo.amebo.common.AppUtil
import com.amebo.amebo.common.FragKeys
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.BaseFragment
import com.amebo.amebo.common.routing.Router
import com.amebo.amebo.common.routing.RouterFactory
import com.amebo.amebo.databinding.SettingsScreenBinding
import com.amebo.amebo.di.Injectable
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject


class SettingsScreen : BaseFragment(R.layout.settings_screen) {
    private val binding by viewBinding(SettingsScreenBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(FragKeys.RESULT_RESHOW_ACCOUNT_LIST) { _, _ ->
            router.toAccountList()
        }
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { router.back() }
        childFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat(), Injectable,
        Preference.OnPreferenceClickListener {

        @Inject
        lateinit var pref: Pref

        @Inject
        lateinit var routerFactory: RouterFactory

        lateinit var router: Router

        @Inject
        lateinit var viewModelFactory: ViewModelProvider.Factory

        private val viewModel by lazy {
            ViewModelProvider(
                this,
                viewModelFactory
            )[SettingsViewModel::class.java]
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = super.onCreateView(inflater, container, savedInstanceState)
            router = routerFactory.create(requireParentFragment())
            return view
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences_home)

            initializeBottomPreference()

            // set build version name
            getPref<Preference>(R.string.key_app_version).title =
                BuildConfig.VERSION_NAME

            // hide logged-in preferences
            getPref<BottomSheetPreference>(R.string.key_topiclist).isVisible = pref.isLoggedIn

            // hide logged-out preferences
            getPref<BottomSheetPreference>(R.string.key_topiclist_anonymous).isVisible =
                pref.isLoggedOut

            setOnClickListeners(
                R.string.key_switch_accounts,
                R.string.key_clear_read_topics,
                R.string.key_clear_search_history,
                R.string.key_privacy,
                R.string.key_licences,
                R.string.key_rate_app
            )
        }


        private fun setOnClickListeners(vararg key: Int) {
            key.forEach {
                getPref<Preference>(it).onPreferenceClickListener = this
            }
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            when (preference.key) {
                getString(R.string.key_switch_accounts) -> router.toAccountList()
                getString(R.string.key_clear_read_topics) -> {
                    viewModel.clearTopicHistory(viewLifecycleOwner) {
                        pref.notifyTopicHistoryCleared()
                        showSnackBar(R.string.read_topics_cleared)
                    }
                }
                getString(R.string.key_clear_search_history) -> {
                    viewModel.clearSearchHistory(viewLifecycleOwner) {
                        showSnackBar(R.string.search_history_cleared)
                    }
                }
                getString(R.string.key_privacy) -> {
                    val url = getString(R.string.privacy_policy_web_page)
                    AppUtil.openInCustomTabs(requireContext(), url)
                }
                getString(R.string.key_licences) -> {
                    val url = getString(R.string.licences_web_page)
                    AppUtil.openInCustomTabs(requireContext(), url)
                }
                getString(R.string.key_rate_app) -> {
                    AppUtil.openInStore(requireContext())
                }
                else -> return false
            }
            return true
        }


        private fun <T : Preference> getPref(@StringRes key: Int): T {
            return findPreference<T>(getString(key))!!
        }

        private fun showSnackBar(@StringRes stringRes: Int) {
            Snackbar.make(requireView(), stringRes, Snackbar.LENGTH_SHORT).show()
        }

        private fun initializeBottomPreference(preferenceGroup: PreferenceGroup = preferenceScreen) {
            val prefCount: Int = preferenceGroup.preferenceCount
            for (i in 0 until prefCount) {
                when (val preference = preferenceGroup.getPreference(i)) {
                    is BottomSheetPreference -> {
                        preference.pref = this.pref
                    }
                    is PreferenceCategory -> {
                        initializeBottomPreference(preference)
                    }
                }
            }
        }
    }
}
