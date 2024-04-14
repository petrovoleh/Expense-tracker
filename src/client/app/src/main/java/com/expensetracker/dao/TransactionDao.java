package com.expensetracker.dao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.expensetracker.models.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);
    @Query("SELECT * FROM transactions")
    LiveData<List<Transaction>> getAllTransactions();
}
