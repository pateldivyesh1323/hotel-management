package com.example.hotel_booking_app.ui.owner;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.databinding.FragmentManageHotelsBinding;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;

import java.util.ArrayList;
import java.util.List;

public class ManageHotelsFragment extends Fragment implements HasTitle {

    private FragmentManageHotelsBinding binding;
    private Navigator nav;
    private HotelAdapter adapter;

    @Override
    public CharSequence title(Context context) {
        return "Manage hotels";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManageHotelsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new HotelAdapter(nav::openEditHotel);
        binding.hotelsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.hotelsList.setAdapter(adapter);
        binding.addHotelButton.setOnClickListener(v -> nav.openAddHotel());
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setItems(nav.repository().getHotels());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static final class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.ViewHolder> {

        interface Listener {
            void onOpen(String hotelId);
        }

        private final List<Hotel> items = new ArrayList<>();
        private final Listener listener;

        HotelAdapter(Listener listener) {
            this.listener = listener;
        }

        void setItems(List<Hotel> hotels) {
            items.clear();
            items.addAll(hotels);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_manage_hotel, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Hotel hotel = items.get(position);
            holder.image.setImageResource(Photos.hotelPhoto(hotel.photoIndex));
            holder.name.setText(hotel.name);
            holder.meta.setText(hotel.city + " · " + Format.stars(hotel.stars));
            holder.itemView.setOnClickListener(v -> listener.onOpen(hotel.id));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView name;
            final TextView meta;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.manage_hotel_image);
                name = itemView.findViewById(R.id.manage_hotel_name);
                meta = itemView.findViewById(R.id.manage_hotel_meta);
            }
        }
    }
}
