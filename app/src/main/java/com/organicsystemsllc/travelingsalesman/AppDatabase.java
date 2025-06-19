package com.organicsystemsllc.travelingsalesman;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.organicsystemsllc.travelingsalesman.ui.notifications.Notification;
import com.organicsystemsllc.travelingsalesman.ui.notifications.NotificationDao;

@Database(entities = {Notification.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NotificationDao notificationDao();
}
