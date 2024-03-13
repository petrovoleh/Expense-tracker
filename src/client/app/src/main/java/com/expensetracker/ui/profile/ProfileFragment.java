package com.expensetracker.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.expensetracker.R;
import com.expensetracker.data.FileManager;
import com.expensetracker.databinding.FragmentProfileBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Context context;
    private FileManager fileManager;

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

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Проверяем наличие данных в файле при открытии страницы
        if (!checkAccountData()) {
            // Если данных нет, перенаправляем пользователя на страницу входа
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_signin);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Метод для проверки наличия данных в файле
    private boolean checkAccountData() {
        try {
            JSONObject accountData = fileManager.readFromFile("accountdata.json");
            // Проверяем, содержит ли объект accountData необходимые данные
            return accountData != null && accountData.has("username") && !accountData.getString("username").isEmpty();
        } catch (JSONException e) {
            fileManager.writeToFile("accountdata.json", new JSONObject());
            e.printStackTrace();
        }
        // В случае ошибки или отсутствия данных возвращаем false
        return false;
    }
}
