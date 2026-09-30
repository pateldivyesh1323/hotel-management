package com.example.hotel_booking_app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.StayRequest;
import com.example.hotel_booking_app.databinding.ActivityMainBinding;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.booking.CheckoutFragment;
import com.example.hotel_booking_app.ui.explore.ExploreFragment;
import com.example.hotel_booking_app.ui.explore.HomeFragment;
import com.example.hotel_booking_app.ui.explore.HotelDetailFragment;
import com.example.hotel_booking_app.ui.profile.ProfileFragment;
import com.example.hotel_booking_app.ui.saved.SavedFragment;
import com.example.hotel_booking_app.ui.trips.TripDetailFragment;
import com.example.hotel_booking_app.ui.trips.TripsFragment;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity implements Navigator {

    private static final long DEFAULT_LEAD_DAYS = 7L;
    private static final long DEFAULT_NIGHTS = 2L;

    private ActivityMainBinding binding;
    private HotelRepository repository;
    private StayRequest stay;
    private String query = "";
    private boolean suppressTabCallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = ((HotelApplication) getApplication()).repository();
        LocalDate today = repository.currentDate();
        stay = new StayRequest(
                today.plusDays(DEFAULT_LEAD_DAYS),
                today.plusDays(DEFAULT_LEAD_DAYS + DEFAULT_NIGHTS),
                2);

        binding.toolbar.setNavigationOnClickListener(v -> getSupportFragmentManager().popBackStack());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (!suppressTabCallback) {
                selectTab(item.getItemId());
            }
            return true;
        });
        getSupportFragmentManager().addOnBackStackChangedListener(this::updateChrome);

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.tab_explore);
        } else {
            query = savedInstanceState.getString("query", "");
            updateChrome();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", query);
    }

    private Fragment fragmentForTab(int id) {
        if (id == R.id.tab_hotels) {
            return new ExploreFragment();
        }
        if (id == R.id.tab_saved) {
            return new SavedFragment();
        }
        if (id == R.id.tab_trips) {
            return new TripsFragment();
        }
        if (id == R.id.tab_profile) {
            return new ProfileFragment();
        }
        return new HomeFragment();
    }

    private void selectTab(int id) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragmentForTab(id))
                .commit();
        binding.toolbar.setVisibility(View.GONE);
        binding.bottomNav.setVisibility(View.VISIBLE);
    }

    private void push(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateChrome() {
        boolean detail = getSupportFragmentManager().getBackStackEntryCount() > 0;
        binding.toolbar.setVisibility(detail ? View.VISIBLE : View.GONE);
        binding.bottomNav.setVisibility(detail ? View.GONE : View.VISIBLE);
        if (detail) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
            CharSequence title = current instanceof HasTitle ? ((HasTitle) current).title(this) : "";
            binding.toolbar.setTitle(title);
        }
    }

    @Override
    public HotelRepository repository() {
        return repository;
    }

    @Override
    public StayRequest stay() {
        return stay;
    }

    @Override
    public void updateStay(StayRequest updated) {
        stay = updated;
    }

    @Override
    public String query() {
        return query;
    }

    @Override
    public void updateQuery(String updated) {
        query = updated;
    }

    @Override
    public void openResults() {
        push(new ExploreFragment());
    }

    @Override
    public void openHotel(String hotelId) {
        push(HotelDetailFragment.newInstance(hotelId));
    }

    @Override
    public void openCheckout(String offerId) {
        push(CheckoutFragment.newInstance(offerId));
    }

    @Override
    public void openTrip(String bookingId) {
        push(TripDetailFragment.newInstance(bookingId));
    }

    @Override
    public void openTripAfterBooking(String bookingId) {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        suppressTabCallback = true;
        binding.bottomNav.setSelectedItemId(R.id.tab_trips);
        suppressTabCallback = false;
        selectTab(R.id.tab_trips);
        push(TripDetailFragment.newInstance(bookingId));
        showMessage("Booking confirmed");
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show();
    }
}
