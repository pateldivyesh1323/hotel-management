package com.example.hotel_booking_app.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.databinding.FragmentSavedBinding;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.components.HotelCardAdapter;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment implements HotelCardAdapter.Listener {

    private FragmentSavedBinding binding;
    private Navigator nav;
    private HotelCardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new HotelCardAdapter(this);
        binding.savedList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.savedList.setAdapter(adapter);
        refresh();
    }

    private void refresh() {
        List<Hotel> saved = nav.repository().favoriteHotels();
        binding.emptyHint.setVisibility(saved.isEmpty() ? View.VISIBLE : View.GONE);

        List<HotelCardAdapter.Entry> entries = new ArrayList<>();
        for (Hotel hotel : saved) {
            entries.add(new HotelCardAdapter.Entry(hotel, cheapestRate(hotel.id)));
        }
        adapter.setItems(entries);
    }

    @Nullable
    private Integer cheapestRate(String hotelId) {
        List<RoomOffer> open = nav.repository().availableOffers(hotelId, nav.stay());
        Integer cheapest = null;
        for (RoomOffer offer : open) {
            if (cheapest == null || offer.nightlyRate < cheapest) {
                cheapest = offer.nightlyRate;
            }
        }
        return cheapest;
    }

    @Override
    public boolean isFavorite(String hotelId) {
        return nav.repository().isFavorite(hotelId);
    }

    @Override
    public void onToggleFavorite(String hotelId) {
        nav.repository().toggleFavorite(hotelId);
        refresh();
    }

    @Override
    public void onOpen(String hotelId) {
        nav.openHotel(hotelId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
