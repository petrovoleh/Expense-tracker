package com.expensetracker.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.DecimalFormat;
import java.util.Date;

import okhttp3.internal.concurrent.TaskRunner;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String backendId;
    private String category;
    private double value;
    private String place;
    private Date time;

    public Transaction() {
    }

    @Ignore
    public Transaction(String category, double value, String place) {
        this.category = category;
        this.place = place;
        this.time = new Date();

        // Round the value to two decimal places
        DecimalFormat df = new DecimalFormat("#.00");
        this.value = Double.parseDouble(df.format(value));
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String id) {
        this.backendId = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        DecimalFormat df = new DecimalFormat("#.00");
        this.value = Double.parseDouble(df.format(value));
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", value=" + value +
                ", place='" + place + '\'' +
                ", time=" + time +
                '}';
    }
}
