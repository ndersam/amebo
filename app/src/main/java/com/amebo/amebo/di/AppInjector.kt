package com.amebo.amebo.di

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.amebo.amebo.application.App
import com.amebo.amebo.common.GlideApp
import com.amebo.amebo.common.PrefImpl
import com.amebo.core.Nairaland
import com.amebo.core.di.DaggerCoreComponent
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader

object AppInjector {
    fun inject(app: App) {
        Nairaland.init(app)

        val pref = PrefImpl(app)

        val coreComponent = DaggerCoreComponent.builder()
            .observable(app.sessionObservable)
            .userSupplier(pref::user)
            .context(app)
            .build()

        DaggerAppComponent.builder()
            .context(app)
            .app(app)
            .pref(pref)
            .coreComponent(coreComponent)
            .build()
            .inject(app)


        DrawerImageLoader.init(
            object : AbstractDrawerImageLoader() {
                override fun set(
                    imageView: ImageView,
                    uri: Uri,
                    placeholder: Drawable,
                    tag: String?
                ) {
                    GlideApp.with(imageView.context)
                        .load(uri)
                        .placeholder(placeholder)
                        .into(imageView)
                }

                override fun cancel(imageView: ImageView) {
                    GlideApp.with(imageView.context).clear(imageView)
                }
            })
    }
}





