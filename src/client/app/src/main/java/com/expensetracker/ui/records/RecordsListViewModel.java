package com.expensetracker.ui.records;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.expensetracker.models.Transaction;
import com.expensetracker.dao.TransactionDao;
import com.expensetracker.data.AppDatabase;
import com.expensetracker.MainActivity;

import java.util.Calendar;
import java.util.List;

public class RecordsListViewModel extends ViewModel {

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
    public void showAllCategories(){
        transactions = transactionDao.getAllTransactions();
    }
    public RecordsListViewModel() {
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
        transactions = transactionDao.getAllTransactions();
    }

    public LiveData<List<Transaction>> getTransactions() {
        Log.println(Log.INFO, "Transaction", "get transactions");
        return transactions;
    }
}