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
import java.util.List;

public class RecordsListViewModel extends ViewModel {

    private final LiveData<List<Transaction>> transactions;
    private final TransactionDao transactionDao;

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