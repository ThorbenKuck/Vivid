package com.vivid.backend.backplane

import com.vivid.backend.backplane.dto.Notification

interface NotificationBackplane {

    fun listen(callback: (Notification) -> Unit)

    fun sendNotification(notification: Notification)

}
