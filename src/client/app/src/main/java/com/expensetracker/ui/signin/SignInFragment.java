package com.expensetracker.ui.signin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.R;
import com.expensetracker.databinding.FragmentSigninBinding;
import com.expensetracker.ui.signup.SignUpFragment;
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

public class SignInFragment extends Fragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private FragmentSigninBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SignInViewModel homeViewModel =
                new ViewModelProvider(this).get(SignInViewModel.class);

        binding = FragmentSigninBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        usernameEditText = root.findViewById(R.id.usernameEditText);
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
    private void signIn() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
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
                .url("http://192.168.56.1:8080/api/auth/signin")
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
                        Toast.makeText(getContext(), "Request failed", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), responseData, Toast.LENGTH_SHORT).show();
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
