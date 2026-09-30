package com.example.hotel_booking_app.ui.trips;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Booking;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.TripStage;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.Photos;
import com.example.hotel_booking_app.ui.StatusTones;

import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    public interface Listener {
        void onOpen(String bookingId);
    }

    private final List<Booking> items = new ArrayList<>();
    private final HotelRepository repository;
    private final Listener listener;

    public TripAdapter(HotelRepository repository, Listener listener) {
        this.repository = repository;
        this.listener = listener;
    }

    public void setItems(List<Booking> bookings) {
        items.clear();
        items.addAll(bookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = items.get(position);
        Hotel hotel = repository.hotel(booking.hotelId);
        if (hotel == null) {
            return;
        }
        TripStage stage = repository.stage(booking);

        holder.image.setImageResource(Photos.hotelPhoto(hotel.photoIndex));
        holder.name.setText(hotel.name);
        holder.location.setText(hotel.getLocation());
        holder.dates.setText(Format.stayRange(booking.checkIn, booking.checkOut)
                + " · " + Format.nightsLabel(booking.getNights()));
        holder.price.setText(Format.money(BookingRules.price(booking).total));

        StatusTones.Tone tone = StatusTones.forStage(holder.status.getContext(), stage);
        holder.status.setText(stage.label);
        holder.status.getBackground().mutate().setColorFilter(tone.container, PorterDuff.Mode.SRC_IN);
        holder.status.setTextColor(tone.content);

        holder.itemView.setOnClickListener(v -> listener.onOpen(booking.id));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView location;
        final TextView dates;
        final TextView status;
        final TextView price;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.trip_image);
            name = itemView.findViewById(R.id.trip_name);
            location = itemView.findViewById(R.id.trip_location);
            dates = itemView.findViewById(R.id.trip_dates);
            status = itemView.findViewById(R.id.trip_status);
            price = itemView.findViewById(R.id.trip_price);
        }
    }
}
