package com.expensetracker.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.expensetracker.models.Transaction;

import com.expensetracker.dao.TransactionDao;

@TypeConverters(Converters.class) // Register the Type Converter
@Database(entities = {Transaction.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
}
