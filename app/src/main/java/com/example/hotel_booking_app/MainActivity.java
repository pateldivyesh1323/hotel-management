package com.example.hotel_booking_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.StayRequest;
import com.example.hotel_booking_app.databinding.ActivityMainBinding;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.auth.AuthActivity;
import com.example.hotel_booking_app.ui.booking.CheckoutFragment;
import com.example.hotel_booking_app.ui.explore.ExploreFragment;
import com.example.hotel_booking_app.ui.explore.HomeFragment;
import com.example.hotel_booking_app.ui.explore.HotelDetailFragment;
import com.example.hotel_booking_app.ui.owner.CancellationsFragment;
import com.example.hotel_booking_app.ui.owner.HotelEditorFragment;
import com.example.hotel_booking_app.ui.owner.ManageBookingsFragment;
import com.example.hotel_booking_app.ui.owner.ManageHotelsFragment;
import com.example.hotel_booking_app.ui.owner.ManageUsersFragment;
import com.example.hotel_booking_app.ui.owner.OwnerDashboardFragment;
import com.example.hotel_booking_app.ui.owner.UserDetailFragment;
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
    private int topInset = 0;
    private boolean heroTop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ((HotelApplication) getApplication()).repository();
        if (!repository.isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LocalDate today = repository.currentDate();
        stay = new StayRequest(
                today.plusDays(DEFAULT_LEAD_DAYS),
                today.plusDays(DEFAULT_LEAD_DAYS + DEFAULT_NIGHTS),
                2);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            applyInsets();
            return insets;
        });

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
        heroTop = id == R.id.tab_explore;
        applyInsets();
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
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.container);
        if (detail) {
            CharSequence title = current instanceof HasTitle ? ((HasTitle) current).title(this) : "";
            binding.toolbar.setTitle(title);
        } else {
            heroTop = current instanceof HomeFragment;
        }
        applyInsets();
    }

    private void applyInsets() {
        binding.toolbar.setPadding(0, topInset, 0, 0);
        boolean detail = getSupportFragmentManager().getBackStackEntryCount() > 0;
        int containerTop = (detail || heroTop) ? 0 : topInset;
        binding.container.setPadding(0, containerTop, 0, 0);
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
        showMessage("Reservation requested · awaiting owner confirmation");
    }

    @Override
    public void openOwnerDashboard() {
        push(new OwnerDashboardFragment());
    }

    @Override
    public void openManageHotels() {
        push(new ManageHotelsFragment());
    }

    @Override
    public void openAddHotel() {
        push(HotelEditorFragment.newInstance(null));
    }

    @Override
    public void openEditHotel(String hotelId) {
        push(HotelEditorFragment.newInstance(hotelId));
    }

    @Override
    public void openManageBookings() {
        push(new ManageBookingsFragment());
    }

    @Override
    public void openManageUsers() {
        push(new ManageUsersFragment());
    }

    @Override
    public void openUserDetail(String email) {
        push(UserDetailFragment.newInstance(email));
    }

    @Override
    public void openCancellations() {
        push(new CancellationsFragment());
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void logOut() {
        repository.logOut();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}
