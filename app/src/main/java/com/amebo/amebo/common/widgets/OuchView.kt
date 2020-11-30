package com.amebo.amebo.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.amebo.amebo.R
import com.amebo.amebo.common.extensions.getDrawableRes
import com.amebo.amebo.common.extensions.getMessage
import com.amebo.core.domain.ErrorResponse

class OuchView : LinearLayout {
    private val imageView: ImageView
    private val title: TextView
    private val subtitle: TextView
    private val button: View

    var buttonIsVisible: Boolean
        get() = button.isVisible
        set(value) {
            button.isVisible = value
        }

    constructor(context: Context) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        inflate(context, R.layout.failure_view, this)
        imageView = findViewById(R.id.image)
        title = findViewById(R.id.title)
        subtitle = findViewById(R.id.subtitle)
        button = findViewById(R.id.btnRetry)
    }

    fun setDrawable(drawableRes: Int) {
        imageView.setImageResource(drawableRes)
    }

    fun setButtonClickListener(listener: (View) -> Unit) {
        button.setOnClickListener(listener)
    }

    fun performButtonClick() {
        button.performClick()
    }


    fun setState(state: State) {
//        button.isVisible = state == State.NetworkError
        val subtitle = if (state.subtitleRes != -1)
            context.getString(state.subtitleRes)
        else ""
        val title = if (state.titleRes != -1)
            context.getString(state.titleRes)
        else ""
        setState(state.drawableRes, subtitle, title)
    }

    fun setState(errorResponse: ErrorResponse) {
        val title = errorResponse.getMessage(context)
        setState(errorResponse.getDrawableRes(), title)
    }

    fun empty() {
        setState(State.Empty)
    }

    fun networkError() {
        setState(State.NetworkError)
    }

    fun setState(
        drawableRes: Int,
        subtitle: String,
        title: String = context.getString(R.string.something_went_wrong)
    ) {
        this.title.text = title
        this.subtitle.text = subtitle
        if (drawableRes == -1) {
            imageView.setImageDrawable(null)
        } else {
            imageView.setImageResource(drawableRes)
        }

    }

    sealed class State(
        val drawableRes: Int,
        val subtitleRes: Int,
        val titleRes: Int = R.string.something_went_wrong
    ) {
        object NetworkError :
            State(R.drawable.ic_error404fun_5, R.string.network_error_subtitle)

        object AccessDenied : State(R.drawable.ic_error404fun_3, R.string.access_denied)
        object Unknown : State(R.drawable.ic_error404fun_18, R.string.unknown_error)
        object NotFound : State(R.drawable.ic_error404fun_3, R.string.not_found)
        object Empty : State(R.drawable.ic_error404fun_8, R.string.quiet_here)
    }
}