package com.expensetracker.ui.records;

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
import com.expensetracker.models.Transaction;
import com.expensetracker.models.TransactionAdapter;

import java.util.ArrayList; // Добавим импорт ArrayList
import java.util.List;

public class RecordsListFragment extends Fragment {

    private RecordsListViewModel recordsListViewModel;
    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordsListViewModel = new ViewModelProvider(this).get(RecordsListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_records_list, container, false);

        recyclerViewTransactions = root.findViewById(R.id.recyclerViewTransactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Создаем адаптер с пустым списком транзакций
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        recyclerViewTransactions.setAdapter(transactionAdapter);
        Button newRecordButton = root.findViewById(R.id.newRecordButton);
        newRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_new_record);
            }
        });
        recordsListViewModel.getTransactions().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                transactionAdapter.setTransactions(transactions);
            }
        });

        return root;
    }
}
