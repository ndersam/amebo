package com.amebo.amebo.screens.youtube

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.viewBinding
import com.amebo.amebo.common.fragments.InjectableBaseDialogFragment
import com.amebo.amebo.databinding.FragmentYoutubeScreenBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener


class YoutubeScreen : InjectableBaseDialogFragment(R.layout.fragment_youtube_screen) {
    override val resizeView: Boolean = false
    private val binding by viewBinding(FragmentYoutubeScreenBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val videoId = requireArguments().getString(VIDEO_ID)!!
        lifecycle.addObserver(binding.player)

        binding.player.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                binding.player.exitFullScreen()
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })
    }

    companion object {
        private const val VIDEO_ID = "videoId"
        fun newBundle(videoId: String) = bundleOf(VIDEO_ID to videoId)
    }
}