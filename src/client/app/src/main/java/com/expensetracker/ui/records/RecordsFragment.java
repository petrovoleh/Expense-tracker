package com.expensetracker.ui.records;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.R;
import com.expensetracker.data.Categories;

public class RecordsFragment extends Fragment {

    private RecordsViewModel recordsViewModel;
    private EditText editTextValue;
    private EditText editTextPlace;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordsViewModel =
                new ViewModelProvider(this).get(RecordsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_records, container, false);

        Spinner spinnerCategory = root.findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, Categories.getCategoriesNames());
        spinnerCategory.setAdapter(adapter);

        editTextValue = root.findViewById(R.id.editTextValue);
        editTextPlace = root.findViewById(R.id.editTextPlace);

        Button buttonAddRecord = root.findViewById(R.id.buttonAddRecord);
        buttonAddRecord.setOnClickListener(view -> {
            String category = spinnerCategory.getSelectedItem().toString();
            String valueText = editTextValue.getText().toString().trim();
            String place = editTextPlace.getText().toString().trim();

            TextView textViewError = root.findViewById(R.id.textHome);
            // Check if any of the values is null or empty
            if (category.isEmpty() || valueText.isEmpty() || place.isEmpty()) {
                textViewError.setText("Please fill in all fields");
                return;
            }

            // Convert value to double if it's not empty
            double value = Double.parseDouble(valueText);

            // Call the method in ViewModel to add the transaction
            recordsViewModel.addTransaction(category, value, place);
            // Получаем NavController из главной активности
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

            // Навигация к навигационному пункту signup
            navController.navigate(R.id.navigation_records);
        });

        return root;
    }

}
