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
        StringBuilder builder = new StringBuilder();
            analytics.getAnalytics(names[position], result -> {
                builder.append(result);
                holder.textViewAnalytics.setText(builder.toString());
                Log.d("analytics", builder.toString()+"\n\n");
            });

    }

    @Override
    public int getItemCount() {
        return analytics.size();
    }

    static class AnalyticsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAnalytics;

        public AnalyticsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAnalytics = itemView.findViewById(R.id.textViewAnalytics);
        }
    }
}
