package com.example.hotel_booking_app.ui.explore;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Amenity;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.HotelResult;
import com.example.hotel_booking_app.data.SearchFilters;
import com.example.hotel_booking_app.data.SortOrder;
import com.example.hotel_booking_app.databinding.FragmentExploreBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.components.HotelCardAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ExploreFragment extends Fragment implements HasTitle, HotelCardAdapter.Listener {

    private static final float[] RATING_STEPS = {0f, 4f, 4.5f, 4.8f};
    private static final int[] PRICE_STEPS = {0, 8000, 15000, 25000};

    private FragmentExploreBinding binding;
    private Navigator nav;
    private HotelCardAdapter adapter;

    private SortOrder sort = SortOrder.RECOMMENDED;
    private float minRating = 0f;
    private int maxPrice = 0;
    private final Set<Amenity> amenities = EnumSet.noneOf(Amenity.class);
    private boolean showFilters = false;

    @Override
    public CharSequence title(Context context) {
        return "Hotels";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new HotelCardAdapter(this);
        binding.resultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.resultsList.setAdapter(adapter);

        binding.promoHint.setText("Use " + BookingRules.promoCodeHints() + " at checkout");

        buildCityChips();
        buildSortChips();
        buildRatingChips();
        buildPriceChips();
        buildAmenityChips();

        binding.toggleFilters.setOnClickListener(v -> {
            showFilters = !showFilters;
            binding.filterPanel.setVisibility(showFilters ? View.VISIBLE : View.GONE);
        });
        binding.clearFilters.setOnClickListener(v -> clearFilters());

        refresh();
    }

    private void buildCityChips() {
        for (String city : nav.repository().cities()) {
            boolean checked = city.equalsIgnoreCase(nav.query());
            addChip(binding.cityChips, city, city, checked);
        }
        binding.cityChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                nav.updateQuery("");
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                nav.updateQuery((String) chip.getTag());
            }
            refresh();
        });
    }

    private void buildSortChips() {
        for (SortOrder order : SortOrder.values()) {
            addChip(binding.sortChips, order.label, order, order == sort);
        }
        binding.sortChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                sort = (SortOrder) chip.getTag();
                refresh();
            }
        });
    }

    private void buildRatingChips() {
        for (float step : RATING_STEPS) {
            String label = step == 0f ? "Any" : step + "+";
            addChip(binding.ratingChips, label, step, step == minRating);
        }
        binding.ratingChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                minRating = (Float) chip.getTag();
                refresh();
            }
        });
    }

    private void buildPriceChips() {
        for (int step : PRICE_STEPS) {
            String label = step == 0 ? "Any" : Format.money(step);
            addChip(binding.priceChips, label, step, step == maxPrice);
        }
        binding.priceChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                maxPrice = (Integer) chip.getTag();
                refresh();
            }
        });
    }

    private void buildAmenityChips() {
        for (Amenity amenity : Amenity.values()) {
            addChip(binding.amenityChips, amenity.label, amenity, amenities.contains(amenity));
        }
        binding.amenityChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            amenities.clear();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                amenities.add((Amenity) chip.getTag());
            }
            refresh();
        });
    }

    private Chip addChip(ChipGroup group, String text, Object tag, boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_filter_chip, group, false);
        chip.setText(text);
        chip.setTag(tag);
        chip.setChecked(checked);
        group.addView(chip);
        return chip;
    }

    private void clearFilters() {
        minRating = 0f;
        maxPrice = 0;
        amenities.clear();
        checkByTag(binding.ratingChips, 0f);
        checkByTag(binding.priceChips, 0);
        binding.amenityChips.clearCheck();
        refresh();
    }

    private void checkByTag(ChipGroup group, Object tag) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            chip.setChecked(tag.equals(chip.getTag()));
        }
    }

    private void refresh() {
        SearchFilters filters = new SearchFilters(minRating, maxPrice > 0 ? maxPrice : null, amenities);
        List<HotelResult> results = nav.repository().search(nav.query(), nav.stay(), filters, sort);

        String query = nav.query();
        binding.exploreTitle.setText(query.trim().isEmpty() ? "All destinations" : query);
        binding.exploreSubtitle.setText(Format.stayRange(nav.stay().checkIn, nav.stay().checkOut)
                + " · " + Format.nightsLabel(nav.stay().getNights())
                + " · " + Format.guestsLabel(nav.stay().guests));

        binding.resultsCount.setText(results.size() == 1
                ? "1 hotel available"
                : results.size() + " hotels available");

        binding.toggleFilters.setText(filters.isActive() ? "Filters (on)" : "Filters");
        binding.clearFilters.setVisibility(filters.isActive() ? View.VISIBLE : View.GONE);
        binding.emptyHint.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);

        List<HotelCardAdapter.Entry> entries = new ArrayList<>();
        for (HotelResult result : results) {
            entries.add(new HotelCardAdapter.Entry(result.hotel, result.fromRate));
        }
        adapter.setItems(entries);
    }

    @Override
    public boolean isFavorite(String hotelId) {
        return nav.repository().isFavorite(hotelId);
    }

    @Override
    public void onToggleFavorite(String hotelId) {
        nav.repository().toggleFavorite(hotelId);
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
