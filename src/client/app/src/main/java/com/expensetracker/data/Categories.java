package com.expensetracker.data;

import android.health.connect.datatypes.units.Length;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.Observer;

import com.expensetracker.MainActivity;
import com.expensetracker.database.AppDatabase;
import com.expensetracker.database.CategoryDao;
import com.expensetracker.models.Category;

import java.util.ArrayList;
import java.util.List;

public class Categories {
    private static final CategoryDao categoryDao =CreateCategory();
    private final static String[] categoriesNames = {
            "Food",
            "Transport",
            "Amusement",
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
    public static void createAllCategoriesAsync() {
        categoryDao.getAllCategories().observeForever(new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> retrievedCategories) {
                if (retrievedCategories == null || retrievedCategories.isEmpty() || retrievedCategories.size() <10) {
                    categories = new ArrayList<>();
                    for (String categoryName : categoriesNames) {
                        Category category = new Category();
                        category.setName(categoryName);
                        category.setBudget(100);
                        categories.add(category);

                        // Insert the default category into the database
                        insertCategory(category);
                    }
                } else {
                    categories = retrievedCategories;
                }

                // Stop observing to avoid memory leaks
                categoryDao.getAllCategories().removeObserver(this);

                // Log completion message or perform any follow-up actions
                Log.d("TAG", "Categories creation completed asynchronously.");
            }
        });
    }

    private static void insertCategory(Category category) {
        new AsyncTask<Category, Void, Void>() {
            @Override
            protected Void doInBackground(Category... categories) {
                categoryDao.insert(categories[0]);
                return null;
            }
        }.execute(category);
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
    public static Category getCategory(String name) {
        while(categories == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Category category : categories) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        return null; // Return null if category not found
    }
}
