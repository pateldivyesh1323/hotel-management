package com.example.hotel_booking_app.ui.owner;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.databinding.FragmentOwnerDashboardBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;

public class OwnerDashboardFragment extends Fragment implements HasTitle {

    private FragmentOwnerDashboardBinding binding;
    private Navigator nav;

    @Override
    public CharSequence title(Context context) {
        return "Admin dashboard";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnerDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        binding.cardHotels.statLabel.setText("Total hotels");
        binding.cardUsers.statLabel.setText("Total users");
        binding.cardBookings.statLabel.setText("Total bookings");
        binding.cardPending.statLabel.setText("Pending bookings");
        binding.cardCancelled.statLabel.setText("Cancelled bookings");
        binding.cardRevenue.statLabel.setText("Revenue");

        binding.actionManageHotels.setOnClickListener(v -> nav.openManageHotels());
        binding.actionManageBookings.setOnClickListener(v -> nav.openManageBookings());
        binding.actionManageUsers.setOnClickListener(v -> nav.openManageUsers());
        binding.actionCancellations.setOnClickListener(v -> nav.openCancellations());
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        HotelRepository repository = nav.repository();
        binding.ownerName.setText("Signed in as " + repository.getProfile().name);
        binding.cardHotels.statValue.setText(String.valueOf(repository.totalHotels()));
        binding.cardUsers.statValue.setText(String.valueOf(repository.totalUsers()));
        binding.cardBookings.statValue.setText(String.valueOf(repository.totalBookings()));
        binding.cardPending.statValue.setText(String.valueOf(repository.pendingCount()));
        binding.cardCancelled.statValue.setText(String.valueOf(repository.cancelledCount()));
        binding.cardRevenue.statValue.setText(Format.money(repository.revenue()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
