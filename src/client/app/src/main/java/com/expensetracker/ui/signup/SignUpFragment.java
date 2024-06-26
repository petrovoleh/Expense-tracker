package com.expensetracker.ui.signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.expensetracker.R;
import com.expensetracker.databinding.FragmentSignupBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SignUpFragment extends Fragment {

    private FragmentSignupBinding binding;
    private SignUpViewModel signUpViewModel;
    NavController navController;
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().findViewById(android.R.id.content).post(() -> {
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
            Menu menu = bottomNavigationView.getMenu();
            for (int i = 0, size = menu.size(); i < size; i++) {
                MenuItem item = menu.getItem(i);
                item.setChecked(item.getItemId() == R.id.navigation_profile);
            }
        });
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        final TextView errorText = binding.errorText;
        final EditText usernameEditText = binding.usernameEditText;
        final EditText emailEditText = binding.emailEditText;
        final EditText passwordEditText = binding.passwordEditText;
        Button signUpButton = binding.signUpButton;
        Button signInButton = binding.signInButton;

        signUpViewModel.getErrorText().observe(getViewLifecycleOwner(), errorText::setText);

        signInButton.setOnClickListener(v -> navigateToSignIn());

        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            signUpViewModel.signUp(getContext(), username, email, password, navController);
        });

        return root;
    }

    private void navigateToSignIn() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_signin);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
