package com.amebo.amebo.screens.leftdrawer

import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import com.amebo.amebo.R
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

/**
 * Describes a [IDrawerItem] with a separate arrow click listener
 */
class CustomExpandableDrawerItem : ExpandableDrawerItem() {

    override val layoutRes: Int
        @LayoutRes
        get() = R.layout.custom_material_drawer_item_expandable

    var onArrowClickListener: (() -> Unit)? = null

    override var onDrawerItemClickListener: ((View?, IDrawerItem<*>, Int) -> Boolean)? =
        { view, drawerItem, position ->
            mOnDrawerItemClickListener?.invoke(view, drawerItem, position) ?: false
        }

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        holder.itemView.findViewById<View>(R.id.material_drawer_arrow_container)
            .setOnClickListener {
                if (this.isExpanded) {
                    ViewCompat.animate(holder.arrow)
                        .rotation(this@CustomExpandableDrawerItem.arrowRotationAngleEnd.toFloat())
                        .start()
                } else {
                    ViewCompat.animate(holder.arrow)
                        .rotation(this@CustomExpandableDrawerItem.arrowRotationAngleStart.toFloat())
                        .start()
                }
                onArrowClickListener?.invoke()
            }
        super.bindView(holder, payloads)
    }

}
