package com.expensetracker.ui.records;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.expensetracker.models.Transaction;
import com.expensetracker.database.TransactionDao;
import com.expensetracker.database.AppDatabase;
import com.expensetracker.MainActivity;

public class NewRecordViewModel extends ViewModel {

    private final MutableLiveData<Transaction> mTransaction = new MutableLiveData<>();


    public NewRecordViewModel() {
        super();
    }

    public LiveData<Transaction> getTransaction() {
        return mTransaction;
    }

    public Transaction addTransaction(String category, double value, String place) {
        Transaction transaction = new Transaction(category, value, place);

        // Perform database operation asynchronously using AsyncTask
         return transaction;
    }



}
