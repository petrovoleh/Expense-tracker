package com.expensetracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String category;
    private double value;
    private String place;
    public Transaction() {
    }
    public Transaction(String category, double value, String place) {
        this.category = category;
        this.value = value;
        this.place = place;
    }

    public String getCategory() {
        return category;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Transaction{" +
                "category='" + category + '\'' +
                ", value=" + value +
                ", place='" + place + '\'' +
                '}';
    }
}
