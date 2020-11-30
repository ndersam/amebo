package com.amebo.amebo.common.widgets

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.amebo.amebo.R

class StateLayout : FrameLayout {

    companion object {
        private var List<View?>.isVisible: Boolean
            get() = throw NotImplementedError("Do not use this")
            set(value) {
                this.forEach { it?.isVisible = value }
            }

        private fun TypedArray.addTo(
            idList: MutableList<Int>,
            styleRes: Int,
            vararg otherStyleRes: Int
        ) {
            idList.addAll(
                (intArrayOf(styleRes) + otherStyleRes)
                    .map { getResourceId(it, -1) }
                    .filter { it != -1 }
            )
        }
    }

    private val contentIds = mutableListOf<Int>()
    private var emptyIds = mutableListOf<Int>()
    private var progressIds = mutableListOf<Int>()
    private var failureIds = mutableListOf<Int>()
    private var listener: StateChangeListener? = null

    private lateinit var initialState: State
    var state: State = State.Content
        set(value) {
            field = value
            when (field) {
                State.Progress -> {
                    content.isVisible = false
                    empty.isVisible = false
                    failure.isVisible = false
                    progress.isVisible = true
                }
                State.Content -> {
                    progress.isVisible = false
                    empty.isVisible = false
                    failure.isVisible = false
                    content.isVisible = true
                }
                State.Empty -> {
                    progress.isVisible = false
                    content.isVisible = false
                    failure.isVisible = false
                    empty.isVisible = true
                }
                State.Failure -> {
                    progress.isVisible = false
                    content.isVisible = false
                    empty.isVisible = false
                    failure.isVisible = true
                }
            }
            listener?.onStateChanged(field)
            invalidate()
        }


    private val content = mutableListOf<View>()
    private val empty = mutableListOf<View>()
    private val progress = mutableListOf<View>()
    private val failure = mutableListOf<View>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context, attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initializeViews()
        state = initialState
    }


    private fun initialize(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StateLayout, 0, 0)
        a.addTo(
            contentIds,
            R.styleable.StateLayout_stateLayout_content,
            R.styleable.StateLayout_stateLayout_content1
        )
        a.addTo(
            emptyIds,
            R.styleable.StateLayout_stateLayout_empty,
            R.styleable.StateLayout_stateLayout_empty1
        )
        a.addTo(
            progressIds,
            R.styleable.StateLayout_stateLayout_progress,
            R.styleable.StateLayout_stateLayout_progress1
        )
        a.addTo(
            failureIds,
            R.styleable.StateLayout_stateLayout_failure,
            R.styleable.StateLayout_stateLayout_failure1
        )

        arrayOf(contentIds, emptyIds, progressIds, failureIds)
            .forEach {
                if (it != contentIds) {
                    a.addTo(
                        it,
                        R.styleable.StateLayout_stateLayout_not_content,
                        R.styleable.StateLayout_stateLayout_not_content1
                    )
                }
                if (it != emptyIds) {
                    a.addTo(
                        it,
                        R.styleable.StateLayout_stateLayout_not_empty,
                        R.styleable.StateLayout_stateLayout_not_empty1
                    )
                }
                if (it != progressIds) {
                    a.addTo(
                        it,
                        R.styleable.StateLayout_stateLayout_not_progress,
                        R.styleable.StateLayout_stateLayout_not_progress1
                    )
                }
                if (it != failureIds) {
                    a.addTo(
                        it,
                        R.styleable.StateLayout_stateLayout_not_failure,
                        R.styleable.StateLayout_stateLayout_not_failure1
                    )
                }
            }
        initialState = State.of(
            a.getInt(
                R.styleable.StateLayout_stateLayout_initialState,
                State.Progress.value
            )
        )
        a.recycle()
    }

    fun content() {
        state = State.Content
    }

    fun empty() {
        state = State.Empty
    }

    fun failure() {
        state = State.Failure
    }

    private fun initializeViews() {
        val adder = { idList: List<Int>, viewList: MutableList<View> ->
            viewList.addAll(idList.map { findViewById(it) })
        }
        adder(contentIds, content)
        adder(failureIds, failure)
        adder(emptyIds, empty)
        adder(progressIds, progress)
    }

    fun loading() {
        state = State.Progress
    }

//    override fun generateDefaultLayoutParams(): LayoutParams {
//        return MyLayoutParams(context, null)
//    }
//
//    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
//        return MyLayoutParams(context, attrs)
//    }

    enum class State(val value: Int) {
        Progress(0),
        Empty(2),
        Content(1),
        Failure(3);

        companion object {
            fun of(value: Int): State = when (value) {
                0 -> Progress
                1 -> Content
                2 -> Empty
                3 -> Failure
                else -> throw IllegalArgumentException("Wrong value `$value`")
            }

            fun from(collection: Collection<*>) = if (collection.isNotEmpty()) Content else Empty
        }

    }

    interface StateChangeListener {
        fun onStateChanged(current: State)
    }

//    private inner class MyLayoutParams(c: Context, attrs: AttributeSet?) : LayoutParams(c, attrs) {
//
//        init {
//
//            if (attrs != null) {
//                val a = c.obtainStyledAttributes(attrs, R.styleable.StateLayout_Layout)
//                val parent = a.getResourceId(R.styleable.StateLayout_Layout_layout_content, -1)
//                Timber.d("Rees: $parent")
//                a.recycle()
//            }
//        }
//    }
}