package com.expensetracker.network;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import com.expensetracker.MainActivity;

import org.json.JSONObject;

public class ApiClient {

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void signUp(JSONObject requestBody, Callback callback) {
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + "/api/auth/signup")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
