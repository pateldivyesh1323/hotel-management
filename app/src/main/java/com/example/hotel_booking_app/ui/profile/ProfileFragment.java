package com.example.hotel_booking_app.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.HotelApplication;
import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.GuestProfile;
import com.example.hotel_booking_app.data.ThemeMode;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.databinding.FragmentProfileBinding;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Navigator nav;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();

        GuestProfile profile = nav.repository().getProfile();
        binding.profileName.setText(profile.name);
        binding.profileEmail.setText(profile.email);
        binding.profilePhone.setText(profile.phone);
        renderHeader();

        buildAppearanceChips();

        binding.profileSave.setOnClickListener(v -> {
            String result = nav.repository().saveProfile(new GuestProfile(
                    textOf(binding.profileName),
                    textOf(binding.profileEmail),
                    textOf(binding.profilePhone)));
            nav.showMessage(result != null ? result : "Details saved");
            renderHeader();
        });

        binding.resetData.setOnClickListener(v -> {
            nav.repository().resetToSample();
            GuestProfile reset = nav.repository().getProfile();
            binding.profileName.setText(reset.name);
            binding.profileEmail.setText(reset.email);
            binding.profilePhone.setText(reset.phone);
            renderHeader();
            nav.showMessage("Bookings reset to the sample data");
        });
    }

    private void renderHeader() {
        GuestProfile profile = nav.repository().getProfile();
        binding.profileAvatar.setImageResource(Photos.avatar(profile.name));
        binding.profileTitle.setText(profile.name);
        int upcoming = nav.repository().trips(TripStage.UPCOMING).size();
        int past = nav.repository().trips(TripStage.PAST).size();
        binding.profileSubtitle.setText(upcoming + " upcoming · " + past + " past stays");
    }

    private void buildAppearanceChips() {
        for (ThemeMode mode : ThemeMode.values()) {
            Chip chip = (Chip) LayoutInflater.from(binding.appearanceChips.getContext())
                    .inflate(R.layout.item_filter_chip, binding.appearanceChips, false);
            chip.setText(mode.label);
            chip.setTag(mode);
            chip.setChecked(mode == nav.repository().getThemeMode());
            binding.appearanceChips.addView(chip);
        }
        binding.appearanceChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            Chip chip = group.findViewById(checkedIds.get(0));
            ThemeMode mode = (ThemeMode) chip.getTag();
            if (mode != nav.repository().getThemeMode()) {
                nav.repository().setThemeMode(mode);
                HotelApplication.applyThemeMode(mode);
            }
        });
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
