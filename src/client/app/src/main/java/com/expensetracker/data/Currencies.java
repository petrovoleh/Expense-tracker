package com.expensetracker.data;

import android.util.Log;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class Currencies {
    public static NumberFormat currency = createFormat();
    private final static String[] currenciesNames = {
            "EUR", // Euro
            "UAH", // Ukrainian Hryvnia
            "USD", // United States Dollar
            "PLN", // Polish Zloty
            "AUD", // Australian Dollar
            "CAD", // Canadian Dollar
            "GBP", // British Pound Sterling
            "JPY", // Japanese Yen
            "CNY", // Chinese Yuan
            "INR", // Indian Rupee
            "SGD", // Singapore Dollar
            "CHF", // Swiss Franc
            "NZD", // New Zealand Dollar
            "HKD"  // Hong Kong Dollar
    };
    private static NumberFormat createFormat(){
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("lt", "LT"));
        format.setMaximumFractionDigits(0);
        format.setCurrency(Currency.getInstance("EUR"));
        return format;
    }
    public static void setCurrency(String currencyName) {
        Locale locale;
        switch (currencyName) {
            case "UAH":
                locale = new Locale("uk", "UA");
                break;
            case "PLN":
                locale = new Locale("pl", "PL");
                break;
            case "USD":
            case "CAD":
            case "AUD":
            case "NZD":
            case "SGD":
                locale = Locale.US;
                break;
            case "EUR":
            case "CHF":
                locale = Locale.GERMANY;
                break;
            case "GBP":
                locale = Locale.UK;
                break;
            case "JPY":
                locale = Locale.JAPAN;
                break;
            case "CNY":
                locale = Locale.CHINA;
                break;
            case "INR":
                locale = new Locale("hi", "IN");
                break;
            case "HKD":
                locale = new Locale("zh", "HK");
                break;
            default:
                locale = Locale.getDefault(); // Fallback to default locale
                break;
        }
        currency = NumberFormat.getCurrencyInstance(locale);
        currency.setCurrency(Currency.getInstance(currencyName));
        currency.setMaximumFractionDigits(0);
        Log.d("currency", "changed currency "+currency.format(10000));
    }
    public static String[] getCurrenciesNames(){
        return currenciesNames;
    }
}
