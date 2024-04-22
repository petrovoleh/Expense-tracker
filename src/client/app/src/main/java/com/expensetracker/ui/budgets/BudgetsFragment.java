package com.expensetracker.ui.budgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.expensetracker.R;
import com.expensetracker.models.Categories;
import com.expensetracker.models.Category;

import java.util.List;

public class BudgetsFragment extends Fragment {

    private EditText[] editTextCategories = new EditText[10];
    private TextView[] textCategories = new TextView[10];
    // Declare EditText fields for other categories

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_budgets, container, false);

        // Move the initializeViews() method call here
        initializeViews(root);

        // Bind EditText fields for other categories
        loadValues();

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

    private void initializeViews(View root) {
        for (int i = 0; i < 10; i++) {
            int editTextId = getResources().getIdentifier("editTextCategory" + (i + 1), "id", requireContext().getPackageName());
            editTextCategories[i] = root.findViewById(editTextId);

            int textId = getResources().getIdentifier("textCategory" + (i + 1), "id", requireContext().getPackageName());
            textCategories[i] = root.findViewById(textId);
        }
    }
    private void loadValues() {
        List<Category> categories = Categories.getCategories();

        for (int i = 0; i < Math.min(categories.size(), 10); i++) {
            int budget = categories.get(i).getBudget();
            String name = categories.get(i).getName();

            editTextCategories[i].setText(String.valueOf(budget));
            textCategories[i].setText(name);
        }
    }

    private void saveBudgets() {
        for (int i = 0; i < 10; i++) {
            String text = editTextCategories[i].getText().toString();
            if (!text.isEmpty()) {
                int budget = Integer.parseInt(text);
                Categories.getCategories().get(i).setBudget(budget);
            }
        }
        Categories.saveCategoriesToDatabase();
    }

}
