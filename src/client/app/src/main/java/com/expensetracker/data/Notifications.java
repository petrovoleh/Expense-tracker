package com.expensetracker.data;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Notifications {
    private final String[] monthNames = new DateFormatSymbols().getMonths();


    public List<String> getNotifications() {
        List<String> notifications = new ArrayList<>();

        addNotification(notifications, createBudgetsNotification());
        addNotification(notifications, savingsGoalNotification());
        addNotification(notifications, exceededBudgetsNotification());

        return notifications;
    }

    private void addNotification(List<String> notifications, String notification) {
        if (notification != null) {
            notifications.add(notification);
        }
    }

    private String createBudgetsNotification() {
        Calendar calendar = Calendar.getInstance();
        int daysLeft = calculateDaysLeft(calendar);
        if (daysLeft < 5)
            return "Left " + daysLeft + " day(s) before the end of the month, select new budgets for the next month";
        return null;
    }

    private String savingsGoalNotification() {
        Calendar calendar = Calendar.getInstance();
        int daysLeft = calculateDaysLeft(calendar);

        Analytics analytics = new Analytics();
        String[] categories = Categories.getCategoriesNames();
        int sum = calculateSumAvailable(analytics, categories);
        String monthString = getMonthString(calendar);

        if (daysLeft <= 1) {
            if (sum > 0)
                return "In " + monthString + " you saved " + Currencies.currency.format(sum);
            else if (sum < 0)
                return "In " + monthString + " you spent " + Currencies.currency.format(sum) + " more than allocated.";
        }
        return null;
    }

    private String exceededBudgetsNotification() {
        Calendar calendar = Calendar.getInstance();
        int daysLeft = calculateDaysLeft(calendar);

        Analytics analytics = new Analytics();
        String[] categories = Categories.getCategoriesNames();
        int sumAllocated = 0;
        int sumAvailable = 0;
        for (String category : categories) {
            sumAllocated += analytics.getAllocated(category);
            sumAvailable += analytics.getAvailable(category);
        }

        if (daysLeft > 1 && (sumAvailable < sumAllocated / 5 && sumAvailable > 0))
            return "You almost used all money allocated for this month: " + Currencies.currency.format(sumAvailable) + " from " + Currencies.currency.format(sumAllocated) + " try to reduce your expenses.";
        if (daysLeft > 1 && sumAvailable < 0)
            return "You you spent "+ Currencies.currency.format(-1*sumAvailable) + " more then allocated for this month: try to reduce your expenses.";

        return null;
    }

    private int calculateDaysLeft(Calendar calendar) {
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return totalDaysInMonth - currentDayOfMonth;
    }

    private int calculateSumAvailable(Analytics analytics, String[] categories) {
        int sum = 0;
        for (String category : categories) {
            sum += analytics.getAvailable(category);
        }
        return sum;
    }

    private String getMonthString(Calendar calendar) {
        int monthNumber = calendar.get(Calendar.MONTH);
        String monthString = "";
        if (monthNumber < monthNames.length) {
            monthString = monthNames[monthNumber];
        }
        return monthString;
    }
}
