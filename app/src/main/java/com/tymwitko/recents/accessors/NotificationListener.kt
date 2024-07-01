package com.tymwitko.recents.accessors

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() { // TODO
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        if (notification.flags and Notification.FLAG_ONGOING_EVENT == Notification.FLAG_ONGOING_EVENT) {
            // This notification is ongoing, so it could be a persistent notification.
            val packageName = sbn.packageName
            Log.i("NotificationListener", "Persistent notification from: $packageName")
        }
    }
}