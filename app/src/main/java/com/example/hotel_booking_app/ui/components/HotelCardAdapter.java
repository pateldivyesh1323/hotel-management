package com.example.hotel_booking_app.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Amenity;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.Photos;
import com.example.hotel_booking_app.ui.Views;

import java.util.ArrayList;
import java.util.List;

public class HotelCardAdapter extends RecyclerView.Adapter<HotelCardAdapter.ViewHolder> {

    public static final class Entry {
        public final Hotel hotel;
        @Nullable
        public final Integer fromRate;

        public Entry(Hotel hotel, @Nullable Integer fromRate) {
            this.hotel = hotel;
            this.fromRate = fromRate;
        }
    }

    public interface Listener {
        boolean isFavorite(String hotelId);

        void onToggleFavorite(String hotelId);

        void onOpen(String hotelId);
    }

    private final List<Entry> items = new ArrayList<>();
    private final Listener listener;

    public HotelCardAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Entry> entries) {
        items.clear();
        items.addAll(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = items.get(position);
        Hotel hotel = entry.hotel;

        holder.image.setImageResource(Photos.hotelPhoto(hotel.photoIndex));
        holder.name.setText(hotel.name);
        holder.rating.setText(Format.ratingLabel(hotel.rating));
        holder.location.setText(hotel.getLocation() + " · " + Format.stars(hotel.stars));
        holder.amenities.setText(amenitySummary(hotel));

        if (entry.fromRate != null) {
            holder.priceRow.setVisibility(View.VISIBLE);
            holder.price.setText(Format.money(entry.fromRate));
        } else {
            holder.priceRow.setVisibility(View.GONE);
        }

        Views.bindFavorite(holder.favorite, listener.isFavorite(hotel.id));

        holder.favorite.setOnClickListener(v -> {
            listener.onToggleFavorite(hotel.id);
            notifyItemChanged(holder.getAdapterPosition());
        });
        holder.itemView.setOnClickListener(v -> listener.onOpen(hotel.id));
    }

    private String amenitySummary(Hotel hotel) {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(3, hotel.amenities.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(" · ");
            }
            Amenity amenity = hotel.amenities.get(i);
            builder.append(amenity.label);
        }
        return builder.toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final ImageView favorite;
        final TextView name;
        final TextView rating;
        final TextView location;
        final TextView amenities;
        final View priceRow;
        final TextView price;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.hotel_image);
            favorite = itemView.findViewById(R.id.favorite_button);
            name = itemView.findViewById(R.id.hotel_name);
            rating = itemView.findViewById(R.id.hotel_rating);
            location = itemView.findViewById(R.id.hotel_location);
            amenities = itemView.findViewById(R.id.hotel_amenities);
            priceRow = itemView.findViewById(R.id.hotel_price_row);
            price = itemView.findViewById(R.id.hotel_price);
        }
    }
}
