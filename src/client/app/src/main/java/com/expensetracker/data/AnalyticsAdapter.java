package com.expensetracker.data;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.data.Analytics;
import com.expensetracker.data.AnalyticsAdapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnalyticsAdapter extends RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder> {

    public final Analytics analytics;

    public AnalyticsAdapter(Analytics analytics){
        this.analytics = analytics;
    }

    @NonNull
    @Override
    public AnalyticsAdapter.AnalyticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_analytics, parent, false);
        return new AnalyticsAdapter.AnalyticsViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull AnalyticsAdapter.AnalyticsViewHolder holder, int position) {
        String [] names = analytics.getNames();
        String[] rows = new String[10];

            analytics.getAnalytics(names[position], result -> {
                rows[position] = result;
                String[] row = rows[position].split(" ");
                holder.textName.setText(row[0]);
                holder.textAllocated.setText(row[1]);
                holder.textUsed.setText(row[2]);
                holder.textAvailable.setText(row[3]);

                Log.d("analytics", rows[position] + "\n\n");

            });

    }

    @Override
    public int getItemCount() {
        return 10;
    }

    static class AnalyticsViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textAllocated;
        TextView textUsed;
        TextView textAvailable;

        public AnalyticsViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textAllocated = itemView.findViewById(R.id.textAllocated);
            textUsed = itemView.findViewById(R.id.textUsed);
            textAvailable= itemView.findViewById(R.id.textAvailable);
        }
    }
}
