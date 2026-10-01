package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.Product
import com.example.data.RawMaterial
import java.text.DecimalFormat

object StockAlertNotificationManager {

    private const val CHANNEL_ID = "stock_alerts_channel"
    private const val CHANNEL_NAME = "การแจ้งเตือนสต็อกใกล้หมด"
    private const val CHANNEL_DESC = "แจ้งเตือนเมื่อสต็อกสินค้าหรือวัตถุดิบต่ำกว่าเกณฑ์ขั้นต่ำ"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendRawMaterialLowStockAlert(context: Context, material: RawMaterial) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val formatter = DecimalFormat("#,##0.##")
        val content = if (material.currentStock <= 0) {
            "วัตถุดิบ '${material.name}' สต็อกหมดแล้ว! กรุณาสั่งซื้อทันที"
        } else {
            "วัตถุดิบ '${material.name}' เหลือ ${formatter.format(material.currentStock)} ${material.unit} (ต่ำกว่าเกณฑ์ ${formatter.format(material.minStockThreshold)} ${material.unit}) กรุณาสั่งซื้อเพิ่ม"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            material.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("แจ้งเตือนสต็อกวัตถุดิบใกล้หมด!")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(material.id.toInt() + 1000, notification)
        } catch (_: SecurityException) {
        }
    }

    fun sendProductLowStockAlert(context: Context, product: Product) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val content = if (product.currentStock <= 0) {
            "สินค้าสำเร็จรูป '${product.name}' สต็อกหมดแล้ว!"
        } else {
            "สินค้าสำเร็จรูป '${product.name}' เหลือเพียง ${product.currentStock} ชิ้น (ต่ำกว่าเกณฑ์ ${product.minStockThreshold} ชิ้น)"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            product.id.toInt() + 5000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("แจ้งเตือนสต็อกสินค้าสำเร็จรูปใกล้หมด!")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(product.id.toInt() + 5000, notification)
        } catch (_: SecurityException) {
        }
    }
}
