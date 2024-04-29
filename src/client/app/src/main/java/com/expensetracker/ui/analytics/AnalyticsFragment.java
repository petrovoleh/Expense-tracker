package com.expensetracker.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.data.Analytics;
import com.expensetracker.adapters.AnalyticsAdapter;
import com.expensetracker.databinding.FragmentAnalyticsBinding;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private RecyclerView recyclerViewAnalytics;
    private AnalyticsAdapter analyticsAdapter;
    private Analytics analytics;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerViewAnalytics = root.findViewById(R.id.recyclerViewAnalytics);
        recyclerViewAnalytics.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create an adapter with an empty list of transactions
        this.analytics = new Analytics();
        getAnalytics();
        analyticsAdapter = new AnalyticsAdapter(analytics);
        recyclerViewAnalytics.setAdapter(analyticsAdapter);

        return root;
    }

    void getAnalytics(){
        analytics.getAllAnalytics();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}