package com.amebo.amebo.common.popup

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.amebo.amebo.R
import com.amebo.amebo.common.popup.PopupItem.Color as ColorItem
import com.amebo.amebo.common.popup.PopupItem.Font as FontItem

object Popup {

    private val colors = listOf(
        R.string.red to Color.RED,
        R.string.black to Color.BLACK,
        R.string.tomato to Color.parseColor("#FF6347"),  // Tomato
        R.string.forest_green to Color.parseColor("#228B22"),  // ForestGreen
        R.string.brown to Color.parseColor("#A52A2A"),  // Brown
        R.string.gold to Color.parseColor("#FFD700"),  // Gold
        R.string.deep_sky_blue to Color.parseColor("#00BFFF"),  // DeepSkyBlue
        R.string.orchid to Color.parseColor("#DA70D6")  // Orchid
    )

    private val fonts = listOf(
        FontItem(R.string.monospace, "monospace", Typeface.MONOSPACE),
        FontItem(R.string.sans_serif, "serif", Typeface.SANS_SERIF),
        FontItem(R.string.serif, "sans-serif", Typeface.SERIF)
    )


    fun colorPicker(activity: Activity, onItemClick: (PopupItem.Color) -> Unit) =
        newPopup(activity, items = colors.map { ColorItem(it.first, it.second) }) {
            onItemClick(it as ColorItem)
        }

    fun fontPicker(activity: Activity, onItemClick: (FontItem) -> Unit) =
        newPopup(activity, fonts) { onItemClick(it as FontItem) }


    private fun newPopup(
        activity: Activity,
        items: List<PopupItem>,
        onItemClick: (PopupItem) -> Unit
    ) = PopupWindow(activity).apply {
        isOutsideTouchable = true
        isFocusable = true
        height = WindowManager.LayoutParams.WRAP_CONTENT
        width = WindowManager.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20F
        contentView = activity.layoutInflater.inflate(R.layout.popup_simple, null)

        val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = ItemAdapter(items) {
            onItemClick(it)
            dismiss()
        }
    }
}