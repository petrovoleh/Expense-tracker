package com.expensetracker.validators;

import android.content.Context;
import android.widget.TextView;
import com.expensetracker.R;
import java.util.regex.Pattern;

public class Validator {

    private static final int MAX_LENGTH = 254;

    public static boolean validateName(Context context, String name, TextView text) {
        // Name should not be empty
        if (name.isEmpty()) {
            text.setText(context.getString(R.string.error_empty));
            return true;
        }
        // Name should not exceed maximum length
        if (name.length() > MAX_LENGTH) {
            text.setText(context.getString(R.string.error_name_length));
            return true;
        }
        return false;
    }

    public static boolean validateEmail(Context context, String email, TextView text) {
        // Simple email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            text.setText(context.getString(R.string.error_email_invalid));
            return false;
        }
        // Email should not exceed maximum length
        if (email.length() > MAX_LENGTH) {
            text.setText(context.getString(R.string.error_email_length));
            return false;
        }
        return true;
    }

    public static boolean validatePassword(Context context, String password, TextView text) {
        // Password validation criteria
        // Minimum length of 6 characters
        if (password.length() < 6) {
            text.setText(context.getString(R.string.error_password_length));
            return false;
        }
        // Password should not exceed maximum length
        if (password.length() > MAX_LENGTH) {
            text.setText(context.getString(R.string.error_password_length));
            return false;
        }
        // Requiring at least one uppercase letter, one lowercase letter, one digit, and one special character
        if (!Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$").matcher(password).matches()) {
            text.setText(context.getString(R.string.error_password_criteria));
            return false;
        }
        return true;
    }
}
