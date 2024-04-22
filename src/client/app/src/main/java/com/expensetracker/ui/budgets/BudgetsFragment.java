package com.expensetracker.ui.budgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.R;
import com.expensetracker.data.Categories;
import com.expensetracker.databinding.FragmentHomeBinding;

public class BudgetsFragment extends Fragment {

    private EditText editTextFood;
    // Declare EditText fields for other categories

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_budgets, container, false);

        // Bind EditText fields
        editTextFood = root.findViewById(R.id.editTextFood);
        // Bind EditText fields for other categories

        // Set onClickListener for Save button
        Button saveButton = root.findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBudgets();
            }
        });

        return root;
    }

    private void saveBudgets() {
        String text = editTextFood.getText().toString();
        if (text.isEmpty())
                return;
        // Get budget values from EditText fields
        int foodBudget = Integer.parseInt(text);
        Categories.getCategories().get(0).setBudget(foodBudget);
    }
}
