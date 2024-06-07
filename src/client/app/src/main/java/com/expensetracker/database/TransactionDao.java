package com.expensetracker.database;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.expensetracker.models.Transaction;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);
    @Query("SELECT * FROM transactions")
    LiveData<List<Transaction>> getAllTransactions();

    // Query method to get transactions filtered by category
    @Query("SELECT * FROM transactions WHERE category = :category")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);
    @Query("SELECT SUM(value) FROM transactions WHERE category = :category AND time >= :startDate")
    Double getTotalValueForCategory(String category, Date startDate);

    // Query method to get transactions filtered by date
    @Query("SELECT * FROM transactions WHERE time >= :startDate AND time <= :endDate")
    LiveData<List<Transaction>> getTransactionsByDate(Date startDate, Date endDate);

    // Delete method by ID
    @Delete
    void deleteTransaction(Transaction transaction);
}
