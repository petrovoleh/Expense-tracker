package com.expensetracker.data;

import java.util.ArrayList;
import java.util.List;

public class Categories {
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

    private static final List<Category> categories = createAllCategories();
    public static List<Category> createAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        for (String categoryName : categoriesNames) {
            Category category = new Category();
            category.setName(categoryName);
            category.setBudget(100);
            categoryList.add(category);
        }
        return categoryList;
    }

    public static List<Category> getCategories() {
        return categories;
    }
    public static String[] getCategoriesNames() {
        return categoriesNames;
    }

}
