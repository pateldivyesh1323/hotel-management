package com.example.hotel_booking_app.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.databinding.FragmentLoginBinding;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthHost host;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        host = (AuthHost) requireActivity();
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.goSignup.setOnClickListener(v -> host.goToSignup());
    }

    private void attemptLogin() {
        String error = host.repository().logIn(textOf(binding.loginEmail), textOf(binding.loginPassword));
        if (error != null) {
            binding.loginPassword.setError(error);
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
