package com.expensetracker.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.data.Analytics;
import com.expensetracker.databinding.FragmentHomeBinding;
import com.expensetracker.models.Categories;

public class AnalyticsFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AnalyticsViewModel homeViewModel =
                new ViewModelProvider(this).get(AnalyticsViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        getAnalytics();
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    void getAnalytics(){
        Analytics analytics = new Analytics();
        String[] names = Categories.getCategoriesNames();
        for (String name : names) {
            analytics.getAnalytics(name);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}