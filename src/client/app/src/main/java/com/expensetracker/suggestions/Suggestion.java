package com.expensetracker.suggestions;

import com.expensetracker.data.Analytics;
import com.expensetracker.data.Categories;

import java.util.ArrayList;
import java.util.List;

public class Suggestion {
    Analytics analytics = new Analytics();
    String[] categories;
    List<String> suggestions = new ArrayList<>();

    public Suggestion(){
        categories = analytics.getNames();
    }

    public List<String> getSuggestions(){

        for(String category: categories){
            int available = analytics.getAvailable(category);
            String suggestion = usedMoreThenAllocated(category, analytics.getAllocated(category), available);
            if (suggestion!= null)
                suggestions.add(suggestion);
        }
        if(suggestions.isEmpty()){
            suggestions.add("There is no suggestions for today");
        }
        return suggestions;
    }

    private String usedMoreThenAllocated(String category, int allocated, int available) {
        if (available == allocated) {
            return null; // No message if available funds match the allocated budget
        }
        if (available > allocated / 2) {
            return "You haven't fully utilized your budget for category: " + category +
                    ". Consider reallocating funds or investing more in this area.";
        }
        if (available < 0) {
            return "Oops! It seems you've exceeded your budget for category: " + category +
                    ". Review your expenses and adjust your budget accordingly.";
        }
        if (available < allocated / 10) {
            return "You're close to using up all your allocated funds for category: " + category +
                    ". Plan your spending wisely and prioritize essential expenses.";
        }
        // No message if none of the above conditions are met
        return null;
    }

}


