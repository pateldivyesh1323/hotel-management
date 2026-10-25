package com.example.hotel_booking_app.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.databinding.FragmentSignupBinding;
import com.google.android.material.textfield.TextInputEditText;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;
    private AuthHost host;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        host = (AuthHost) requireActivity();
        binding.signupButton.setOnClickListener(v -> attemptSignup());
        binding.goLogin.setOnClickListener(v -> host.goToLogin());
    }

    private void attemptSignup() {
        String error = host.repository().signUp(
                textOf(binding.signupName),
                textOf(binding.signupEmail),
                textOf(binding.signupPhone),
                textOf(binding.signupPassword));
        if (error != null) {
            if (error.contains("name")) {
                binding.signupName.setError(error);
            } else if (error.contains("email")) {
                binding.signupEmail.setError(error);
            } else {
                binding.signupPassword.setError(error);
            }
            return;
        }
        host.onAuthenticated();
    }

    private String textOf(TextInputEditText field) {
        Editable text = field.getText();
        return text == null ? "" : text.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
