package com.expensetracker.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.data.Currencies;
import com.expensetracker.databinding.FragmentProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
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
        ProfileViewModel homeViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Check if account data exists in the file
        if (!checkAccountData()) {
            // If data doesn't exist, navigate the user to the sign-in page
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_signin);
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        } else {
            // If account data exists, load username and email
            try {
                JSONObject accountData = fileManager.readFromFile("accountdata.json");
                if (accountData != null) {
                    String username = accountData.getString("username");
                    String email = accountData.getString("email");
                    // Set username and email to TextViews
                    binding.textName.setText(username);
                    binding.textEmail.setText(email);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Button exitButton = root.findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileManager.writeToFile("accountdata.json", new JSONObject());
                // Получаем NavController из главной активности
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_signin);
            }
        });


        Spinner spinnerCurrency = root.findViewById(R.id.spinnerCurrency);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, Currencies.getCurrenciesNames());
        spinnerCurrency.setAdapter(adapter);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = (String) parent.getItemAtPosition(position);
                Currencies.setCurrency(selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return root;
    }
    @Override
    public void onResume() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        super.onResume();
        if (!checkAccountData()) {
            // If data doesn't exist, navigate the user to the sign-in page
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_signin);
        } else {
            // If account data exists, load username and email
            try {
                JSONObject accountData = fileManager.readFromFile("accountdata.json");
                if (accountData != null) {
                    String username = accountData.getString("username");
                    String email = accountData.getString("email");
                    // Set username and email to TextViews
                    binding.textName.setText(username);
                    binding.textEmail.setText(email);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Method to check if account data exists in the file
    private boolean checkAccountData() {
        try {
            JSONObject accountData = fileManager.readFromFile("accountdata.json");
            return accountData != null && accountData.has("username") && !accountData.getString("username").isEmpty();
        } catch (JSONException e) {
            fileManager.writeToFile("accountdata.json", new JSONObject());
            e.printStackTrace();
        }
        return false;
    }
}

