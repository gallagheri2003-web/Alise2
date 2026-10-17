package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class JarvisFloatingOverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "jarvis_hud_core_channel"
        const val NOTIFICATION_ID = 1001
        var isRunning = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        setupFloatingOrb()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_hud_service_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_hud_service_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ALISE Core Active")
            .setContentText("Active assistant service • Galaxy S25")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupFloatingOrb() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val orbSizePx = (68 * density).toInt()

        val params = WindowManager.LayoutParams(
            orbSizePx,
            orbSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        val orb = AliseHollowGlassOrbView(this)

        orb.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(overlayView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            // Launch Main HUD
                            val launchIntent = Intent(this@JarvisFloatingOverlayService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("EXTRA_VOICE_TRIGGER", true)
                            }
                            startActivity(launchIntent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        overlayView = orb
        try {
            windowManager?.addView(overlayView, params)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

/**
 * Custom Hardware-Accelerated 120Hz Floating View for the A.L.I.S.E. Blue-Glass Hollow 3D Sphere.
 * Ensures compact overlay mode is visually coherent with the primary expanded sphere.
 */
class AliseHollowGlassOrbView(context: Context) : View(context) {

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val centerX = w / 2f
        val centerY = h / 2f
        val baseRadius = (w.coerceAtMost(h) / 2f) * 0.85f

        val timeMs = android.os.SystemClock.uptimeMillis()

        // 360° Continuous Seamless Loop (12 second period)
        val rotYAngle = ((timeMs % 12000L) / 12000.0) * 2.0 * Math.PI
        val tiltXAngle = Math.sin((timeMs % 8000L) / 8000.0 * 2.0 * Math.PI) * Math.toRadians(12.0)
        val breathing = 1.0 + Math.sin((timeMs % 2600L) / 2600.0 * 2.0 * Math.PI) * 0.04
        val currentRadius = (baseRadius * breathing).toFloat()

        // 1. Ambient Glass Fresnel Glow
        val glowShader = android.graphics.RadialGradient(
            centerX, centerY, currentRadius * 1.25f,
            intArrayOf(
                android.graphics.Color.argb(85, 0, 229, 255),
                android.graphics.Color.argb(20, 0, 140, 255),
                android.graphics.Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.6f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = glowShader
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, currentRadius * 1.25f, paint)
        paint.shader = null

        // 2. Liquid Glass Silhouette Ring
        strokePaint.color = android.graphics.Color.argb(190, 186, 230, 253)
        strokePaint.strokeWidth = 2.5f
        canvas.drawCircle(centerX, centerY, currentRadius, strokePaint)

        // 3. Symmetrical 3D Hollow Wireframe Sphere
        val latBands = 8
        val lonBands = 14

        val cosX = Math.cos(tiltXAngle).toFloat()
        val sinX = Math.sin(tiltXAngle).toFloat()
        val cosY = Math.cos(rotYAngle).toFloat()
        val sinY = Math.sin(rotYAngle).toFloat()

        for (i in 0..latBands) {
            val lat = Math.PI * (i.toDouble() / latBands - 0.5)
            val sinLat = Math.sin(lat).toFloat()
            val cosLat = Math.cos(lat).toFloat()

            for (j in 0..lonBands) {
                val lon = 2.0 * Math.PI * (j.toDouble() / lonBands)
                val sinLon = Math.sin(lon).toFloat()
                val cosLon = Math.cos(lon).toFloat()

                // Unit Sphere Points
                val x0 = cosLat * cosLon
                val y0 = sinLat
                val z0 = cosLat * sinLon

                // Y-axis 360° rotation
                val x1 = x0 * cosY + z0 * sinY
                val y1 = y0
                val z1 = -x0 * sinY + z0 * cosY

                // X-axis tilt
                val x2 = x1
                val y2 = y1 * cosX - z1 * sinX
                val z2 = y1 * sinX + z1 * cosX // Depth Z: -1 to +1

                val px = centerX + x2 * currentRadius
                val py = centerY + y2 * currentRadius

                val depthAlpha = ((z2 + 1f) / 2f).coerceIn(0.15f, 1f)
                val dotRadius = 1.6f + (z2 + 1f) * 1.2f

                if (z2 > 0.4f) {
                    paint.color = android.graphics.Color.argb((240 * depthAlpha).toInt(), 255, 255, 255)
                } else {
                    paint.color = android.graphics.Color.argb((160 * depthAlpha).toInt(), 0, 229, 255)
                }
                canvas.drawCircle(px, py, dotRadius, paint)
            }
        }

        // 4. Central Luminous Nucleus
        paint.color = android.graphics.Color.argb(220, 255, 255, 255)
        canvas.drawCircle(centerX, centerY, currentRadius * 0.12f, paint)

        // Continuous high-refresh animation frame loop (120Hz compatible on Samsung Galaxy)
        postInvalidateOnAnimation()
    }
}
