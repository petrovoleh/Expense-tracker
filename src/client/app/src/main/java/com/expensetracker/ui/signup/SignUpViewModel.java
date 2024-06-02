package com.expensetracker.ui.signup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.network.ApiClient;
import com.expensetracker.validators.Validator;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignUpViewModel extends ViewModel {

    private final MutableLiveData<String> errorText = new MutableLiveData<>();
    private FileManager fileManager;
    NavController navController;

    public LiveData<String> getErrorText() {
        return errorText;
    }

    public void signUp(Context context, String username, String email, String password, NavController navController) {
        String errorCode = Validator.validateNameString(context, username);
        if (errorCode == null) {
            errorCode = Validator.validateEmailString(context, email);
        }
        if (errorCode == null) {
            errorCode = Validator.validatePasswordString(context, password);
        }
        if (errorCode != null) {
            errorText.setValue(errorCode);
            return;
        }

        fileManager = new FileManager(context);
        this.navController = navController;
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            errorText.setValue("Failed to create request body");
            return;
        }

        ApiClient.signUp(requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                errorText.postValue("The request failed, the server is unavailable, please try again after some time");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                handleSignUpResponse(responseData);
            }

            private void handleSignUpResponse(String responseData) {
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject json = new JSONObject();
                    json.put("id", jsonResponse.getString("id"));
                    json.put("username", jsonResponse.getString("username"));
                    json.put("email", jsonResponse.getString("email"));
                    json.put("accessToken", jsonResponse.getString("accessToken"));
                    json.put("tokenType", jsonResponse.getString("tokenType"));
                    Log.d("tag", responseData);
                    fileManager.writeToFile("accountdata.json", json);
                    new Handler(Looper.getMainLooper()).post(() -> navigateToProfile());
                } catch (JSONException e) {
                    handleError(responseData);
                }
            }

            private void handleError(String responseData) {
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    errorText.postValue(jsonResponse.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void navigateToProfile() {navController.navigate(R.id.navigation_profile);
    }
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
}
