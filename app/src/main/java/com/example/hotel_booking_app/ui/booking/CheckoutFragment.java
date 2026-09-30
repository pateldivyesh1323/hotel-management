package com.example.hotel_booking_app.ui.booking;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.BookingOutcome;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.PriceBreakdown;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.StayRequest;
import com.example.hotel_booking_app.databinding.FragmentCheckoutBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;
import com.example.hotel_booking_app.ui.TextWatchers;
import com.example.hotel_booking_app.ui.components.PriceCards;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

public class CheckoutFragment extends Fragment implements HasTitle {

    private static final String ARG_OFFER_ID = "offer_id";

    private FragmentCheckoutBinding binding;
    private Navigator nav;
    private RoomOffer offer;
    private String appliedPromo = "";
    private boolean payAtHotel = false;

    public static CheckoutFragment newInstance(String offerId) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OFFER_ID, offerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence title(Context context) {
        return "Confirm booking";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        String offerId = requireArguments().getString(ARG_OFFER_ID);
        offer = nav.repository().offer(offerId);
        Hotel hotel = offer != null ? nav.repository().hotel(offer.hotelId) : null;
        if (offer == null || hotel == null) {
            binding.summaryName.setText("This room is no longer offered");
            binding.confirmBooking.setVisibility(View.GONE);
            return;
        }

        StayRequest stay = nav.stay();
        binding.summaryImage.setImageResource(Photos.hotelPhoto(hotel.photoIndex));
        binding.summaryName.setText(hotel.name);
        binding.summaryRoom.setText(offer.type.label + " room · " + offer.beds);
        binding.summaryStay.setText(Format.stayRange(stay.checkIn, stay.checkOut)
                + " · " + Format.nightsLabel(stay.getNights())
                + " · " + Format.guestsLabel(stay.guests));

        binding.guestName.setText(nav.repository().getProfile().name);
        binding.guestEmail.setText(nav.repository().getProfile().email);
        binding.guestPhone.setText(nav.repository().getProfile().phone);

        binding.guestName.addTextChangedListener(TextWatchers.onChanged(text -> hideError()));
        binding.guestEmail.addTextChangedListener(TextWatchers.onChanged(text -> hideError()));

        binding.breakfastHint.setText(Format.money(BookingRules.BREAKFAST_RATE) + " per guest per night");
        binding.breakfastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> renderPrice());

        binding.promoField.addTextChangedListener(TextWatchers.onChanged(text -> hidePromoNote()));
        binding.promoApply.setOnClickListener(v -> applyPromo());

        buildPaymentChips();
        renderPrice();

        binding.confirmBooking.setOnClickListener(v -> confirm(stay));
    }

    private void buildPaymentChips() {
        addPaymentChip("Pay now by card", false, !payAtHotel);
        addPaymentChip("Pay at the hotel", true, payAtHotel);
        binding.paymentChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                payAtHotel = (Boolean) chip.getTag();
            }
        });
    }

    private void addPaymentChip(String text, boolean tag, boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(binding.paymentChips.getContext())
                .inflate(R.layout.item_filter_chip, binding.paymentChips, false);
        chip.setText(text);
        chip.setTag(tag);
        chip.setChecked(checked);
        binding.paymentChips.addView(chip);
    }

    private void applyPromo() {
        Editable input = binding.promoField.getText();
        String code = input == null ? "" : input.toString();
        Integer percent = BookingRules.promoPercent(code);
        binding.promoNote.setVisibility(View.VISIBLE);
        if (percent == null) {
            appliedPromo = "";
            binding.promoNote.setText("That code isn't valid");
            binding.promoNote.setTextColor(MaterialColors.getColor(binding.promoNote,
                    com.google.android.material.R.attr.colorError));
        } else {
            appliedPromo = code;
            binding.promoNote.setText(percent + "% off applied");
            binding.promoNote.setTextColor(MaterialColors.getColor(binding.promoNote,
                    com.google.android.material.R.attr.colorPrimary));
        }
        renderPrice();
    }

    private void renderPrice() {
        StayRequest stay = nav.stay();
        Integer promoPercent = BookingRules.promoPercent(appliedPromo);
        PriceBreakdown price = BookingRules.price(offer.nightlyRate, stay.getNights(), stay.guests,
                binding.breakfastSwitch.isChecked(), promoPercent == null ? 0 : promoPercent);
        PriceCards.bind(binding.priceCard.getRoot(), price);
        binding.confirmBooking.setText("Confirm booking · " + Format.money(price.total));
    }

    private void confirm(StayRequest stay) {
        BookingOutcome outcome = nav.repository().book(
                offer.id,
                textOf(binding.guestName),
                textOf(binding.guestPhone),
                textOf(binding.guestEmail),
                stay,
                textOf(binding.guestRequests),
                binding.breakfastSwitch.isChecked(),
                appliedPromo,
                payAtHotel);
        if (outcome.confirmed) {
            nav.openTripAfterBooking(outcome.bookingId);
        } else {
            binding.checkoutError.setText(outcome.message);
            binding.checkoutError.setVisibility(View.VISIBLE);
        }
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText field) {
        Editable text = field.getText();
        return text == null ? "" : text.toString();
    }

    private void hideError() {
        binding.checkoutError.setVisibility(View.GONE);
    }

    private void hidePromoNote() {
        binding.promoNote.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
