package com.expensetracker.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.data.Currencies;
import com.expensetracker.databinding.FragmentProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
        if (avatarFile.exists()) {
            // If the file exists, load the image into ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            ImageView imageView = root.findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
        }
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
        Button exitButton = root.findViewById(R.id.back);
        Button editButton = root.findViewById(R.id.editButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileManager.writeToFile("accountdata.json", new JSONObject());
                // Delete the avatar file
                File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
                if (avatarFile.exists()) {
                    avatarFile.delete();
                }
                // Получаем NavController из главной активности
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_signin);
            }
        });

        Button changePasswordButton = root.findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_change_password);
            }
        });
        Button deleteButton = root.findViewById(R.id.deleteButton);
        // Inside your deleteButton OnClickListener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete your account?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User confirmed, proceed with account deletion
                                deleteAccount();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_edit_profile);
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
                JSONObject accountData = fileManager.readFromFile("accountdata.json");
                try {
                    accountData.put("currency", selectedCurrency);
                    fileManager.writeToFile("accountdata.json", accountData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Currency changed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return root;
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
    private void deleteAccount(){
        OkHttpClient client = new OkHttpClient();
        String token = getTokenFromFile();
        // Create DELETE request to the delete account endpoint
        Request request = new Request.Builder()
                .delete()
                .url(MainActivity.baseUrl+"/api/profile/delete")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(responseData);
                        // Handle response here
                        Toast.makeText(requireContext(), responseData, Toast.LENGTH_SHORT).show();
                        if (response.isSuccessful()) {
                            // After sending the request, you may perform additional local cleanup
                            fileManager.writeToFile("accountdata.json", new JSONObject());
                            File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
                            if (avatarFile.exists()) {
                                avatarFile.delete();
                            }

                            // Navigate to the sign-in screen
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                            navController.navigate(R.id.navigation_signin);
                        }
                    }
                });
            }
        });


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

