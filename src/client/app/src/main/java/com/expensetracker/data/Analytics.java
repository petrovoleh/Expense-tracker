package com.expensetracker.data;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.expensetracker.MainActivity;
import com.expensetracker.database.AppDatabase;
import com.expensetracker.database.TransactionDao;
import com.expensetracker.models.Category;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Analytics {
    private final NumberFormat currency = Currencies.currency;
    private String text;
    private final TransactionDao transactionDao;
    private final String[] names = Categories.getCategoriesNames();

    public Analytics() {
        text = new String();
        AppDatabase database = MainActivity.getDatabase();
        transactionDao = database.transactionDao();
    }

    public String getAllAnalytics() {
        StringBuilder builder = new StringBuilder();
        for (String name : names) {

            getAnalytics(name, result -> builder.append(result).append("\n"));

        }
        String text = builder.toString();
        return text;
    }

    public int getAllocated(String categoryName) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        return category.getBudget();
    }

    public int getAvailable(String categoryName) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        int all = category.getBudget();

        // Observe the LiveData for total value
        Double spent = transactionDao.getTotalValueForCategory(categoryName);
        if (spent == null) {
            spent = 0.0;
        }
        return (int) (all - spent);
    }

    public void getAnalytics(String categoryName, AnalyticsCallback callback) {
        Category category = Categories.getCategory(categoryName);
        assert category != null;
        int all = category.getBudget();

        // Create a single-threaded ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Fetch total value from the database
            Double spent = transactionDao.getTotalValueForCategory(categoryName);
            if (spent == null) {
                spent = 0.0;
            }

            // Calculate available budget
            int available = (int) (all - spent);

            // Prepare result
            String result = categoryName + " " + currency.format(all) + " " + currency.format(spent.intValue()) + " " + currency.format(available);

            // Deliver the result on the main thread
            callback.onAnalyticsResult(result);

            // Shutdown the executor
            executor.shutdown();
        });
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public interface AnalyticsCallback {
        void onAnalyticsResult(String result);
    }

    public String[] getNames() {
        return names;
    }

    public int size() {
        return names.length;
    }

    // Method to save all analytics to a CSV file
    public void saveAllAnalyticsToCSV(OutputStream outputStream) throws IOException {

        if (outputStream != null) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            // Write header
            writer.write("Category,Budget,Spent,Available\n");

            for (String name : names) {
                Category category = Categories.getCategory(name);
                assert category != null;
                int budget = category.getBudget();
                Double spent = transactionDao.getTotalValueForCategory(name);
                if (spent == null) {
                    spent = 0.0;
                }
                int available = (int) (budget - spent);
                // Write data
                writer.write(name + "," + currency.format(budget) + "," + currency.format(spent.intValue()) + "," + currency.format(available) + "\n");
            }

            writer.flush();
            writer.close();
            outputStream.close();
        } else {
            throw new IOException("Failed to open output stream");
        }
    }

    public void saveAllAnalyticsToPDF(OutputStream outputStream) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream));
        Document document = new Document(pdfDoc);

        try {
            // Add title
            document.add(new Paragraph("Expense Tracker Analytics"));

            // Add header
            document.add(new Paragraph("Category   Budget   Spent   Available"));

            for (String name : names) {
                Category category = Categories.getCategory(name);
                assert category != null;
                int budget = category.getBudget();
                Double spent = transactionDao.getTotalValueForCategory(name);
                if (spent == null) {
                    spent = 0.0;
                }
                int available = (int) (budget - spent);
                // Add data
                document.add(new Paragraph(name + "   " + currency.format(budget) + "   " + currency.format(spent.intValue()) + "   " + currency.format(available)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}