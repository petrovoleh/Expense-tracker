package com.expensetracker.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {

    private static final String TAG = "FileManager";

    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    // Метод для записи данных в файл
    public void writeToFile(String fileName, JSONObject data) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(data.toString().getBytes());
            Log.d(TAG, "Data written to file: " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file: " + e.getMessage());
        }
    }

    // Метод для чтения данных из файла
    public JSONObject readFromFile(String fileName) {
        JSONObject jsonData = null;
        try (FileInputStream fis = context.openFileInput(fileName)) {
            StringBuilder stringBuilder = new StringBuilder();
            int character;
            while ((character = fis.read()) != -1) {
                stringBuilder.append((char) character);
            }
            jsonData = new JSONObject(stringBuilder.toString());
            Log.d(TAG, "Data read from file: " + fileName);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading from file: " + e.getMessage());
        }
        return jsonData;
    }
}

