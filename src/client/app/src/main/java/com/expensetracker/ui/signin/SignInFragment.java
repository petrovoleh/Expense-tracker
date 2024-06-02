package com.expensetracker.ui.signin;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.databinding.FragmentSigninBinding;
import com.expensetracker.validators.Validator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignInFragment extends Fragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private FragmentSigninBinding binding;
    private FileManager fileManager;
    private TextView errorText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SignInViewModel homeViewModel =
                new ViewModelProvider(this).get(SignInViewModel.class);
        fileManager = new FileManager(getContext());
        binding = FragmentSigninBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        usernameEditText = root.findViewById(R.id.usernameEditText);
        errorText = root.findViewById(R.id.errorText);
        passwordEditText = root.findViewById(R.id.passwordEditText);
        // Находим кнопку "Sign Up"

        Button signUpButton = root.findViewById(R.id.signUpButton);
        Button signInButton = root.findViewById(R.id.signInButton);
        // Устанавливаем слушатель нажатий
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получаем NavController из главной активности
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_signup);
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().findViewById(android.R.id.content).post(() -> {
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
            Menu menu = bottomNavigationView.getMenu();
            for (int i = 0, size = menu.size(); i < size; i++) {
                MenuItem item = menu.getItem(i);
                item.setChecked(item.getItemId() == R.id.navigation_profile);
            }
        });
    }
    private void signIn() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if(Validator.validateName(getContext(), username, errorText)
                || Validator.validateName(getContext(), password, errorText)
        ){
            return;
        }
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Use the Context object to get the ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check network connectivity
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            System.out.println("no internet connection");
            errorText.setText("The request failed,no internet connection");
        }
        else{
            System.out.println("internet connection");
        }
        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create request body
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Create request object
        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + "/api/auth/signin")
                .post(body)
                .build();
        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println( "Request failed");
                        errorText.setText("The request failed, the server is unavailable, please try again after some time");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle response here
                        if(!response.isSuccessful()) {
                            errorText.setText(responseData);
                        }
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONObject json = new JSONObject();
                            json.put("id", jsonResponse.getString("id"));
                            json.put("username", jsonResponse.getString("username"));
                            json.put("email", jsonResponse.getString("email"));
                            json.put("accessToken", jsonResponse.getString("accessToken"));
                            json.put("tokenType", jsonResponse.getString("tokenType"));
                            json.put("avatar", jsonResponse.getString("avatar"));

                            // Write JSON data to file
                            fileManager.writeToFile("accountdata.json", json);

                            // Download and save the avatar image
                            if(!jsonResponse.getString("avatar").isEmpty()) {
                                String avatarUrl = MainActivity.baseUrl + "/public/images/" + jsonResponse.getString("avatar");
                                downloadAndSaveAvatar(avatarUrl, jsonResponse.getString("accessToken"));
                            }
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                            // Навигация к навигационному пункту signup
                            navController.navigate(R.id.navigation_profile);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        });
    }
    private void downloadAndSaveAvatar(String avatarUrl, String accessToken) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(avatarUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    saveImageToStorage(bitmap);
                }
            }
        });
    }

    private void saveImageToStorage(Bitmap bitmap) {
        File directory = new File(requireContext().getFilesDir(), "avatars");
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, "avatar.jpg");

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            requireActivity().runOnUiThread(() -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.navigation_profile);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
