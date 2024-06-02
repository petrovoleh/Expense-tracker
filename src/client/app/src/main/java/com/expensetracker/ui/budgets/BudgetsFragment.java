package com.expensetracker.ui.budgets;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.expensetracker.R;
import com.expensetracker.data.Categories;
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
        Button buttonClear = root.findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBudgets();
            }
        });
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

    private void clearBudgets(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Are you sure you want to clear all budgets?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearAllBudgets();
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearAllBudgets() {
        for (int i = 0; i < 10; i++) {
            Categories.getCategories().get(i).setBudget(100);
            editTextCategories[i].setText("100");
        }
        Categories.saveCategoriesToDatabase();
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
            if (!text.isEmpty() ) {
                int budget = 100000;
                if (text.length() < 10) {
                    budget = Integer.parseInt(text);
                }
                if (budget < 100000) {
                    if (budget > 0) {
                    Categories.getCategories().get(i).setBudget(budget);
                    }
                    else{
                        editTextCategories[i].setText("0");
                        Categories.getCategories().get(i).setBudget(0);
                    }

                }
                else{
                    editTextCategories[i].setText("100000");
                    Categories.getCategories().get(i).setBudget(100000);
                }
            }
        }
        Categories.saveCategoriesToDatabase();
    }

}
