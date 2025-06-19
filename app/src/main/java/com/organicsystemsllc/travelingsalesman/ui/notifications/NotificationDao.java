package com.organicsystemsllc.travelingsalesman.ui.notifications;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notification")
    List<Notification> getAll();

    @Insert
    void insert(Notification notification);



}


