package com.amebo.amebo.screens.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.amebo.amebo.R
import com.amebo.amebo.common.Pref
import com.amebo.amebo.common.asTheme

@Suppress("unused")
class BottomSheetPreference : Preference {
    private var selectedIndex: Int = -1
    private lateinit var view: ViewGroup
    private lateinit var entries: List<String>
    private lateinit var entryValues: List<Int>
    var pref: Pref? = null


    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context) : super(context) {
        initialize(null)
    }

    private fun initialize(attrs: AttributeSet?) {
        widgetLayoutResource = R.layout.preference_bottom_sheet
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetPreference)
        val entriesId = ta.getResourceId(R.styleable.BottomSheetPreference_entries, -1)
        val entryValuesId = ta.getResourceId(R.styleable.BottomSheetPreference_entryValues, -1)
        require(entriesId != -1) { "No entries supplied" }

        entries = context.resources.getStringArray(entriesId).toList()
        entryValues = if (entryValuesId != -1)
            context.resources.getIntArray(entryValuesId).toList()
        else
            entries.indices.toList()

        ta.recycle()
        setOnPreferenceClickListener {
            val pref = pref ?: return@setOnPreferenceClickListener false
            val theme = context.asTheme()
            val data =
                BottomPreferenceData(title.toString(), key, selectedIndex, entries, entryValues)
            val context = ContextThemeWrapper(context, theme.bottomSheetDialogThemeRes)
            val dialog = PreferenceBottomSheetDialog(context, view, pref, data)
            dialog.setOnDismissListener {
                selectedIndex = entryValues.indexOfFirst { getPersistedInt(selectedIndex) == it }
                setSelectedEntryText()
            }
            dialog.show()
            true
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Int {
        return a.getInt(index, -1)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val value = getPersistedInt(defaultValue as? Int ?: -1)
        selectedIndex = entryValues.indexOfFirst { value == it }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        view = holder.itemView as ViewGroup
        setSelectedEntryText()
    }

    private fun setSelectedEntryText() {
        if (selectedIndex != -1) {
            val textView = view.findViewById(R.id.textView) as TextView
            textView.text = entries[selectedIndex]
        }
    }
}