package com.expensetracker.ui.records;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.models.Categories;
import com.expensetracker.models.Transaction;
import com.expensetracker.models.TransactionAdapter;

import java.util.Calendar;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RecordsListFragment extends Fragment {

    private RecordsListViewModel recordsListViewModel;
    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recordsListViewModel = new ViewModelProvider(this).get(RecordsListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_records_list, container, false);

        recyclerViewTransactions = root.findViewById(R.id.recyclerViewTransactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create an adapter with an empty list of transactions
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        recyclerViewTransactions.setAdapter(transactionAdapter);

        Button newRecordButton = root.findViewById(R.id.newRecordButton);
        Button filterCategoryButton = root.findViewById(R.id.filterCategoryButton);
        Button filterDateButton = root.findViewById(R.id.filterDateButton);


        newRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_new_record);
            }
        });

        filterCategoryButton.setOnClickListener(v -> showCategoryFilterDialog());

        filterDateButton.setOnClickListener(v -> showDatePicker());

        recordsListViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });

        return root;
    }

    @Override
    public void onResume() {

        super.onResume();
        recordsListViewModel.showAllCategories();
        recordsListViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });
    }

    // Method to filter transactions by category
    public void filterByCategory(String category) {
        if(category.equals("Show all")){
            recordsListViewModel.showAllCategories();
        }
        else {
            recordsListViewModel.setCategoryFilter(category);
        }
        recordsListViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });
    }

    // Method to filter transactions by date
    public void filterByDate(Calendar date1,Calendar date2) {
        recordsListViewModel.setDateFilter(date1, date2);
        recordsListViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });
    }

    // Method to show a dialog for selecting category filter
    private void showCategoryFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category");

        String[] categories = new String[Categories.getCategoriesNames().length + 1];
        categories[0] = "Show all";
        System.arraycopy(Categories.getCategoriesNames(), 0, categories, 1, Categories.getCategoriesNames().length);

        builder.setItems(categories, (dialog, which) -> {
            String selectedCategory = categories[which];
            filterByCategory(selectedCategory);
        });

        builder.create().show();
    }

    // Method to show a date picker for selecting date filter
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, monthOfYear, dayOfMonth1) -> {
            Calendar fromDate = Calendar.getInstance();
            fromDate.set(year1, monthOfYear, dayOfMonth1);

            DatePickerDialog.OnDateSetListener toDateListener = (view2, year2, monthOfYear2, dayOfMonth2) -> {
                Calendar toDate = Calendar.getInstance();
                toDate.set(year2, monthOfYear2, dayOfMonth2);
                filterByDate(fromDate, toDate);
            };

            new DatePickerDialog(requireContext(), toDateListener, year, month, dayOfMonth).show();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


}
