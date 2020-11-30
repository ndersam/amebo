package com.amebo.amebo.screens.imagepicker

sealed class ViewState {
    class ImagesAvailable(val images: List<ImageItem>) : ViewState()
    class Error(val reason: Reason) : ViewState() {
        enum class Reason { TOO_LARGE, UNKNOWN }
        companion object {
            val TooLarge = Error(Reason.TOO_LARGE)
            val Unknown = Error(Reason.UNKNOWN)
        }
    }
}
