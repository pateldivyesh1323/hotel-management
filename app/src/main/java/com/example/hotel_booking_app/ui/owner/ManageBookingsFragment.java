package com.example.hotel_booking_app.ui.owner;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Booking;
import com.example.hotel_booking_app.data.BookingStatus;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.databinding.FragmentManageBookingsBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.StatusTones;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ManageBookingsFragment extends Fragment implements HasTitle {

    private FragmentManageBookingsBinding binding;
    private Navigator nav;
    private OwnerBookingAdapter adapter;

    @Override
    public CharSequence title(Context context) {
        return "Manage bookings";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new OwnerBookingAdapter(nav.repository(), new OwnerBookingAdapter.Listener() {
            @Override
            public void onConfirm(String bookingId) {
                decide(nav.repository().confirmBooking(bookingId), "Booking confirmed");
            }

            @Override
            public void onReject(String bookingId) {
                promptReject(bookingId);
            }

            @Override
            public void onUpdateStatus(String bookingId) {
                promptStatus(bookingId);
            }
        });
        binding.ownerBookingsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.ownerBookingsList.setAdapter(adapter);
        render();
    }

    private void decide(@Nullable String error, String success) {
        nav.showMessage(error != null ? error : success);
        render();
    }

    private void promptReject(String bookingId) {
        View body = getLayoutInflater().inflate(R.layout.view_cancel_reason, null);
        TextInputEditText input = body.findViewById(R.id.cancel_reason_input);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reject this reservation?")
                .setView(body)
                .setPositiveButton("Reject", (dialog, which) -> {
                    Editable text = input.getText();
                    decide(nav.repository().rejectBooking(bookingId, text == null ? "" : text.toString()),
                            "Booking rejected");
                })
                .setNegativeButton("Keep", null)
                .show();
    }

    private void promptStatus(String bookingId) {
        BookingStatus[] statuses = BookingStatus.values();
        String[] labels = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            labels[i] = statuses[i].label;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update booking status")
                .setItems(labels, (dialog, which) -> {
                    decide(nav.repository().setBookingStatus(bookingId, statuses[which]),
                            "Status set to " + statuses[which].label);
                })
                .show();
    }

    private void render() {
        List<Booking> ordered = sorted(nav.repository().getBookings());
        adapter.setItems(ordered);
        binding.ownerBookingsEmpty.setVisibility(ordered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Booking> sorted(List<Booking> bookings) {
        List<Booking> pending = new ArrayList<>();
        List<Booking> others = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.status == BookingStatus.PENDING) {
                pending.add(booking);
            } else {
                others.add(booking);
            }
        }
        pending.addAll(others);
        return pending;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static final class OwnerBookingAdapter extends RecyclerView.Adapter<OwnerBookingAdapter.ViewHolder> {

        interface Listener {
            void onConfirm(String bookingId);

            void onReject(String bookingId);

            void onUpdateStatus(String bookingId);
        }

        private final List<Booking> items = new ArrayList<>();
        private final HotelRepository repository;
        private final Listener listener;

        OwnerBookingAdapter(HotelRepository repository, Listener listener) {
            this.repository = repository;
            this.listener = listener;
        }

        void setItems(List<Booking> bookings) {
            items.clear();
            items.addAll(bookings);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_owner_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking booking = items.get(position);
            Hotel hotel = repository.hotel(booking.hotelId);
            holder.hotel.setText(hotel != null ? hotel.name : booking.hotelId);

            TripStage stage = repository.stage(booking);
            StatusTones.Tone tone = StatusTones.forBooking(holder.status.getContext(), booking, stage);
            holder.status.setText(StatusTones.label(booking, stage));
            holder.status.getBackground().mutate().setColorFilter(tone.container, PorterDuff.Mode.SRC_IN);
            holder.status.setTextColor(tone.content);

            holder.guest.setText(booking.guestName);
            holder.contact.setText(contact(booking));
            holder.stay.setText(Format.stayRange(booking.checkIn, booking.checkOut)
                    + " · " + Format.nightsLabel(booking.getNights())
                    + " · " + roomsLabel(booking.rooms));

            boolean pending = booking.status == BookingStatus.PENDING;
            holder.actions.setVisibility(pending ? View.VISIBLE : View.GONE);
            if (pending) {
                holder.confirm.setOnClickListener(v -> listener.onConfirm(booking.id));
                holder.reject.setOnClickListener(v -> listener.onReject(booking.id));
            }
            holder.statusUpdate.setOnClickListener(v -> listener.onUpdateStatus(booking.id));
        }

        private String contact(Booking booking) {
            if (booking.guestPhone.trim().isEmpty()) {
                return booking.guestEmail;
            }
            return booking.guestEmail + " · " + booking.guestPhone;
        }

        private String roomsLabel(int rooms) {
            return rooms == 1 ? "1 room" : rooms + " rooms";
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final TextView hotel;
            final TextView status;
            final TextView guest;
            final TextView contact;
            final TextView stay;
            final View actions;
            final View confirm;
            final View reject;
            final View statusUpdate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                hotel = itemView.findViewById(R.id.owner_booking_hotel);
                status = itemView.findViewById(R.id.owner_booking_status);
                guest = itemView.findViewById(R.id.owner_booking_guest);
                contact = itemView.findViewById(R.id.owner_booking_contact);
                stay = itemView.findViewById(R.id.owner_booking_stay);
                actions = itemView.findViewById(R.id.owner_booking_actions);
                confirm = itemView.findViewById(R.id.owner_booking_confirm);
                reject = itemView.findViewById(R.id.owner_booking_reject);
                statusUpdate = itemView.findViewById(R.id.owner_booking_status_update);
            }
        }
    }
}
