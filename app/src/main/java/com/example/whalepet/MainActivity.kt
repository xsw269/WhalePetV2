package com.example.whalepet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(0xFF061224.toInt())
        
        val title = TextView(this)
        title.text = "桌面宠物"
        title.textSize = 24f
        title.setTextColor(0xFF7DD3FC.toInt())
        title.gravity = Gravity.CENTER
        layout.addView(title)

        val btnStart = Button(this)
        btnStart.text = "开启桌宠"
        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请允许悬浮窗", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                val intent = Intent(this, PetService::class.java)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
                moveTaskToBack(true)
            }
        }
        layout.addView(btnStart)
        setContentView(layout)
    }
}
