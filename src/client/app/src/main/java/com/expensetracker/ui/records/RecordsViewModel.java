package com.expensetracker.ui.records;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.expensetracker.models.Transaction;
import com.expensetracker.database.TransactionDao;
import com.expensetracker.database.AppDatabase;
import com.expensetracker.MainActivity;

import java.util.Calendar;
import java.util.List;

public class RecordsViewModel extends ViewModel {

    private LiveData<List<Transaction>> transactions;
    private final TransactionDao transactionDao;
    public void setCategoryFilter(String category){
        transactions = transactionDao.getTransactionsByCategory(category);
    }
    public void setDateFilter(Calendar date1, Calendar date2) {
        // Set time for date1 to 00:00:00
        date1.set(Calendar.HOUR_OF_DAY, 0);
        date1.set(Calendar.MINUTE, 0);
        date1.set(Calendar.SECOND, 0);
        date1.set(Calendar.MILLISECOND, 0);

        // Set time for date2 to 23:59:59
        date2.set(Calendar.HOUR_OF_DAY, 23);
        date2.set(Calendar.MINUTE, 59);
        date2.set(Calendar.SECOND, 59);
        date2.set(Calendar.MILLISECOND, 999);

        transactions = transactionDao.getTransactionsByDate(date1.getTime(), date2.getTime());
    }
    private LiveData<List<Transaction>> setDateFilterFromBeginningOfMonth() {
        Calendar currentDate = Calendar.getInstance();
        // Get the current month and year
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);

        // Create a Calendar instance for the first day of the current month at 00:01
        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(Calendar.YEAR, year);
        firstDayOfMonth.set(Calendar.MONTH, month);
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        firstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        firstDayOfMonth.set(Calendar.MINUTE, 1);
        firstDayOfMonth.set(Calendar.SECOND, 0);
        firstDayOfMonth.set(Calendar.MILLISECOND, 0);

        // Set time for currentDate to 23:59:59
        currentDate.set(Calendar.HOUR_OF_DAY, 23);
        currentDate.set(Calendar.MINUTE, 59);
        currentDate.set(Calendar.SECOND, 59);
        currentDate.set(Calendar.MILLISECOND, 999);

       return transactionDao.getTransactionsByDate(firstDayOfMonth.getTime(), currentDate.getTime());
    }
    public void showAllCategories(){
        transactions = setDateFilterFromBeginningOfMonth();
    }
    public RecordsViewModel() {
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
        transactions = setDateFilterFromBeginningOfMonth();
    }

    public LiveData<List<Transaction>> getTransactions() {
        Log.println(Log.INFO, "Transaction", "get transactions");
        return transactions;
    }
}