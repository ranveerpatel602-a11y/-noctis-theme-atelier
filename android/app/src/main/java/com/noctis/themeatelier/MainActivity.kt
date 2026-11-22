package com.noctis.themeatelier

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    data class Theme(val name: String, val subtitle: String, val top: Int, val bottom: Int, val accent: Int)
    private val themes = listOf(
        Theme("Nocturne", "Midnight violet · calm dark", 0xFF12101D.toInt(), 0xFF48335D.toInt(), 0xFFC4A7FF.toInt()),
        Theme("Moonlit", "Silver night · cool glow", 0xFF091A2C.toInt(), 0xFF3A5879.toInt(), 0xFFB6D8FF.toInt()),
        Theme("Sandstone", "Warm neutral · soft earth", 0xFF38291F.toInt(), 0xFFB78B61.toInt(), 0xFFF4D4A9.toInt()),
        Theme("Evergreen", "Forest green · deep calm", 0xFF071E19.toInt(), 0xFF30634C.toInt(), 0xFFB8E2C4.toInt()),
        Theme("Rose Noir", "Muted rose · dark elegance", 0xFF24131E.toInt(), 0xFF8A4E67.toInt(), 0xFFF5B6D0.toInt()),
        Theme("Glacier", "Ice blue · clean minimal", 0xFF0C2231.toInt(), 0xFF5A9BB5.toInt(), 0xFFC2F2FF.toInt()),
        Theme("Citrus Dusk", "Golden hour · fresh amber", 0xFF342014.toInt(), 0xFFAA6930.toInt(), 0xFFFFD78A.toInt())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 24)
            setBackgroundColor(Color.rgb(14, 14, 20))
        }
        val title = TextView(this).apply {
            text = "NOCTIS\nTHEME ATELIER"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 8)
        }
        list.addView(title)
        val intro = TextView(this).apply {
            text = "Native Android app · Preview and set wallpapers"
            textSize = 14f
            setTextColor(0xFFB8B8C8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 22)
        }
        list.addView(intro)
        themes.forEach { theme ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 22, 24, 22)
                setBackgroundColor(0xFF24242E.toInt())
            }
            val swatch = TextView(this).apply {
                text = "●  ●  ●"
                textSize = 28f
                setTextColor(theme.accent)
                setPadding(0, 0, 0, 4)
            }
            val name = TextView(this).apply {
                text = theme.name
                textSize = 20f
                setTextColor(Color.WHITE)
            }
            val sub = TextView(this).apply {
                text = theme.subtitle
                textSize = 13f
                setTextColor(0xFFBBBBCA.toInt())
                setPadding(0, 4, 0, 14)
            }
            val preview = TextView(this).apply {
                text = "Wallpaper preview"
                textSize = 14f
                setTextColor(theme.accent)
                setPadding(0, 8, 0, 8)
            }
            val apply = TextView(this).apply {
                text = "APPLY WALLPAPER"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(0xFF12121A.toInt())
                setBackgroundColor(theme.accent)
                setPadding(20, 16, 20, 16)
                setOnClickListener { applyWallpaper(theme) }
            }
            card.addView(swatch)
            card.addView(name)
            card.addView(sub)
            card.addView(preview)
            card.addView(apply)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 20)
            list.addView(card, params)
        }
        scroll.addView(list)
        setContentView(scroll)
    }

    private fun applyWallpaper(theme: Theme) {
        try {
            val manager = WallpaperManager.getInstance(this)
            manager.setBitmap(makeWallpaper(theme), null, true, WallpaperManager.FLAG_SYSTEM)
            Toast.makeText(this, "Wallpaper applied to Home screen", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't set wallpaper: " + (e.message ?: "device issue"), Toast.LENGTH_LONG).show()
        }
    }

    private fun makeWallpaper(theme: Theme): Bitmap {
        val w = 1080
        val h = 2160
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), theme.top, theme.bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null
        paint.color = theme.accent
        paint.alpha = 45
        canvas.drawCircle(w * 0.76f, h * 0.26f, w * 0.28f, paint)
        paint.alpha = 255
        paint.textSize = 72f
        paint.typeface = android.graphics.Typeface.create("sans-serif-light", 0)
        canvas.drawText(theme.name.uppercase(), 72f, h * 0.82f, paint)
        paint.textSize = 32f
        paint.alpha = 180
        canvas.drawText("NOCTIS THEME ATELIER", 72f, h * 0.85f, paint)
        return bitmap
    }
}
