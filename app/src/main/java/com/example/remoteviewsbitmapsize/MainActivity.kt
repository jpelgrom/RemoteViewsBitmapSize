package com.example.remoteviewsbitmapsize

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import com.example.remoteviewsbitmapsize.ui.theme.RemoteViewsBitmapSizeTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "RemoteViewsBitmap"
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        Log.i(TAG, "Permission was ${ if (it) "granted" else "not granted" }")
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteViewsBitmapSizeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(onClick = { requestPermission() }) {
                                Text(text = "Request permission")
                            }
                        }
                        Button(onClick = { sendNotification(6) }) {
                            Text(text = "Notification with 6 frames")
                        }
                        Button(onClick = { sendNotification(12) }) {
                            Text(text = "Notification with 12 frames")
                        }
                        Button(onClick = { sendNotification(18) }) {
                            Text(text = "Notification with 18 frames")
                        }
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun sendNotification(numberOfFrames: Int) {
        val builder = Notification.Builder(this, "channel")
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setContentTitle("Notification with $numberOfFrames frames")
        builder.setContentText("Subtitle")

        val channel = NotificationChannel("channel", "Channel", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val frames = getFrames(numberOfFrames)
        Log.i(TAG, "Got ${frames.size} frames, total size ${frames.sumOf { it.allocationByteCount } } bytes")
        // Expected output:
        // Got 6 frames, total size 2073600 bytes
        // Got 12 frames, total size 4147200 bytes
        // Got 18 frames, total size 6220800 bytes

        RemoteViews(packageName, R.layout.notification).let { layout ->
            frames.forEach { frame ->
                layout.addView(
                    R.id.frame_flipper,
                    RemoteViews(packageName, R.layout.notification_frame).apply {
                        setImageViewBitmap(
                            R.id.frame,
                            frame
                        )
                    }
                )
            }

            builder.setCustomBigContentView(layout)
            builder.setStyle(Notification.DecoratedCustomViewStyle())
        }

        notificationManager.notify(numberOfFrames, builder.build())
    }

    private fun getFrames(numberOfFrames: Int): List<Bitmap> {
        // Use some basic colors instead of images for this sample
        val colors = listOf(Color.RED, Color.BLACK, Color.BLUE, Color.GREEN, Color.YELLOW, Color.GRAY)

        val frames = mutableListOf<Bitmap>()
        for (i in 0 until numberOfFrames) {
            frames += Bitmap.createBitmap(
                IntArray(480 * 360) { colors[i % 6] },
                480,
                360,
                Bitmap.Config.RGB_565
            ) // the allocationByteCount of each frame will be 345600 bytes
        }
        return frames
    }
}