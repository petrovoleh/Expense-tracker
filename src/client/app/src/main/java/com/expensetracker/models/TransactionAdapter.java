package com.expensetracker.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.expensetracker.R;
import com.expensetracker.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        Locale currentLocale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm", currentLocale);
        String formattedDate = dateFormat.format(transaction.getTime());
        String text = formattedDate + " " + transaction.getCategory() + "\n" + transaction.getPlace() + " " + transaction.getValue() + " EUR";
        holder.textViewTransaction.setText(text);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTransaction;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTransaction = itemView.findViewById(R.id.textViewTransaction);
        }
    }
}
