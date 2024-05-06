package com.expensetracker.ui.analytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.data.Analytics;
import com.expensetracker.adapters.AnalyticsAdapter;
import com.expensetracker.databinding.FragmentAnalyticsBinding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private RecyclerView recyclerViewAnalytics;
    private AnalyticsAdapter analyticsAdapter;
    private Analytics analytics;
    private Button saveCSVButton;
    private Button savePDFButton;

    private String type = "csv";
    // Activity result launcher for SAF folder selection
    private ActivityResultLauncher<Intent> folderSelectionLauncher;

    private ActivityResultLauncher<Intent> folderSelectionLauncher2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerViewAnalytics = root.findViewById(R.id.recyclerViewAnalytics);
        recyclerViewAnalytics.setLayoutManager(new LinearLayoutManager(getContext()));
        saveCSVButton = root.findViewById(R.id.saveCSV);
        savePDFButton = root.findViewById(R.id.savePDF);

        // Create an adapter with an empty list of transactions
        this.analytics = new Analytics();
        getAnalytics();
        analyticsAdapter = new AnalyticsAdapter(analytics);
        recyclerViewAnalytics.setAdapter(analyticsAdapter);

        // Initialize activity result launcher for SAF folder selection
        folderSelectionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri uri = data.getData();
                            saveAnalytics(uri);
                        }
                    }
                });
        saveCSVButton.setOnClickListener(v -> selectFolder("csv")); // Call method to select folder on button click
        savePDFButton.setOnClickListener(v -> selectFolder("pdf")); // Call method to select folder on button click

        return root;
    }

    void getAnalytics() {
        analytics.getAllAnalytics();
    }

    private void selectFolder(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        this.type = type;
        folderSelectionLauncher.launch(intent);
    }

    private void saveAnalytics(Uri folderUri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                DocumentFile folder = DocumentFile.fromTreeUri(getContext(), folderUri);
                DocumentFile file = folder.createFile("text/" + (type.equals("csv") ? "csv" : "pdf"), "analytics." + type);
                OutputStream outputStream = getContext().getContentResolver().openOutputStream(file.getUri());

                if (type.equals("csv")) {
                    analytics.saveAllAnalyticsToCSV(outputStream);
                } else if (type.equals("pdf")) {
                    analytics.saveAllAnalyticsToPDF(outputStream);
                }

                getActivity().runOnUiThread(() -> writeToast("Analytics saved to " + type.toUpperCase() + " successfully"));
            } catch (IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> writeToast("Failed to save analytics to " + type.toUpperCase()));
            }
            executor.shutdown();
        });
    }

    private void writeToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
