package com.expensetracker.ui.records;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.expensetracker.data.Categories;
import com.expensetracker.models.Transaction;
import com.expensetracker.dao.TransactionDao;
import com.expensetracker.data.AppDatabase;
import com.expensetracker.MainActivity;

import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

public class RecordsViewModel extends ViewModel {

    private final MutableLiveData<Transaction> mTransaction = new MutableLiveData<>();
    private final TransactionDao transactionDao;

    public RecordsViewModel() {
        super();
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
    }

    public LiveData<Transaction> getTransaction() {
        return mTransaction;
    }

    public void addTransaction(String category, double value, String place) {
        Transaction transaction = new Transaction(category, value, place);

        // Perform database operation asynchronously using AsyncTask
        new InsertTransactionTask().execute(transaction);

        // Log the details of the new transaction
        Log.println(Log.INFO, "New Transaction", "Category: " + category + ", Value: " + value + ", Place: " + place);
    }

    private class InsertTransactionTask extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.insert(transactions[0]);
            return null;
        }
    }

}
