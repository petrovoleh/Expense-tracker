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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private Context context;
    private ImageView imageView;
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
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageView = root.findViewById(R.id.imageView);
        File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
        if (avatarFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }

        if (!checkAccountData()) {
            navigateToSignIn();
        } else {
            loadAccountData();
        }

        setupButtonListeners(root);

        return root;
    }

    private void setupButtonListeners(View root) {
        Button deleteButton = root.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        Button changeAvatarButton = root.findViewById(R.id.editButton);
        changeAvatarButton.setOnClickListener(v -> openImagePicker());

        Button saveButton = root.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveProfile());

        Button backButton = root.findViewById(R.id.back);
        backButton.setOnClickListener(v -> navigateToProfile());
    }

    private void navigateToSignIn() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_signin);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
    }

    private void navigateToProfile() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_profile);
    }

    private void saveProfile() {
        String username = binding.editTextName.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
       if (!Validator.validateName(getContext(), username, binding.editTextName)
                && Validator.validateEmail(getContext(), email, binding.editTextEmail)) {
            saveAccountData(username, email);
            Toast.makeText(requireContext(), "Account data saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Account data was not saved", Toast.LENGTH_SHORT).show();
        }
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

    private String getFileExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            return filename.substring(index + 1);
        }
        return "";
    }

    private void sendPhotoToServer(File file) {
        String token = getTokenFromFile();
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/" + getFileExtension(file.getName()));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(mediaType, file))
                .build();

        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + "/api/user/avatar")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("response", response.body().string());
                } else {
                    Log.d("response", response.body().string());
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete the avatar?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteAvatar());
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void deleteAvatar() {
        File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
        if (avatarFile.exists()) {
            avatarFile.delete();
        }
        imageView.setImageResource(R.drawable.account);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                saveImageToStorage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
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
            sendPhotoToServer(file);
            Toast.makeText(requireContext(), "Avatar saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save avatar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        if (!checkAccountData()) {
            navigateToSignIn();
        } else {
            loadAccountData();
        }
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

            updateProfileOnServer(username, email);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save account data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileOnServer(String username, String email) {
        String token = getTokenFromFile();
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", username);
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(MainActivity.baseUrl + "/api/user/update")
                .addHeader("Authorization", "Bearer " + token)
                .put(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("response", response.body().string());
                } else {
                    Log.e("response", response.body().string());
                }
            }
        });
    }

    private void loadAccountData() {
        JSONObject accountData = fileManager.readFromFile("accountdata.json");

        try {
            String username = accountData.getString("username");
            String email = accountData.getString("email");

            binding.editTextName.setText(username);
            binding.editTextEmail.setText(email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkAccountData() {
        JSONObject accountData = fileManager.readFromFile("accountdata.json");
        try {
            if (accountData != null && accountData.has("accessToken")) {
                String accessToken = accountData.getString("accessToken");
                return accessToken != null && !accessToken.isEmpty();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
