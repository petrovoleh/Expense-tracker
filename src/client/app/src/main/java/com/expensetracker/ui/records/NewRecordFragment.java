package com.expensetracker.ui.records;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.Categories;
import com.expensetracker.data.FileManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewRecordFragment extends Fragment {

    private NewRecordViewModel newRecordViewModel;
    private EditText editTextValue;
    private EditText editTextPlace;
    private Context context;
    private FileManager fileManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        fileManager = new FileManager(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newRecordViewModel =
                new ViewModelProvider(this).get(NewRecordViewModel.class);

        View root = inflater.inflate(R.layout.fragment_new_record, container, false);

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
            if(value >100000.0){
                value = 100000.0;
            }
            // Call the method in ViewModel to add the transaction
            newRecordViewModel.addTransaction(category, value, place);
            // Получаем NavController из главной активности
            saveRecord(category, value, place);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

            // Навигация к навигационному пункту signup
            navController.navigate(R.id.navigation_records);
        });

        return root;
    }
    private void saveRecord(String category, double value, String place) {
        String token = getTokenFromFile(); // Assuming you have a method to retrieve the auth token
        if(token == null || token.isEmpty()){
            return;
        }
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("category", category);
            jsonObject.put("value", value);
            jsonObject.put("description", place);
            jsonObject.put("userId", "");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + "/api/record/add")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to save record to the server", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Record saved successfully", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to save record", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
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

}
