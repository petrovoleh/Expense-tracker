package com.expensetracker.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.Currencies;
import com.expensetracker.data.Notifications;
import com.expensetracker.database.AppDatabase;
import com.expensetracker.database.TransactionDao;
import com.expensetracker.models.Transaction;
import com.expensetracker.suggestions.Suggestion;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final NumberFormat currency = Currencies.currency;
    private List<Transaction> transactions;
    private TransactionViewHolder holder;
    private final TransactionDao transactionDao;

    private Context context; // Add context field

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
        this.context = context; // Store context
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
        if (transactions.isEmpty()){
            holder.buttonDelete.setVisibility(View.INVISIBLE);
            holder.buttonDelete.setEnabled(false);
            holder.textViewTransaction.setText(holder.itemView.getContext().getString(R.string.no_records_message));
            return;
        }
        else{
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonDelete.setEnabled(true);
        }
        Transaction transaction = transactions.get(position);
        Locale currentLocale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm", currentLocale);
        String formattedDate = dateFormat.format(transaction.getTime());
        String text = formattedDate + " " + transaction.getCategory() + "\n" + transaction.getPlace() + " " + currency.format(transaction.getValue());
        holder.textViewTransaction.setText(text);
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecord(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (transactions.isEmpty()) {
          return 1;
        } else {
            return transactions.size();
        }

    }

    private void deleteRecord(Transaction transaction){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this record?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    transactionDao.deleteTransaction(transaction);
                    executor.shutdown();
                });
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTransaction;
        Button buttonDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTransaction = itemView.findViewById(R.id.textViewTransaction);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
