package com.expensetracker.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey
    @NonNull
    private String name;
    private int budget;
    public String getName(){
        return name;
    }
    public int getBudget(){
        return budget;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setBudget(int budget){
        this.budget = budget;
    }
    @Override
    public String toString() {
        return "Transaction{" +
                "name=" + name +
                ", budget='" + budget + '\'' +
                '}';
    }
}
