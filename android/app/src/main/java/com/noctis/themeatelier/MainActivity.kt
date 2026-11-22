package com.noctis.themeatelier

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    data class ThemePack(val title:String, val subtitle:String, val base:Int, val light:Int, val accent:Int)
    private val packs = listOf(
        ThemePack("Midnight", "Deep violet • Liquid glass", 0xFF10101A.toInt(), 0xFF42345F.toInt(), 0xFFC6B5FF.toInt()),
        ThemePack("Ocean", "Blue horizon", 0xFF071B2B.toInt(), 0xFF286180.toInt(), 0xFFB6E6FF.toInt()),
        ThemePack("Sand", "Warm minimal", 0xFF33261C.toInt(), 0xFF9D754D.toInt(), 0xFFFFD9A8.toInt()),
        ThemePack("Forest", "Natural green", 0xFF081C17.toInt(), 0xFF285B45.toInt(), 0xFFB8E7C9.toInt()),
        ThemePack("Rose", "Soft rose dusk", 0xFF24131D.toInt(), 0xFF834A65.toInt(), 0xFFFFBCD7.toInt()),
        ThemePack("Ice", "Cool glacier", 0xFF092131.toInt(), 0xFF438BA7.toInt(), 0xFFC5F2FF.toInt()),
        ThemePack("Sunset", "Amber glow", 0xFF302015.toInt(), 0xFF9E5D2B.toInt(), 0xFFFFD28B.toInt())
    )
    private var packIndex = 0
    private lateinit var container: LinearLayout
    private var searchText = ""
    private var selectedWallpaperUri: Uri? = null
    private val prefs by lazy { getSharedPreferences("noctis-launcher", MODE_PRIVATE) }
    private val appList by lazy { queryApps() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packIndex = prefs.getInt("pack",0).coerceIn(packs.indices)
        selectedWallpaperUri = prefs.getString("wallpaper_uri", null)?.let { Uri.parse(it) }
        render()
    }

    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun pack()=packs[packIndex]
    private fun bg(color:Int, radius:Int, stroke:Int=0): GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius=dp(radius).toFloat()
        if(stroke!=0) setStroke(dp(1),stroke)
    }
    private fun text(value:String, size:Float, color:Int, bold:Boolean=false)=TextView(this).apply {
        text=value; textSize=size; setTextColor(color)
        if(bold) setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD)
    }

    private fun render() {
        val p=pack()
        window.statusBarColor=p.base
        window.navigationBarColor=p.base
        val scroller=ScrollView(this)
        container=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(20),dp(18),dp(20),dp(28))
            setBackgroundColor(p.base)
        }
        scroller.addView(container); setContentView(scroller)

        val top=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        top.addView(text("NOCTIS",12f,p.accent,true),LinearLayout.LayoutParams(0,-2,1f))
        top.addView(text("⋯",26f,Color.WHITE).apply { gravity=Gravity.CENTER; background=bg(0x22FFFFFF,24); setPadding(dp(14),dp(3),dp(14),dp(3)) })
        container.addView(top)
        val creatorButton=text("CREATOR STUDIO  •  SELL THEMES",12f,Color.WHITE,true).apply {
            gravity=Gravity.CENTER; background=bg(p.light,16); setPadding(dp(12),dp(13),dp(12),dp(13))
            setOnClickListener { openCreatorStudio() }
        }
        container.addView(creatorButton,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(14)})
        container.addView(text("Good day.",28f,Color.WHITE,true).apply { setPadding(0,dp(20),0,0) })
        container.addView(text("Make this home yours.",14f,0xFFB9BBC8.toInt()).apply { setPadding(0,dp(4),0,dp(16) })

        val search=EditText(this).apply {
            hint="Search apps"
            setHintTextColor(0xFFB5B5C2.toInt()); setTextColor(Color.WHITE)
            textSize=14f; singleLine=true
            background=bg(0x22FFFFFF.toInt(),24)
            setPadding(dp(16),dp(10),dp(16),dp(10))
        }
        container.addView(search,LinearLayout.LayoutParams(-1,dp(48)))
        container.addView(text("APP LIBRARY",11f,p.accent,true).apply { setPadding(0,dp(22),0,dp(10)) })
        val grid=GridLayout(this).apply { columnCount=4 }
        container.addView(grid)
        fun populate(filter:String) {
            grid.removeAllViews()
            val filtered=appList.filter { it.loadLabel(packageManager).toString().contains(filter,true) }
            filtered.forEachIndexed { index,ri ->
                val tile=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(dp(4),dp(8),dp(4),dp(8)) }
                val icon=ImageView(this).apply { setImageDrawable(ri.loadIcon(packageManager)); scaleType=ImageView.ScaleType.FIT_CENTER; background=bg(0x25FFFFFF,17,p.accent); setPadding(dp(8),dp(8),dp(8),dp(8)) }
                tile.addView(icon,LinearLayout.LayoutParams(dp(56),dp(56)))
                tile.addView(text(ri.loadLabel(packageManager).toString(),11f,Color.WHITE).apply { gravity=Gravity.CENTER; maxLines=2; setPadding(0,dp(5),0,0 },LinearLayout.LayoutParams(-1,-2))
                tile.setOnClickListener {
                    try { startActivity(packageManager.getLaunchIntentForPackage(ri.activityInfo.packageName)) }
                    catch (_:Exception) { Toast.makeText(this,"Unable to open app",Toast.LENGTH_SHORT).show() }
                }
                val lp=GridLayout.LayoutParams(GridLayout.spec(index/4),GridLayout.spec(index%4,1f))
                lp.width=0; lp.setMargins(dp(3),dp(2),dp(3),dp(2)); grid.addView(tile,lp)
            }
            if(filtered.isEmpty()) grid.addView(text("No apps found",13f,0xFFB8B8C5.toInt()).apply { setPadding(0,dp(12),0,dp(12)) })
        }
        populate("")
        search.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
            override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){populate(s?.toString().orEmpty())}
            override fun afterTextChanged(s:Editable?){}
        })

        container.addView(text("WALLPAPER & COLORS",11f,p.accent,true).apply { setPadding(0,dp(22),0,dp(10)) })
        val themeRow=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        packs.forEachIndexed { i,t ->
            val chip=text(t.title,11f,if(i==packIndex)p.base else Color.WHITE,true).apply {
                gravity=Gravity.CENTER; background=bg(if(i==packIndex)p.accent else 0x24FFFFFF,20); setPadding(dp(12),dp(9),dp(12),dp(9))
                setOnClickListener { packIndex=i; prefs.edit().putInt("pack",i).apply(); render() }
            }
            themeRow.addView(chip,LinearLayout.LayoutParams(-2,-2).apply { rightMargin=dp(6) })
        }
        val themeScroll=ScrollView(this).apply { isFillViewport=false }
        val horizontal=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        packs.forEachIndexed { i,t ->
            val preview=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(12),dp(10),dp(12),dp(10)); background=bg(t.light,18); setOnClickListener { packIndex=i; prefs.edit().putInt("pack",i).apply(); render() } }
            preview.addView(text(t.title,14f,Color.WHITE,true))
            preview.addView(text(t.subtitle,10f,0xFFD6D5DE.toInt()).apply { setPadding(0,dp(4),0,0) })
            horizontal.addView(preview,LinearLayout.LayoutParams(dp(150),dp(80)).apply { rightMargin=dp(8) })
        }
        themeScroll.addView(horizontal)
        container.addView(themeScroll,LinearLayout.LayoutParams(-1,dp(88)))
        val applyBtn=text("APPLY WALLPAPER",13f,p.base,true).apply {
            gravity=Gravity.CENTER; background=bg(p.accent,16); setPadding(dp(12),dp(16),dp(12),dp(16))
            setOnClickListener { setWallpaper(p) }
        }
        container.addView(applyBtn,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(14) })
        val photoBtn=text("CHOOSE PHOTO WALLPAPER",13f,Color.WHITE,true).apply {
            gravity=Gravity.CENTER; background=bg(0x22FFFFFF,16); setPadding(dp(12),dp(15),dp(12),dp(15))
            setOnClickListener { openWallpaperPicker() }
        }
        container.addView(photoBtn,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(8) })
        selectedWallpaperUri?.let { uri ->
            val preview=ImageView(this).apply { setImageURI(uri); scaleType=ImageView.ScaleType.CENTER_CROP }
            container.addView(preview,LinearLayout.LayoutParams(-1,dp(220)).apply { topMargin=dp(10) })
        }
        val saveThemeBtn=text("SAVE CURRENT THEME",13f,Color.WHITE,true).apply {
            gravity=Gravity.CENTER; background=bg(p.light,16); setPadding(dp(12),dp(15),dp(12),dp(15))
            setOnClickListener {
                prefs.edit().putInt("saved_theme_pack",packIndex)
                    .putString("saved_wallpaper_uri",selectedWallpaperUri?.toString() ?: "").apply()
                Toast.makeText(this,"Theme saved on this phone.",Toast.LENGTH_SHORT).show()
            }
        }
        container.addView(saveThemeBtn,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(8) })
        val loadThemeBtn=text("LOAD SAVED THEME",13f,Color.WHITE,true).apply {
            gravity=Gravity.CENTER; background=bg(0x22FFFFFF,16); setPadding(dp(12),dp(15),dp(12),dp(15))
            setOnClickListener {
                val savedPack=prefs.getInt("saved_theme_pack",-1)
                if(savedPack in packs.indices) {
                    packIndex=savedPack
                    val savedUri=prefs.getString("saved_wallpaper_uri","")
                    selectedWallpaperUri=savedUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                    prefs.edit().putInt("pack",packIndex).putString("wallpaper_uri",selectedWallpaperUri?.toString() ?: "").apply()
                    render()
                } else Toast.makeText(this,"No saved theme yet.",Toast.LENGTH_SHORT).show()
            }
        }
        container.addView(loadThemeBtn,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(8) })
        container.addView(text("Choose Noctis in Android’s Home app settings to use this launcher as your home screen. Apps outside this launcher keep their original icons.",11f,0xFFB4B4C2.toInt()).apply { setPadding(0,dp(16),0,0 }))
    }

    private fun openWallpaperPicker() {
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type="image/*"
        }
        try { startActivityForResult(intent, 4102) }
        catch (_:Exception) { Toast.makeText(this,"Photo picker unavailable.",Toast.LENGTH_SHORT).show() }
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==4102 && resultCode==RESULT_OK) {
            val uri=data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_:Exception) {}
            selectedWallpaperUri=uri
            prefs.edit().putString("wallpaper_uri",uri.toString()).apply()
            render()
        }
    }

    private fun openCreatorStudio() {
        val nameField=EditText(this).apply { hint="Theme pack name"; setText(prefs.getString("creator_name","")) }
        val priceField=EditText(this).apply { hint="Optional suggested price in INR"; inputType=2; setText(prefs.getString("creator_price","")) }
        val form=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(8),dp(20),dp(4)); addView(nameField); addView(priceField) }
        AlertDialog.Builder(this).setTitle("Noctis Creator Studio").setMessage("Create a theme listing draft").setView(form)
            .setPositiveButton("Save listing draft") { _,_ ->
                prefs.edit().putString("creator_name",nameField.text.toString().ifBlank{"Untitled theme"})
                    .putString("creator_price",priceField.text.toString()).putString("listing_draft","saved").apply()
                Toast.makeText(this,"Draft saved locally on this phone.",Toast.LENGTH_SHORT).show()
            }.setNeutralButton("View my draft") { _,_ ->
                val saved=prefs.getString("listing_draft","")
                val title=prefs.getString("creator_name","Untitled theme") ?: "Untitled theme"
                val price=prefs.getString("creator_price","") ?: ""
                AlertDialog.Builder(this).setTitle("My theme listing")
                    .setMessage(if(saved=="saved") "$title\\nSuggested price: ${if(price.isBlank()) "Not set" else "₹$price"}\\nStatus: Local draft, not published" else "No listing draft saved yet.")
                    .setPositiveButton("OK",null).show()
            }.setNegativeButton("Cancel",null).show()
    }

    private fun queryApps():List<ResolveInfo> {
        val i=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(i,0).filter { it.activityInfo.packageName!=packageName }.sortedBy { it.loadLabel(packageManager).toString().lowercase() }
    }
    private fun setWallpaper(t:ThemePack) {
        try {
            val uri=selectedWallpaperUri
            val bitmap=if(uri!=null) contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } else null
            WallpaperManager.getInstance(this).setBitmap(bitmap ?: createBitmap(t),null,true,WallpaperManager.FLAG_SYSTEM)
            Toast.makeText(this,"Wallpaper applied.",Toast.LENGTH_LONG).show()
        } catch(e:Exception) { Toast.makeText(this,"Wallpaper apply failed: "+(e.message ?: ""),Toast.LENGTH_LONG).show() }
    }
    private fun createBitmap(t:ThemePack):Bitmap {
        val w=1080; val h=2160
        val b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); val c=Canvas(b); val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader=LinearGradient(0f,0f,w.toFloat(),h.toFloat(),t.base,t.light,Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w.toFloat(),h.toFloat(),paint); paint.shader=null
        paint.color=t.accent; paint.alpha=35; c.drawCircle(w*.78f,h*.22f,w*.27f,paint)
        return b
    }
}