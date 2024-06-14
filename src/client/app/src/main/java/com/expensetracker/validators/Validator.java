package com.expensetracker.validators;

import android.content.Context;
import android.widget.TextView;
import com.expensetracker.R;
import java.util.regex.Pattern;

public class Validator {

    private static final int MAX_LENGTH = 254;
    //DEPRECATED
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
    //DEPRECATED
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
    //DEPRECATED
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
    public static String validatePasswordString(Context context, String password) {
        // Password validation criteria
        // Minimum length of 6 characters
        if (password.length() < 6) {
            return context.getString(R.string.error_password_length);
        }
        // Password should not exceed maximum length
        if (password.length() > MAX_LENGTH) {
            return context.getString(R.string.error_password_length);
        }
        // Requiring at least one uppercase letter, one lowercase letter, one digit, and one special character
        if (!Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$").matcher(password).matches()) {
            return context.getString(R.string.error_password_criteria);
        }
        return null;
    }
    public static String validateNameString(Context context, String name) {
        // Password validation criteria
        // Minimum length of 6 characters
        if (name.isEmpty()) {
            return context.getString(R.string.error_empty);
        }
        // Name should not exceed maximum length
        if (name.length() > MAX_LENGTH) {
            return context.getString(R.string.error_name_length);
        }
        return null;
    }
    public static String validateEmailString(Context context, String email) {
        // Simple email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return context.getString(R.string.error_email_invalid);
        }
        // Email should not exceed maximum length
        if (email.length() > MAX_LENGTH) {
            return context.getString(R.string.error_email_length);
        }
        return null;
    }
    public static boolean validateTwoPasswords(Context context, String password, String password2, TextView text) {
        if (!password.equals(password2)) {
            text.setText("Passwords should be the same");
            return true;
        }
        return false;
    }
}
