package com.expensetracker.data;

import androidx.lifecycle.Observer;

import com.expensetracker.MainActivity;
import com.expensetracker.dao.TransactionDao;
import com.expensetracker.models.Categories;
import com.expensetracker.models.Category;

public class Analytics {

    private final TransactionDao transactionDao;
    public Analytics() {
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
    }

    public void getAnalytics(String categoryName ) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        int all = category.getBudget();

        // Observe the LiveData for total value
        transactionDao.getTotalValueForCategory(categoryName).observeForever(new Observer<Double>() {
            @Override
            public void onChanged(Double spent) {
                if (spent == null) {
                    spent = 0.0;
                }
                double available = all - spent;

                System.out.println("all: " + all + " spent: " + spent + " available: " + available);

            }
        });
    }

}
