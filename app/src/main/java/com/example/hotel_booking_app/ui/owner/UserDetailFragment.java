package com.example.hotel_booking_app.ui.owner;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Booking;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.databinding.FragmentUserDetailBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;
import com.example.hotel_booking_app.ui.StatusTones;

import java.util.List;

public class UserDetailFragment extends Fragment implements HasTitle {

    private static final String ARG_EMAIL = "email";

    private FragmentUserDetailBinding binding;
    private Navigator nav;

    public static UserDetailFragment newInstance(String email) {
        UserDetailFragment fragment = new UserDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence title(Context context) {
        return "Guest";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        String email = requireArguments().getString(ARG_EMAIL);
        List<Booking> history = nav.repository().bookingsForGuest(email);
        if (history.isEmpty()) {
            binding.userName.setText("No bookings for this guest");
            return;
        }

        Booking latest = history.get(0);
        binding.userAvatar.setImageResource(Photos.avatar(latest.guestName));
        binding.userName.setText(latest.guestName);
        binding.userEmail.setText(latest.guestEmail);
        binding.userPhone.setText(latest.guestPhone.trim().isEmpty() ? "No phone on file" : latest.guestPhone);

        LayoutInflater inflater = getLayoutInflater();
        binding.historyContainer.removeAllViews();
        for (Booking booking : history) {
            View card = inflater.inflate(R.layout.item_history_booking, binding.historyContainer, false);
            bindRow(card, booking);
            binding.historyContainer.addView(card);
        }
    }

    private void bindRow(View card, Booking booking) {
        Hotel hotel = nav.repository().hotel(booking.hotelId);
        ((TextView) card.findViewById(R.id.history_hotel))
                .setText(hotel != null ? hotel.name : booking.hotelId);
        ((TextView) card.findViewById(R.id.history_dates)).setText(
                Format.stayRange(booking.checkIn, booking.checkOut)
                        + " · " + Format.nightsLabel(booking.getNights()));

        TripStage stage = nav.repository().stage(booking);
        TextView status = card.findViewById(R.id.history_status);
        StatusTones.Tone tone = StatusTones.forBooking(status.getContext(), booking, stage);
        status.setText(StatusTones.label(booking, stage));
        status.getBackground().mutate().setColorFilter(tone.container, PorterDuff.Mode.SRC_IN);
        status.setTextColor(tone.content);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
