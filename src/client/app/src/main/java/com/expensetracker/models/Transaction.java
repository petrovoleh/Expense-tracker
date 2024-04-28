package com.expensetracker.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String category;
    private double value;
    private String place;
    private Date time;

    public Transaction() {
    }

    @Ignore
    public Transaction(String category, double value, String place) {
        this.category = category;
        this.value = value;
        this.place = place;
        this.time = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        this.value = value;
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
