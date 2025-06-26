package com.organicsystemsllc.travelingsalesman;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;


@Entity
public class NodeEntity {
    @PrimaryKey
    public int id;
    public double latitude;
    public double longitude;
    public Timestamp created;
}
