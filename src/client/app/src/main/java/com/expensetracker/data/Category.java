package com.expensetracker.data;

public class Category {
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
}
