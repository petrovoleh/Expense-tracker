package com.expensetracker.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.expensetracker.MainActivity;
import com.expensetracker.database.AppDatabase;
import com.expensetracker.database.TransactionDao;
import com.expensetracker.models.Category;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlinx.coroutines.CoroutineScope;

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

    public int getAllocated(String categoryName){
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        return category.getBudget();
    }

        public int getAvailable(String categoryName) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        int all = category.getBudget();

        // Observe the LiveData for total value
        Double spent = transactionDao.getTotalValueForCategory(categoryName);
            if (spent == null) {
                spent = 0.0;
            }
            return (int) (all - spent);
    }

    public void getAnalytics(String categoryName, AnalyticsCallback callback) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        int all = category.getBudget();

        // Create a single-threaded ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Fetch total value from the database
            Double spent = transactionDao.getTotalValueForCategory(categoryName);
            if (spent == null) {
                spent = 0.0;
            }

            // Calculate available budget
            int available = (int)(all - spent);

            // Prepare result
            String result = categoryName + " " + all + " " +  spent.intValue() + " " + available;

            // Deliver the result on the main thread
            callback.onAnalyticsResult(result);

            // Shutdown the executor
            executor.shutdown();
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
    // Method to save all analytics to a CSV file
    public void saveAllAnalyticsToCSV(File csvFile) throws IOException {
        FileWriter writer = new FileWriter(csvFile);
        // Write header
        writer.append("Category,Budget,Spent,Available\n");
        for (String name : names) {
            Category category = Categories.getCategory(name);
            assert category != null;
            int budget = category.getBudget();
            Double spent = transactionDao.getTotalValueForCategory(name);
            if (spent == null) {
                spent = 0.0;
            }
            int available = (int) (budget - spent);
            writer.append(name).append(",").append(String.valueOf(budget)).append(",")
                    .append(String.valueOf(spent.intValue())).append(",").append(String.valueOf(available)).append("\n");
        }
        writer.flush();
        writer.close();
    }
}
