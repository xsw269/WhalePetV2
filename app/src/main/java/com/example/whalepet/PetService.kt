package com.example.whalepet

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import kotlin.math.abs

class PetService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var petView: FrameLayout
    private lateinit var webView: WebView
    private lateinit var params: WindowManager.LayoutParams
    private var initialX = 0; private var initialY = 0
    private var touchX = 0f; private var touchY = 0f; private var dragging = false

    override fun onCreate() {
        super.onCreate()
        val channelId = "pet"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(channelId, "桌宠", NotificationManager.IMPORTANCE_LOW))
        }
        val notification = if (Build.VERSION.SDK_INT >= 26)
            Notification.Builder(this, channelId).setContentTitle("运行中").setSmallIcon(android.R.drawable.ic_menu_compass).build()
        else Notification.Builder(this).setContentTitle("运行中").setSmallIcon(android.R.drawable.ic_menu_compass).build()
        startForeground(1, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createPetView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createPetView() {
        val size = (170 * resources.displayMetrics.density).toInt()
        petView = object : FrameLayout(this) { override fun onInterceptTouchEvent(ev: MotionEvent) = true }
        webView = WebView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            loadUrl("file:///android_asset/whale.html")
        }
        petView.addView(webView, FrameLayout.LayoutParams(-1, -1))
        params = WindowManager.LayoutParams(size, size,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START; x = 80; y = 320
        }
        petView.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y; touchX = e.rawX; touchY = e.rawY; dragging = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - touchX; val dy = e.rawY - touchY
                    if (abs(dx) > 12 || abs(dy) > 12) {
                        dragging = true; params.x = initialX + dx.toInt(); params.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(petView, params)
                    }; true
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragging) {
                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.deepseek.com")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
                    }; true
                }
                else -> false
            }
        }
        windowManager.addView(petView, params)
    }
    override fun onDestroy() { super.onDestroy(); if (::petView.isInitialized) { windowManager.removeView(petView); webView.destroy() } }
    override fun onBind(intent: Intent?): IBinder? = null
}
