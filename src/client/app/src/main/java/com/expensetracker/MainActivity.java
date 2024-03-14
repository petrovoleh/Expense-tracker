package com.expensetracker;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.expensetracker.databinding.ActivityMainBinding;

import io.github.cdimascio.dotenv.Dotenv;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public static String baseUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dotenv dotenv = Dotenv.configure()
                .directory("./assets")
                .filename("env") // instead of '.env', use 'env'
                .load();
        baseUrl = dotenv.get("BASE_URL");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_records, R.id.navigation_profile, R.id.navigation_analytics, R.id.navigation_budgets, R.id.navigation_signin, R.id.navigation_signup)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}