package com.expensetracker.ui.signup;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.databinding.FragmentSignupBinding;
import com.expensetracker.validators.Validator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

public class SignUpFragment extends Fragment {

    private FragmentSignupBinding binding;
    private EditText usernameEditText;
    private EditText emailEditText;
    private TextView errorText;
    private EditText passwordEditText;
    private FileManager fileManager;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SignUpViewModel homeViewModel =
                new ViewModelProvider(this).get(SignUpViewModel.class);
        fileManager = new FileManager(getContext());
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        errorText = root.findViewById(R.id.errorText);
        usernameEditText = root.findViewById(R.id.usernameEditText);
        emailEditText = root.findViewById(R.id.emailEditText);
        passwordEditText = root.findViewById(R.id.passwordEditText);

        Button signUpButton = root.findViewById(R.id.signUpButton);
        Button signInButton = root.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получаем NavController из главной активности
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                // Навигация к навигационному пункту signup
                navController.navigate(R.id.navigation_signin);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        return root;
    }

    private void signUp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if(
        Validator.validateName(getContext(), username, errorText) ||
        !Validator.validateEmail(getContext(),email,errorText) ||
        !Validator.validatePassword(getContext(),password,errorText)
        ){
            return;
        }


        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create request body
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Create request object
        Request request = new Request.Builder()
                .url(MainActivity.baseUrl +"/api/auth/signup")
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
                        errorText.setText("The request failed, the server is unavailable, please try again after some time");
                        System.out.println("Request failed");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(responseData);
                        // Handle response here

                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONObject json = new JSONObject();
                            json.put("id", jsonResponse.getString("id"));
                            json.put("username", jsonResponse.getString("username"));
                            json.put("email", jsonResponse.getString("email"));
                            json.put("accessToken", jsonResponse.getString("accessToken"));
                            json.put("tokenType", jsonResponse.getString("tokenType"));

                            // Write JSON data to file
                            fileManager.writeToFile("accountdata.json", json);
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

                            // Навигация к навигационному пункту signup
                            navController.navigate(R.id.navigation_profile);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            JSONObject jsonResponse = null;
                            try {
                                jsonResponse = new JSONObject(responseData);
                                errorText.setText(jsonResponse.getString("message"));
                            } catch (JSONException ex) {
                                throw new RuntimeException(ex);
                            }

                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
