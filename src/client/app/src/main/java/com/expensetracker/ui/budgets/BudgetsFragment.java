package com.expensetracker.ui.budgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.Categories;
import com.expensetracker.data.FileManager;
import com.expensetracker.models.Category;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BudgetsFragment extends Fragment {

    private EditText[] editTextCategories = new EditText[10];
    private TextView[] textCategories = new TextView[10];

    private FileManager fileManager;
    // Declare EditText fields for other categories
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fileManager = new FileManager(context);
    }
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

        sendBudgetsToServer(Categories.getCategories());
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
    private void sendBudgetsToServer(List<Category> categories) {
        String token = getTokenFromFile();
        if(token == null || token.isEmpty()){
            return;
        }
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        for (Category category : categories) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", category.getName());
                jsonObject.put("budget", category.getBudget());
                jsonObject.put("userId", "");
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(MainActivity.baseUrl + "/api/budget/add")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to save budgets to the server", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("budgets","Budget for " + category.getName() + " saved successfully");
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to save budget for " + category.getName(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        }
    }

    private String getTokenFromFile() {
        JSONObject accountData = fileManager.readFromFile("accountdata.json");
        try {
            if (accountData != null && accountData.has("accessToken")) {
                return accountData.getString("accessToken");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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

        sendBudgetsToServer(Categories.getCategories());
    }

}
