package com.expensetracker.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.MainActivity;
import com.expensetracker.R;
import com.expensetracker.data.Currencies;
import com.expensetracker.data.FileManager;
import com.expensetracker.databinding.FragmentEditProfileBinding;
import com.expensetracker.validators.Validator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
        ProfileViewModel homeViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
        if (avatarFile.exists()) {
            // If the file exists, load the image into ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            imageView = root.findViewById(R.id.imageView);
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
                    binding.editTextName.setText(username);
                    binding.editTextEmail.setText(email);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Button deleteButton = root.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
        Button changeAvatarButton = root.findViewById(R.id.editButton);
        changeAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        Button saveButton = root.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.editTextName.getText().toString().trim();
                String email = binding.editTextEmail.getText().toString().trim();
                if(Validator.validateName(getContext(), username, binding.editTextName)
                        || Validator.validateEmail(getContext(), email, binding.editTextEmail)
                    ){
                        saveAccountData(username, email);
                        Toast.makeText(requireContext(), "Account data saved", Toast.LENGTH_SHORT).show();

                    }else{
                    Toast.makeText(requireContext(), "Account data was not saved", Toast.LENGTH_SHORT).show();

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
        // Example code using OkHttp library
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

        // Determine the media type based on the file extension
        String extension = getFileExtension(file.getName());
        MediaType mediaType;
        if ("jpeg".equalsIgnoreCase(extension) || "jpg".equalsIgnoreCase(extension)) {
            mediaType = MEDIA_TYPE_JPEG;
        } else if ("png".equalsIgnoreCase(extension)) {
            mediaType = MEDIA_TYPE_PNG;
        } else {
            // Unsupported file type
            return;
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file))
                .build();

        Request request = new Request.Builder()
                .url(MainActivity.baseUrl+"/api/auth/avatar")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure to send the photo to the server
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle successful response from the server after sending the photo
                    String responseData = response.body().string();
                    Log.d("response",responseData);
                } else {
                    String responseData = response.body().string();
                    Log.d("response",responseData);
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
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed deletion, delete the avatar
                deleteAvatar();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteAvatar() {
        // Delete the avatar file
        File avatarFile = new File(requireContext().getFilesDir(), "avatars/avatar.jpg");
        if (avatarFile.exists()) {
            avatarFile.delete();
        }

        // Update the ImageView to display a placeholder image
        imageView.setImageResource(R.drawable.account);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                // Load the selected image into ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                ImageView imageView = requireView().findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);

                // Save the selected image to local storage
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
        sendPhotoToServer(file);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(requireContext(), "Avatar saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save avatar", Toast.LENGTH_SHORT).show();
        }
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
                    binding.editTextName.setText(username);
                    binding.editTextEmail.setText(email);
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
    private void saveAccountData(String username, String email) {
        JSONObject accountData = fileManager.readFromFile("accountdata.json");

        try {
            accountData.put("username", username);
            accountData.put("email", email);
            fileManager.writeToFile("accountdata.json", accountData);

            // Make the API call to update the user's profile
            String token = getTokenFromFile();
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("username", username);
            requestBodyJson.put("email", email);

            RequestBody requestBody = RequestBody.create(JSON, requestBodyJson.toString());

            Request request = new Request.Builder()
                    .url(MainActivity.baseUrl + "/api/auth/update")
                    .addHeader("Authorization", "Bearer " + token)
                    .put(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    // Handle failure to update profile
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Handle successful response from the server after updating profile
                        String responseData = response.body().string();
                        Log.d("response", responseData);
                    } else {
                        // Handle unsuccessful response
                        String responseData = response.body().string();
                        Log.d("response", responseData);
                    }
                }
            });

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
