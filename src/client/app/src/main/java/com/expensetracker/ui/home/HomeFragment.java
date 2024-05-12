package com.expensetracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SuggestionsInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.adapters.SuggestionsAdapter;
import com.expensetracker.data.Categories;
import com.expensetracker.data.Notifications;
import com.expensetracker.databinding.FragmentHomeBinding;
import com.expensetracker.suggestions.Suggestion;
import com.expensetracker.ui.records.RecordsViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<String> suggestions;
    private List<String> notificationsList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Suggestion suggestion = new Suggestion();
            suggestions = suggestion.getSuggestions();
            Notifications notifications = new Notifications();
            notificationsList = notifications.getNotifications();
            // Update UI on the main thread
            getActivity().runOnUiThread(this::updateText);
            executor.shutdown();
        });
        return root;
    }

    private void updateText() {
        RecyclerView recyclerView = binding.recyclerViewSuggestions;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestions.addAll(notificationsList);
        SuggestionsAdapter adapter = new SuggestionsAdapter(suggestions);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
