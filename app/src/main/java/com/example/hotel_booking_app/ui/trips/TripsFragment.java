package com.example.hotel_booking_app.ui.trips;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.databinding.FragmentTripsBinding;
import com.example.hotel_booking_app.ui.Navigator;
import com.google.android.material.chip.Chip;

public class TripsFragment extends Fragment implements TripAdapter.Listener {

    private FragmentTripsBinding binding;
    private Navigator nav;
    private TripAdapter adapter;
    private TripStage stage = TripStage.UPCOMING;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTripsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new TripAdapter(nav.repository(), this);
        binding.tripsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.tripsList.setAdapter(adapter);

        buildStageChips();
        refresh();
    }

    private void buildStageChips() {
        for (TripStage entry : TripStage.values()) {
            Chip chip = (Chip) LayoutInflater.from(binding.stageChips.getContext())
                    .inflate(R.layout.item_filter_chip, binding.stageChips, false);
            chip.setText(entry.label + " (" + nav.repository().trips(entry).size() + ")");
            chip.setTag(entry);
            chip.setChecked(entry == stage);
            binding.stageChips.addView(chip);
        }
        binding.stageChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                stage = (TripStage) chip.getTag();
                refresh();
            }
        });
    }

    private void refresh() {
        adapter.setItems(nav.repository().trips(stage));
        boolean empty = nav.repository().trips(stage).isEmpty();
        binding.emptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.emptyHint.setText(emptyMessage());
    }

    private String emptyMessage() {
        switch (stage) {
            case PAST:
                return "Your completed stays will show up here.";
            case CANCELLED:
                return "You haven't cancelled any bookings.";
            case UPCOMING:
            default:
                return "No upcoming trips. Find a hotel in the Explore tab.";
        }
    }

    @Override
    public void onOpen(String bookingId) {
        nav.openTrip(bookingId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
