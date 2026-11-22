package com.noctis.themeatelier

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    data class Theme(val name: String, val description: String, val top: Int, val bottom: Int, val accent: Int)
    private val themes = listOf(
        Theme("Nocturne", "Midnight violet", 0xFF12101D.toInt(), 0xFF48335D.toInt(), 0xFFC4A7FF.toInt()),
        Theme("Moonlit", "Silver night", 0xFF091A2C.toInt(), 0xFF3A5879.toInt(), 0xFFB6D8FF.toInt()),
        Theme("Sandstone", "Warm earth", 0xFF38291F.toInt(), 0xFFB78B61.toInt(), 0xFFF4D4A9.toInt()),
        Theme("Evergreen", "Forest calm", 0xFF071E19.toInt(), 0xFF30634C.toInt(), 0xFFB8E2C4.toInt()),
        Theme("Rose Noir", "Muted rose", 0xFF24131E.toInt(), 0xFF8A4E67.toInt(), 0xFFF5B6D0.toInt()),
        Theme("Glacier", "Ice blue", 0xFF0C2231.toInt(), 0xFF5A9BB5.toInt(), 0xFFC2F2FF.toInt()),
        Theme("Citrus Dusk", "Golden hour", 0xFF342014.toInt(), 0xFFAA6930.toInt(), 0xFFFFD78A.toInt())
    )
    private var selected = 0
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected = getSharedPreferences("noctis", MODE_PRIVATE).getInt("theme", 0).coerceIn(themes.indices)
        render()
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun render() {
        val theme = themes[selected]
        window.statusBarColor = theme.top
        window.navigationBarColor = theme.top
        val scroll = ScrollView(this)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(28))
            setBackgroundColor(theme.top)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(label("NOCTIS", 12f, theme.accent, true).apply { letterSpacing = .18f })
        root.addView(label("Your home.\nYour atmosphere.", 28f, Color.WHITE, true).apply { setPadding(0, dp(6), 0, dp(6)) })
        root.addView(label("Choose a theme, then apply wallpaper and launcher styling.", 14f, 0xFFBDBCC9.toInt()).apply { setPadding(0, 0, 0, dp(18)) })

        val themeTitle = label("THEME COLLECTION", 12f, theme.accent, true).apply { setPadding(0, dp(4), 0, dp(10)) }
        root.addView(themeTitle)
        themes.forEachIndexed { index, item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                background = rounded(if (index == selected) item.accent else 0xFF292833.toInt(), dp(14))
            }
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val swatch = View(this).apply { background = rounded(item.bottom, dp(12)) }
            row.addView(swatch, LinearLayout.LayoutParams(dp(54), dp(54)))
            val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }
            details.addView(label(item.name, 17f, if (index == selected) theme.top else Color.WHITE, true))
            details.addView(label(item.description, 12f, if (index == selected) theme.top else 0xFFBDBCC9.toInt()).apply { setPadding(0, dp(3), 0, 0) })
            row.addView(details, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(label(if (index == selected) "SELECTED" else "SELECT", 10f, if (index == selected) theme.top else item.accent, true))
            card.addView(row)
            card.setOnClickListener { selected = index; getSharedPreferences("noctis", MODE_PRIVATE).edit().putInt("theme", selected).apply(); render() }
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(8)
            root.addView(card, params)
        }

        val apply = TextView(this).apply {
            text = "APPLY WALLPAPER + LAUNCHER THEME"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(theme.top)
            background = rounded(theme.accent, dp(14))
            setPadding(dp(14), dp(17), dp(14), dp(17))
            setOnClickListener { applyTheme(theme) }
        }
        root.addView(apply, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12); bottomMargin = dp(22) })

        root.addView(label("HOME SCREEN APPS", 12f, theme.accent, true).apply { setPadding(0, 0, 0, dp(12)) })
        val grid = GridLayout(this).apply { columnCount = 4; alignmentMode = GridLayout.ALIGN_MARGINS }
        val apps = getLaunchableApps()
        apps.forEachIndexed { index, info ->
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(6), dp(10), dp(6), dp(10))
            }
            val iconBg = ImageView(this).apply {
                setImageDrawable(info.loadIcon(packageManager))
                background = rounded(0xFF302D3A.toInt(), dp(18))
                setPadding(dp(10), dp(10), dp(10), dp(10))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            tile.addView(iconBg, LinearLayout.LayoutParams(dp(56), dp(56)))
            tile.addView(label(info.loadLabel(packageManager).toString(), 11f, Color.WHITE).apply {
                gravity = Gravity.CENTER; maxLines = 2; setPadding(0, dp(5), 0, 0)
            }, LinearLayout.LayoutParams(-1, -2))
            tile.setOnClickListener {
                try { startActivity(packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)) }
                catch (_: Exception) { Toast.makeText(this, "App couldn't open", Toast.LENGTH_SHORT).show() }
            }
            val lp = GridLayout.LayoutParams(GridLayout.spec(index / 4), GridLayout.spec(index % 4, 1f))
            lp.width = 0
            lp.setMargins(dp(2), dp(2), dp(2), dp(2))
            grid.addView(tile, lp)
        }
        root.addView(grid)
        root.addView(label("Set Noctis as your default Home app to use this launcher. Icons and system UI outside this launcher may remain unchanged depending on your phone.", 12f, 0xFFBDBCC9.toInt()).apply { setPadding(0, dp(18), 0, 0 })
    }

    private fun getLaunchableApps(): List<android.content.pm.ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
    }

    private fun applyTheme(theme: Theme) {
        try {
            val manager = WallpaperManager.getInstance(this)
            manager.setBitmap(makeWallpaper(theme), null, true, WallpaperManager.FLAG_SYSTEM)
            getSharedPreferences("noctis", MODE_PRIVATE).edit().putInt("theme", selected).apply()
            Toast.makeText(this, "Wallpaper set. Noctis launcher theme is active in this Home screen.", Toast.LENGTH_LONG).show()
            render()
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't apply wallpaper: " + (e.message ?: "device restriction"), Toast.LENGTH_LONG).show()
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color)
        if (bold) setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }

    private fun makeWallpaper(theme: Theme): Bitmap {
        val width = 1080
        val height = 2160
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), theme.top, theme.bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = theme.accent
        paint.alpha = 35
        canvas.drawCircle(width * .78f, height * .24f, width * .26f, paint)
        return bitmap
    }
}
