package com.example.hotel_booking_app.ui.explore;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.databinding.FragmentHomeBinding;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.StayPicker;
import com.example.hotel_booking_app.ui.TextWatchers;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Navigator nav = (Navigator) requireActivity();

        binding.destinationField.setText(nav.query());
        binding.destinationField.addTextChangedListener(TextWatchers.onChanged(text -> nav.updateQuery(text)));

        bindStay(nav);

        binding.searchButton.setOnClickListener(v -> {
            Editable text = binding.destinationField.getText();
            nav.updateQuery(text == null ? "" : text.toString());
            nav.openResults();
        });
    }

    private void bindStay(Navigator nav) {
        StayPicker.bind(binding.stayPicker.getRoot(), getParentFragmentManager(),
                nav.stay(), nav.repository().currentDate(), updated -> {
                    nav.updateStay(updated);
                    bindStay(nav);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
