package com.example.hotel_booking_app.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.HotelApplication;
import com.example.hotel_booking_app.MainActivity;
import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity implements AuthHost {

    private ActivityAuthBinding binding;
    private HotelRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ((HotelApplication) getApplication()).repository();
        if (repository.isLoggedIn()) {
            onAuthenticated();
            return;
        }
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        if (savedInstanceState == null) {
            goToLogin();
        }
    }

    @Override
    public HotelRepository repository() {
        return repository;
    }

    @Override
    public void goToLogin() {
        show(new LoginFragment());
    }

    @Override
    public void goToSignup() {
        show(new SignupFragment());
    }

    @Override
    public void onAuthenticated() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
