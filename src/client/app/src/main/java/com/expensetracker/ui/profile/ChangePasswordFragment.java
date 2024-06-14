package com.expensetracker.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.databinding.FragmentChangePasswordBinding;
import com.expensetracker.databinding.FragmentEditProfileBinding;
import com.expensetracker.validators.Validator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;
    private Context context;
    private ImageView imageView;
    private TextView textView;
    private FileManager fileManager;
    private static final int PICK_IMAGE_REQUEST = 1;
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

        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        textView = binding.textView2;
        File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
        if (avatarFile.exists()) {
            // If the file exists, load the image into ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            imageView = root.findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
        }


        Button saveButton = root.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve old password, new password, and token
                String oldPassword = binding.editTextOldPassword.getText().toString().trim(); // Assuming old password is entered in editTextName
                String newPassword = binding.editTextNewPassword.getText().toString().trim(); // Assuming new password is entered in editTextEmail
                String newPassword2 = binding.editTextNewPassword2.getText().toString().trim(); // Assuming new password is entered in editTextEmail
                if(Validator.validateTwoPasswords(context,newPassword,newPassword2,textView) || Validator.validatePassword(context,newPassword,textView)){
                    String token = getTokenFromFile();
                    // Call changePassword method
                    changePassword(oldPassword, newPassword, token);
                }

            }
        });

        Button backButton = root.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.navigation_profile);
            }
        });
        return root;
    }// Inside ChangePasswordFragment
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
    private void changePassword(String oldPassword, String newPassword, String token) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("oldPassword", oldPassword);
            requestBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create request body
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Create request object with authorization header
        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + "/api/auth/changepassword")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
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
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                            navController.navigate(R.id.navigation_profile);
                        }
                    }
                });
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void saveAccountData(String username, String email) {
        JSONObject accountData = fileManager.readFromFile("accountdata.json");

        try {
            accountData.put("username", username);
            accountData.put("email", email);
            fileManager.writeToFile("accountdata.json", accountData);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save account data", Toast.LENGTH_SHORT).show();
        }
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
