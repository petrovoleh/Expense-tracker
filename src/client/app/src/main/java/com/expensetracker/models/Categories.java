package com.expensetracker.models;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.Observer;

import com.expensetracker.MainActivity;
import com.expensetracker.dao.CategoryDao;
import com.expensetracker.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Categories {
    private static final CategoryDao categoryDao =CreateCategory();
    private final static String[] categoriesNames = {
            "Food",
            "Transportation",
            "Entertainment",
            "Shopping",
            "Bills",
            "Others",
            "Healthcare",
            "Travel",
            "Education",
            "Utilities"
    };
    private static CategoryDao CreateCategory (){
        AppDatabase database = MainActivity.getDatabase();
        return database.categoryDao();
    }
    private static List<Category> categories;
    public static CompletableFuture<Void> createAllCategoriesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // Post a Runnable to the main thread to observe LiveData
            new Handler(Looper.getMainLooper()).post(() -> {
                categoryDao.getAllCategories().observeForever(new Observer<List<Category>>() {
                    @Override
                    public void onChanged(List<Category> retrievedCategories) {
                        // Stop observing to avoid memory leaks
                        categoryDao.getAllCategories().removeObserver(this);

                        // Process the retrieved categories
                        if (retrievedCategories == null || retrievedCategories.isEmpty()) {
                            categories = new ArrayList<>();
                            for (String categoryName : categoriesNames) {
                                Category category = new Category();
                                category.setName(categoryName);
                                category.setBudget(100);
                                categories.add(category);

                                // Insert the default category into the database
                                categoryDao.insert(category);
                            }
                        } else {
                            categories = retrievedCategories;
                        }

                        // Complete the CompletableFuture
                        CompletableFuture.completedFuture(null);
                    }
                });
            });
            return null;
        });
    }


    public static List<Category> getCategories() {

        return categories;
    }
    public static String[] getCategoriesNames() {
        return categoriesNames;
    }
    public static void saveCategoriesToDatabase() {
        List<Category> categories = Categories.getCategories();
        for (Category category : categories) {
            new InsertCategoryTask().execute(category);
        }
    }

    private static class InsertCategoryTask extends AsyncTask<Category, Void, Void> {
        @Override
        protected Void doInBackground(Category... categories) {
            Category category = categories[0];
            categoryDao.insert(category);
            return null;
        }
    }
}
