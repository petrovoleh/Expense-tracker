package com.expensetracker.ui.signin;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import io.github.cdimascio.dotenv.Dotenv;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.databinding.FragmentSigninBinding;
import com.expensetracker.ui.signup.SignUpFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    private FileManager fileManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SignInViewModel homeViewModel =
                new ViewModelProvider(this).get(SignInViewModel.class);
        fileManager = new FileManager(getContext());
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

        // Use the Context object to get the ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check network connectivity
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            System.out.println("no internet connection");
            Toast.makeText(getContext(), "no internet connection", Toast.LENGTH_SHORT).show();
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
                        System.out.println(responseData+ Toast.LENGTH_SHORT);
                        Toast.makeText(getContext(), responseData, Toast.LENGTH_SHORT).show();
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
