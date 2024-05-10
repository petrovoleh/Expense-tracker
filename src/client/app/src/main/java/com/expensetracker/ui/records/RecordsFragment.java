package com.expensetracker.ui.records;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.data.Categories;
import com.expensetracker.models.Transaction;
import com.expensetracker.adapters.TransactionAdapter;
import com.expensetracker.suggestions.Suggestion;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordsFragment extends Fragment {

    private RecordsViewModel recordsViewModel;
    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
private TextView month;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recordsViewModel = new ViewModelProvider(this).get(RecordsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_records_list, container, false);


        month = root.findViewById(R.id.month);
        selectParticularMonth();

        recyclerViewTransactions = root.findViewById(R.id.recyclerViewTransactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create an adapter with an empty list of transactions
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        recyclerViewTransactions.setAdapter(transactionAdapter);

        ImageButton newRecordButton = root.findViewById(R.id.newRecordButton);
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

        recordsViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
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
        recordsViewModel.showAllCategories();
        recordsViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });
    }

    // Method to filter transactions by category
    public void filterByCategory(String category) {
        if(category.equals("Show all")){
            recordsViewModel.showAllCategories();
            selectParticularMonth();
        }
        else {
            recordsViewModel.setCategoryFilter(category);
            month.setText(category);
        }
        recordsViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });

    }

    // Method to filter transactions by date
    public void filterByDate(Calendar date1, Calendar date2) {
        recordsViewModel.setDateFilter(date1, date2);
        recordsViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });

        // Format the months for display
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String formattedMonth1 = monthFormat.format(date1.getTime());
        String formattedMonth2 = monthFormat.format(date2.getTime());
        if(formattedMonth1.equals(formattedMonth2))
            month.setText(formattedMonth1);
        else
            month.setText(formattedMonth1 + " - " + formattedMonth2);
    }
    private void selectParticularMonth(){
        Calendar calendar = Calendar.getInstance();
        int monthNumber = calendar.get(Calendar.MONTH);

        String[] monthNames = new DateFormatSymbols().getMonths();

        if (monthNumber < monthNames.length) {
            String monthString = monthNames[monthNumber];
            month.setText(monthString);
        }
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
        int monthN = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, monthOfYear, dayOfMonth1) -> {
            Calendar fromDate = Calendar.getInstance();
            fromDate.set(year1, monthOfYear, dayOfMonth1);

            DatePickerDialog.OnDateSetListener toDateListener = (view2, year2, monthOfYear2, dayOfMonth2) -> {
                Calendar toDate = Calendar.getInstance();
                toDate.set(year2, monthOfYear2, dayOfMonth2);
                filterByDate(fromDate, toDate);
            };

            new DatePickerDialog(requireContext(), toDateListener, year, monthN, dayOfMonth).show();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, year, monthN, dayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


}
