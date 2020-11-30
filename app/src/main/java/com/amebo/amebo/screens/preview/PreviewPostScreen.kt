package com.amebo.amebo.screens.preview

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.databinding.FragmentPreviewPostScreenBinding


class PreviewPostScreen : DialogFragment(R.layout.fragment_preview_post_screen) {
    private val binding by viewBinding(FragmentPreviewPostScreenBinding::bind)
    private val html get() = requireArguments().getString(HTML)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadDataWithBaseURL(
            "file:///android_res/drawable",
            html!!,
            "text/html",
            "utf-8",
            null
        )
    }

    companion object {
        private const val HTML = "html"
        fun newBundle(html: String) = bundleOf(HTML to html)
    }
}
