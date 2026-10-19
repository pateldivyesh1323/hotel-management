package com.example.hotel_booking_app.ui.owner;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
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
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.SampleData;
import com.example.hotel_booking_app.databinding.FragmentHotelEditorBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HotelEditorFragment extends Fragment implements HasTitle {

    private static final String ARG_HOTEL_ID = "hotel_id";
    private static final int MIN_STARS = 1;
    private static final int MAX_STARS = 5;
    private static final int MAX_UNITS = SampleData.ROOM_UNITS;

    private FragmentHotelEditorBinding binding;
    private Navigator nav;
    @Nullable
    private Hotel hotel;
    private int stars = 4;
    private int photoIndex = 0;

    public static HotelEditorFragment newInstance(@Nullable String hotelId) {
        HotelEditorFragment fragment = new HotelEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOTEL_ID, hotelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence title(Context context) {
        return getArguments() != null && getArguments().getString(ARG_HOTEL_ID) != null
                ? "Edit hotel" : "Add hotel";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHotelEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        String hotelId = requireArguments().getString(ARG_HOTEL_ID);
        hotel = hotelId != null ? nav.repository().hotel(hotelId) : null;

        buildPhotoRow();
        buildAmenityChips();
        binding.starsMinus.setOnClickListener(v -> changeStars(-1));
        binding.starsPlus.setOnClickListener(v -> changeStars(1));

        if (hotel != null) {
            bindExisting(hotel);
        }
        renderStars();

        binding.saveHotel.setOnClickListener(v -> save());
    }

    private void bindExisting(Hotel hotel) {
        binding.hotelName.setText(hotel.name);
        binding.hotelAddress.setText(hotel.address);
        binding.hotelCity.setText(hotel.city);
        binding.hotelCountry.setText(hotel.country);
        binding.hotelDescription.setText(hotel.description);
        binding.hotelPriceLayout.setVisibility(View.GONE);
        stars = hotel.stars;
        photoIndex = hotel.photoIndex;
        renderPhotoSelection();
        for (int i = 0; i < binding.amenitiesChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.amenitiesChips.getChildAt(i);
            chip.setChecked(hotel.amenities.contains((Amenity) chip.getTag()));
        }
        binding.saveHotel.setText("Save changes");

        binding.deleteHotel.setVisibility(View.VISIBLE);
        binding.deleteHotel.setOnClickListener(v -> confirmDelete(hotel));
        binding.roomsHeader.setVisibility(View.VISIBLE);
        bindRooms(hotel);
    }

    private void bindRooms(Hotel hotel) {
        LayoutInflater inflater = getLayoutInflater();
        binding.roomsContainer.removeAllViews();
        for (RoomOffer offer : nav.repository().offersFor(hotel.id)) {
            View card = inflater.inflate(R.layout.item_manage_room, binding.roomsContainer, false);
            bindRoom(card, offer);
            binding.roomsContainer.addView(card);
        }
    }

    private void bindRoom(View card, RoomOffer offer) {
        ((TextView) card.findViewById(R.id.room_type_title)).setText(offer.type.label + " room");
        TextInputEditText priceInput = card.findViewById(R.id.room_price_input);
        priceInput.setText(String.valueOf(offer.nightlyRate));

        TextView unitsCount = card.findViewById(R.id.room_units_count);
        View minus = card.findViewById(R.id.room_units_minus);
        View plus = card.findViewById(R.id.room_units_plus);
        setUnits(unitsCount, minus, plus, offer.units);
        minus.setOnClickListener(v -> setUnits(unitsCount, minus, plus, readUnits(unitsCount) - 1));
        plus.setOnClickListener(v -> setUnits(unitsCount, minus, plus, readUnits(unitsCount) + 1));

        card.findViewById(R.id.room_save).setOnClickListener(v -> {
            int price = parseInt(textOf(priceInput));
            if (price <= 0) {
                nav.showMessage("Enter a valid nightly price");
                return;
            }
            String error = nav.repository().updateOffer(offer.id, price, readUnits(unitsCount));
            nav.showMessage(error != null ? error : offer.type.label + " room updated");
        });
    }

    private void setUnits(TextView count, View minus, View plus, int value) {
        int clamped = Math.max(0, Math.min(MAX_UNITS, value));
        count.setText(String.valueOf(clamped));
        minus.setEnabled(clamped > 0);
        plus.setEnabled(clamped < MAX_UNITS);
    }

    private int readUnits(TextView count) {
        return Integer.parseInt(count.getText().toString());
    }

    private void changeStars(int delta) {
        int updated = stars + delta;
        if (updated < MIN_STARS || updated > MAX_STARS) {
            return;
        }
        stars = updated;
        renderStars();
    }

    private void renderStars() {
        binding.starsCount.setText(stars + " ★");
        binding.starsMinus.setEnabled(stars > MIN_STARS);
        binding.starsPlus.setEnabled(stars < MAX_STARS);
    }

    private void buildPhotoRow() {
        LayoutInflater inflater = getLayoutInflater();
        for (int index = 0; index < Photos.hotelPhotoCount(); index++) {
            final int value = index;
            MaterialCardView card = (MaterialCardView) inflater
                    .inflate(R.layout.item_photo_option, binding.photoRow, false);
            ((ImageView) card.findViewById(R.id.photo_option_image))
                    .setImageResource(Photos.hotelPhoto(index));
            card.setOnClickListener(v -> {
                photoIndex = value;
                renderPhotoSelection();
            });
            binding.photoRow.addView(card);
        }
        renderPhotoSelection();
    }

    private void renderPhotoSelection() {
        int selected = MaterialColors.getColor(binding.photoRow,
                com.google.android.material.R.attr.colorPrimary);
        int unselected = MaterialColors.getColor(binding.photoRow,
                com.google.android.material.R.attr.colorOutline);
        for (int i = 0; i < binding.photoRow.getChildCount(); i++) {
            MaterialCardView card = (MaterialCardView) binding.photoRow.getChildAt(i);
            card.setStrokeColor(i == photoIndex ? selected : unselected);
            card.setStrokeWidth(i == photoIndex ? dp(2) : dp(1));
        }
    }

    private void buildAmenityChips() {
        LayoutInflater inflater = getLayoutInflater();
        for (Amenity amenity : Amenity.values()) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_filter_chip, binding.amenitiesChips, false);
            chip.setText(amenity.label);
            chip.setTag(amenity);
            binding.amenitiesChips.addView(chip);
        }
    }

    private void save() {
        String name = textOf(binding.hotelName);
        String city = textOf(binding.hotelCity);
        String country = textOf(binding.hotelCountry);
        String address = textOf(binding.hotelAddress);
        String description = textOf(binding.hotelDescription);

        if (name.isEmpty() || city.isEmpty() || country.isEmpty() || address.isEmpty()) {
            showError("Fill in the hotel name, address, city and country");
            return;
        }
        if (description.isEmpty()) {
            showError("Add a short description");
            return;
        }

        if (hotel != null) {
            nav.repository().updateHotel(hotel.id, name, city, country, address, description, stars,
                    selectedAmenities(), photoIndex);
            nav.showMessage(name + " updated");
        } else {
            int baseRate = parseInt(textOf(binding.hotelPrice));
            if (baseRate <= 0) {
                showError("Enter a base nightly price");
                return;
            }
            nav.repository().addHotel(name, city, country, address, description, stars,
                    selectedAmenities(), photoIndex, baseRate);
            nav.showMessage(name + " added");
        }
        getParentFragmentManager().popBackStack();
    }

    private void confirmDelete(Hotel hotel) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this hotel?")
                .setMessage(hotel.name + " and its rooms will be removed. This can't be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String error = nav.repository().deleteHotel(hotel.id);
                    nav.showMessage(error != null ? error : hotel.name + " deleted");
                    getParentFragmentManager().popBackStack();
                })
                .setNegativeButton("Keep", null)
                .show();
    }

    private List<Amenity> selectedAmenities() {
        List<Amenity> amenities = new ArrayList<>();
        for (int i = 0; i < binding.amenitiesChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.amenitiesChips.getChildAt(i);
            if (chip.isChecked()) {
                amenities.add((Amenity) chip.getTag());
            }
        }
        return amenities;
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showError(String message) {
        binding.editorError.setText(message);
        binding.editorError.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String textOf(TextInputEditText field) {
        Editable text = field.getText();
        return text == null ? "" : text.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
