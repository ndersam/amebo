package com.amebo.amebo.common

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.amebo.amebo.R


/**
 * Context must be not be application context
 * SplashActivity and MainActivity have different themes
 */
class Theme(private val context: Context) {

    companion object {
        private const val DEFAULT = "Default"
        private const val DEFAULT_DARK = "Default.Dark"
        private const val NAIRALAND = "Nairaland"
        private const val NAIRALAND_DARK = "Nairaland.Dark"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)

    private val currentTheme: String
        get() {
            val style = when (val idx = prefs.getInt(Pref.CURRENT_THEME, 0)) {
                0 -> DEFAULT
                1 -> NAIRALAND
                else -> throw IllegalArgumentException("Unknown theme for index $idx")
            }
            return if (isDark) "$style.Dark" else style
        }

    val isDark: Boolean get() = prefs.getBoolean(Pref.DARK_MODE, false)

    val colorBackground: Int get() = resolveColor(R.attr.colorBackground)
    val colorSurface: Int get() = resolveColor(R.attr.colorSurface)
    val colorAccent: Int get() = resolveColor(R.attr.colorAccent)
    val colorPrimary: Int get() = resolveColor(R.attr.colorPrimary)
    val colorPrimaryDark: Int get() = resolveColor(R.attr.colorPrimaryDark)
    val colorOnPrimary: Int get() = resolveColor(R.attr.colorOnPrimary)
    val colorOnBackground: Int get() = resolveColor(R.attr.colorOnBackground)

    val textColorPrimary: Int get() = resolveColor(android.R.attr.textColorPrimary)

    val textColorSecondary: Int get() = resolveColor(android.R.attr.textColorSecondary)

    val colorControlHighlight: Int get() = resolveColor(android.R.attr.colorControlHighlight)


    /**
     * For disabled text, Opacity 38%
     */
    val textColorTertiary: Int get() = resolveColor(android.R.attr.textColorTertiary)

    @get:StyleRes
    val bottomSheetDialogThemeRes: Int
        get() = when (currentTheme) {
            DEFAULT -> R.style.BottomSheetDialogTheme_Default
            DEFAULT_DARK -> R.style.BottomSheetDialogTheme_Default_Dark
            NAIRALAND -> R.style.BottomSheetDialogTheme_Nairaland
            NAIRALAND_DARK -> R.style.BottomSheetDialogTheme_Nairaland_Dark
            else -> throw IllegalArgumentException("Unknown bottomSheetDialogThemeRes for theme $currentTheme")
        }

    @get:StyleRes
    val exploreBottomDialogThemeRes: Int
        get() = if (isDark) R.style.BottomDialogTheme_Dark else R.style.BottomDialogTheme_Default

    @get:ColorInt
    val bottomSheetTouchOutSideColor: Int
        get() = ContextCompat.getColor(context, R.color.black_opacity_60)

    @get:StyleRes
    val style: Int
        get() {
            return context.resources
                .getIdentifier("AppTheme.$currentTheme", "style", context.packageName)
        }

    fun apply() {
        try {
            context.setTheme(style)
        } catch (e: ClassCastException) {
            val style = when (prefs.getString(Pref.CURRENT_THEME, null)) {
                "Dark" -> DEFAULT_DARK
                else -> DEFAULT
            }
            context.resources
                .getIdentifier("AppTheme.$style", "style", context.packageName)
        }
    }

    private fun resolveColor(@AttrRes colorRes: Int): Int {
        val typedValue = TypedValue()
        val theme: Resources.Theme = context.theme
        theme.resolveAttribute(colorRes, typedValue, true)
        return typedValue.data
    }
}

fun Context.asTheme(): Theme = Theme(this)