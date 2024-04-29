package com.expensetracker.data;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.expensetracker.MainActivity;
import com.expensetracker.dao.TransactionDao;
import com.expensetracker.models.Categories;
import com.expensetracker.models.Category;

public class Analytics {
    private String text;
    private final TransactionDao transactionDao;
    private final String[] names = Categories.getCategoriesNames();
    public Analytics() {
        text = new String();
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
    }

    public String getAllAnalytics(){
        StringBuilder builder = new StringBuilder();
        for (String name:names){

            getAnalytics(name, result -> builder.append(result).append("\n"));

        }
        String text = builder.toString();
        return text;
    }

    public void getAnalytics(String categoryName, AnalyticsCallback callback) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        int all = category.getBudget();

        // Observe the LiveData for total value
        transactionDao.getTotalValueForCategory(categoryName).observeForever(spent -> {
            if (spent == null) {
                spent = 0.0;
            }
            double available = all - spent;
            String result = categoryName + " " + all + " " + spent + " " + available;
            callback.onAnalyticsResult(result);
        });
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public interface AnalyticsCallback {
        void onAnalyticsResult(String result);
    }
    public String[] getNames(){
        return names;
    }

    public int size(){
        return names.length;
    }

}
