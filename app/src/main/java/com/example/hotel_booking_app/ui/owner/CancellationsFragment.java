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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Booking;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.RefundStatus;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.databinding.FragmentCancellationsBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.StatusTones;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CancellationsFragment extends Fragment implements HasTitle {

    private FragmentCancellationsBinding binding;
    private Navigator nav;
    private CancellationAdapter adapter;

    @Override
    public CharSequence title(Context context) {
        return "Cancellation management";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCancellationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new CancellationAdapter(nav.repository(), this::promptRefund);
        binding.cancellationsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cancellationsList.setAdapter(adapter);
        render();
    }

    private void render() {
        List<Booking> cancelled = nav.repository().cancellations();
        adapter.setItems(cancelled);
        binding.cancellationsEmpty.setVisibility(cancelled.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void promptRefund(String bookingId) {
        RefundStatus[] statuses = RefundStatus.values();
        String[] labels = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            labels[i] = statuses[i].label;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update refund status")
                .setItems(labels, (dialog, which) -> {
                    String error = nav.repository().setRefundStatus(bookingId, statuses[which]);
                    nav.showMessage(error != null ? error : "Refund set to " + statuses[which].label);
                    render();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static final class CancellationAdapter extends RecyclerView.Adapter<CancellationAdapter.ViewHolder> {

        interface Listener {
            void onUpdateRefund(String bookingId);
        }

        private final List<Booking> items = new ArrayList<>();
        private final HotelRepository repository;
        private final Listener listener;

        CancellationAdapter(HotelRepository repository, Listener listener) {
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
                    .inflate(R.layout.item_cancellation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking booking = items.get(position);
            Hotel hotel = repository.hotel(booking.hotelId);
            holder.hotel.setText(hotel != null ? hotel.name : booking.hotelId);

            TripStage stage = repository.stage(booking);
            StatusTones.Tone tone = StatusTones.forBooking(holder.status.getContext(), booking, stage);
            holder.status.setText(booking.status.label);
            holder.status.getBackground().mutate().setColorFilter(tone.container, PorterDuff.Mode.SRC_IN);
            holder.status.setTextColor(tone.content);

            holder.guest.setText(booking.guestName);
            String date = booking.cancelledOn != null ? Format.fullDate(booking.cancelledOn) : "Date unknown";
            holder.detail.setText(date + " · " + booking.cancellationReason);
            holder.refund.setText(booking.refundStatus.label);
            holder.refundUpdate.setOnClickListener(v -> listener.onUpdateRefund(booking.id));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final TextView hotel;
            final TextView status;
            final TextView guest;
            final TextView detail;
            final TextView refund;
            final View refundUpdate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                hotel = itemView.findViewById(R.id.cancellation_hotel);
                status = itemView.findViewById(R.id.cancellation_status);
                guest = itemView.findViewById(R.id.cancellation_guest);
                detail = itemView.findViewById(R.id.cancellation_detail);
                refund = itemView.findViewById(R.id.cancellation_refund);
                refundUpdate = itemView.findViewById(R.id.cancellation_refund_update);
            }
        }
    }
}
