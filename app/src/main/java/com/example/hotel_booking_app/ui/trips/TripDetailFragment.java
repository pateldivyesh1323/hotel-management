package com.example.hotel_booking_app.ui.trips;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Booking;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.Review;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.databinding.FragmentTripDetailBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;
import com.example.hotel_booking_app.ui.StatusTones;
import com.example.hotel_booking_app.ui.Views;
import com.example.hotel_booking_app.ui.components.PriceCards;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class TripDetailFragment extends Fragment implements HasTitle {

    private static final String ARG_BOOKING_ID = "booking_id";

    private FragmentTripDetailBinding binding;
    private Navigator nav;
    private String bookingId;

    public static TripDetailFragment newInstance(String bookingId) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOKING_ID, bookingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence title(Context context) {
        return "Booking";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTripDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        bookingId = requireArguments().getString(ARG_BOOKING_ID);
        render();
    }

    private void render() {
        Booking booking = nav.repository().booking(bookingId);
        Hotel hotel = booking != null ? nav.repository().hotel(booking.hotelId) : null;
        RoomOffer offer = booking != null ? nav.repository().offer(booking.roomId) : null;
        if (booking == null || hotel == null || offer == null) {
            binding.tripName.setText("Booking not found");
            return;
        }
        TripStage stage = nav.repository().stage(booking);

        binding.tripImage.setImageResource(Photos.hotelPhoto(hotel.photoIndex));
        binding.tripName.setText(hotel.name);
        binding.tripAddress.setText(hotel.address + ", " + hotel.getLocation());

        StatusTones.Tone tone = StatusTones.forStage(requireContext(), stage);
        binding.tripStage.setText(stage.label);
        binding.tripStage.getBackground().mutate().setColorFilter(tone.container, PorterDuff.Mode.SRC_IN);
        binding.tripStage.setTextColor(tone.content);

        bindStayRows(booking, offer);
        bindGuestRows(booking);

        PriceCards.bind(binding.priceCard.getRoot(), BookingRules.price(booking));

        bindReviewSection(booking, stage);

        binding.viewHotel.setOnClickListener(v -> nav.openHotel(hotel.id));

        if (nav.repository().canCancel(booking)) {
            binding.cancelBooking.setVisibility(View.VISIBLE);
            binding.cancelBooking.setOnClickListener(v -> confirmCancel(hotel, booking));
        } else {
            binding.cancelBooking.setVisibility(View.GONE);
        }
    }

    private void bindStayRows(Booking booking, RoomOffer offer) {
        binding.stayRows.removeAllViews();
        Views.addDetailRow(binding.stayRows, "Confirmation", booking.id.toUpperCase());
        Views.addDetailRow(binding.stayRows, "Check in", Format.fullDate(booking.checkIn));
        Views.addDetailRow(binding.stayRows, "Check out", Format.fullDate(booking.checkOut));
        Views.addDetailRow(binding.stayRows, "Length", Format.nightsLabel(booking.getNights()));
        Views.addDetailRow(binding.stayRows, "Room", offer.type.label + " · " + offer.beds);
        Views.addDetailRow(binding.stayRows, "Guests", Format.guestsLabel(booking.guests));
        Views.addDetailRow(binding.stayRows, "Booked on", Format.fullDate(booking.bookedOn));
        Views.addDetailRow(binding.stayRows, "Breakfast",
                booking.breakfastIncluded ? "Included" : "Not included");
        Views.addDetailRow(binding.stayRows, "Payment",
                booking.payAtHotel ? "Pay at the hotel" : "Paid by card");
    }

    private void bindGuestRows(Booking booking) {
        binding.guestRows.removeAllViews();
        Views.addDetailRow(binding.guestRows, "Name", booking.guestName);
        Views.addDetailRow(binding.guestRows, "Email", booking.guestEmail);
        if (!booking.guestPhone.trim().isEmpty()) {
            Views.addDetailRow(binding.guestRows, "Phone", booking.guestPhone);
        }
        if (!booking.specialRequests.trim().isEmpty()) {
            Views.addDetailRow(binding.guestRows, "Requests", booking.specialRequests);
        }
    }

    private void bindReviewSection(Booking booking, TripStage stage) {
        if (stage != TripStage.PAST) {
            binding.reviewCard.setVisibility(View.GONE);
            return;
        }
        binding.reviewCard.setVisibility(View.VISIBLE);
        binding.reviewBody.removeAllViews();
        Review existing = nav.repository().reviewFor(booking.id);
        LayoutInflater inflater = getLayoutInflater();
        if (existing != null) {
            binding.reviewTitle.setText("Your review");
            View body = inflater.inflate(R.layout.view_review_existing, binding.reviewBody, false);
            ((TextView) body.findViewById(R.id.review_existing_rating))
                    .setText(Format.ratingLabel(existing.rating));
            ((TextView) body.findViewById(R.id.review_existing_comment)).setText(existing.comment);
            binding.reviewBody.addView(body);
        } else {
            binding.reviewTitle.setText("Rate your stay");
            View body = inflater.inflate(R.layout.view_review_form, binding.reviewBody, false);
            ChipGroup ratingChips = body.findViewById(R.id.review_rating_chips);
            for (int value = 1; value <= 5; value++) {
                Chip chip = (Chip) inflater.inflate(R.layout.item_filter_chip, ratingChips, false);
                chip.setText(value + " ★");
                chip.setTag(value);
                chip.setChecked(value == 5);
                ratingChips.addView(chip);
            }
            TextInputEditText comment = body.findViewById(R.id.review_comment);
            body.findViewById(R.id.submit_review).setOnClickListener(v -> {
                int rating = selectedRating(ratingChips);
                Editable text = comment.getText();
                String result = nav.repository().addReview(booking.id, rating,
                        text == null ? "" : text.toString());
                nav.showMessage(result != null ? result : "Thanks for your review");
                render();
            });
            binding.reviewBody.addView(body);
        }
    }

    private int selectedRating(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == View.NO_ID) {
            return 5;
        }
        Chip chip = group.findViewById(id);
        return (Integer) chip.getTag();
    }

    private void confirmCancel(Hotel hotel, Booking booking) {
        String message = hotel.name + ", " + Format.fullDate(booking.checkIn)
                + " – " + Format.fullDate(booking.checkOut) + ". This can't be undone.";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancel this booking?")
                .setMessage(message)
                .setPositiveButton("Cancel booking", (dialog, which) -> {
                    String result = nav.repository().cancelBooking(booking.id);
                    nav.showMessage(result != null ? result : "Booking cancelled");
                    render();
                })
                .setNegativeButton("Keep booking", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
