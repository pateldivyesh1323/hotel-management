package com.example.hotel_booking_app.ui.explore;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Amenity;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.Review;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.databinding.FragmentHotelDetailBinding;
import com.example.hotel_booking_app.ui.AmenityIcons;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;
import com.example.hotel_booking_app.ui.StayPicker;
import com.example.hotel_booking_app.ui.Views;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import java.util.List;

public class HotelDetailFragment extends Fragment implements HasTitle {

    private static final String ARG_HOTEL_ID = "hotel_id";

    private FragmentHotelDetailBinding binding;
    private Navigator nav;
    private Hotel hotel;

    public static HotelDetailFragment newInstance(String hotelId) {
        HotelDetailFragment fragment = new HotelDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOTEL_ID, hotelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence title(Context context) {
        return "Hotel";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHotelDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        String hotelId = requireArguments().getString(ARG_HOTEL_ID);
        hotel = nav.repository().hotel(hotelId);
        if (hotel == null) {
            binding.detailName.setText("This hotel is no longer listed");
            return;
        }

        binding.detailImage.setImageResource(Photos.hotelPhoto(hotel.photoIndex));
        bindFavorite();
        binding.detailFavorite.setOnClickListener(v -> {
            nav.repository().toggleFavorite(hotel.id);
            bindFavorite();
        });

        binding.detailName.setText(hotel.name);
        binding.detailRating.setText(Format.ratingLabel(hotel.rating));
        binding.detailStarsAddress.setText(Format.stars(hotel.stars) + " · "
                + hotel.address + ", " + hotel.getLocation());
        binding.detailReviewCount.setText(hotel.reviewCount + " guest reviews");
        binding.detailDescription.setText(hotel.description);

        bindHighlights();
        bindAmenities();
        bindGoodToKnow();
        bindReviews();

        renderStayDependent();
    }

    private void bindFavorite() {
        Views.bindFavorite(binding.detailFavorite, nav.repository().isFavorite(hotel.id));
    }

    private void bindHighlights() {
        LayoutInflater inflater = getLayoutInflater();
        binding.highlightsContainer.removeAllViews();
        for (String line : hotel.highlights) {
            View row = inflater.inflate(R.layout.item_highlight, binding.highlightsContainer, false);
            ((TextView) row.findViewById(R.id.highlight_text)).setText(line);
            binding.highlightsContainer.addView(row);
        }
    }

    private void bindAmenities() {
        LayoutInflater inflater = getLayoutInflater();
        binding.amenitiesGroup.removeAllViews();
        for (Amenity amenity : hotel.amenities) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_assist_chip, binding.amenitiesGroup, false);
            chip.setText(amenity.label);
            chip.setChipIconResource(AmenityIcons.icon(amenity));
            binding.amenitiesGroup.addView(chip);
        }
    }

    private void bindGoodToKnow() {
        binding.goodToKnowContainer.removeAllViews();
        Views.addDetailRow(binding.goodToKnowContainer, "Check-in", "From 2:00 PM");
        Views.addDetailRow(binding.goodToKnowContainer, "Check-out", "Until 11:00 AM");
        Views.addDetailRow(binding.goodToKnowContainer, "Cancellation", "Free until the day before check-in");
        Views.addDetailRow(binding.goodToKnowContainer, "Breakfast",
                "Add for " + Format.money(BookingRules.BREAKFAST_RATE) + " per guest per night");
    }

    private void bindReviews() {
        List<Review> reviews = nav.repository().reviewsFor(hotel.id);
        binding.reviewsEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = getLayoutInflater();
        binding.reviewsContainer.removeAllViews();
        for (Review review : reviews) {
            View card = inflater.inflate(R.layout.item_review, binding.reviewsContainer, false);
            ((ImageView) card.findViewById(R.id.review_avatar))
                    .setImageResource(Photos.avatar(review.guestName));
            ((TextView) card.findViewById(R.id.review_name)).setText(review.guestName);
            ((TextView) card.findViewById(R.id.review_date)).setText(Format.fullDate(review.date));
            ((TextView) card.findViewById(R.id.review_rating)).setText(Format.ratingLabel(review.rating));
            ((TextView) card.findViewById(R.id.review_comment)).setText(review.comment);
            binding.reviewsContainer.addView(card);
        }
    }

    private void renderStayDependent() {
        StayPicker.bind(binding.stayPicker.getRoot(), getParentFragmentManager(),
                nav.stay(), nav.repository().currentDate(), updated -> {
                    nav.updateStay(updated);
                    renderStayDependent();
                });
        bindRooms();
    }

    private void bindRooms() {
        List<RoomOffer> offers = nav.repository().offersFor(hotel.id);
        offers.sort((a, b) -> Integer.compare(a.nightlyRate, b.nightlyRate));
        LayoutInflater inflater = getLayoutInflater();
        binding.roomsContainer.removeAllViews();
        for (RoomOffer offer : offers) {
            View card = inflater.inflate(R.layout.item_room_offer, binding.roomsContainer, false);
            bindRoom(card, offer);
            binding.roomsContainer.addView(card);
        }
    }

    private void bindRoom(View card, RoomOffer offer) {
        int guests = nav.stay().guests;
        int unitsLeft = nav.repository().unitsLeft(offer, nav.stay());
        boolean fits = offer.type.maxGuests >= guests;
        boolean bookable = fits && unitsLeft > 0;

        ((ImageView) card.findViewById(R.id.room_image)).setImageResource(Photos.roomPhoto(offer));
        ((TextView) card.findViewById(R.id.room_title)).setText(offer.type.label + " room");
        ((TextView) card.findViewById(R.id.room_specs)).setText(
                offer.beds + " · " + offer.sizeSqm + " m² · sleeps " + offer.type.maxGuests);
        ((TextView) card.findViewById(R.id.room_price)).setText(Format.money(offer.nightlyRate));

        TextView availability = card.findViewById(R.id.room_availability);
        String text;
        if (!fits) {
            text = "Too small for " + Format.guestsLabel(guests);
        } else if (unitsLeft == 0) {
            text = "Sold out for these dates";
        } else if (unitsLeft <= 2) {
            text = "Only " + unitsLeft + " left";
        } else {
            text = "Free cancellation before check-in";
        }
        availability.setText(text);
        boolean neutral = bookable && unitsLeft > 2;
        availability.setTextColor(MaterialColors.getColor(availability, neutral
                ? com.google.android.material.R.attr.colorOnSurfaceVariant
                : com.google.android.material.R.attr.colorError));

        MaterialButton book = card.findViewById(R.id.room_book);
        book.setEnabled(bookable);
        book.setOnClickListener(v -> nav.openCheckout(offer.id));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
