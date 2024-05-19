package org.expencetracker.webserver.component.models;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "budgets")
public class Budget {
    @Id
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private int budget;

    @NotBlank
    private String userId;

    public Budget() {}

    public Budget(String name, int budget) {
        this.name = name;
        this.budget = budget;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
