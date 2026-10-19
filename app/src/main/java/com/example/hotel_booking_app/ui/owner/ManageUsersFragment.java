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
import com.example.hotel_booking_app.data.GuestAccount;
import com.example.hotel_booking_app.databinding.FragmentManageUsersBinding;
import com.example.hotel_booking_app.ui.HasTitle;
import com.example.hotel_booking_app.ui.Navigator;
import com.example.hotel_booking_app.ui.Photos;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersFragment extends Fragment implements HasTitle {

    private FragmentManageUsersBinding binding;
    private Navigator nav;
    private UserAdapter adapter;

    @Override
    public CharSequence title(Context context) {
        return "Manage users";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManageUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nav = (Navigator) requireActivity();
        adapter = new UserAdapter(nav::openUserDetail);
        binding.usersList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.usersList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        List<GuestAccount> users = nav.repository().users();
        adapter.setItems(users);
        binding.usersEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static final class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        interface Listener {
            void onOpen(String email);
        }

        private final List<GuestAccount> items = new ArrayList<>();
        private final Listener listener;

        UserAdapter(Listener listener) {
            this.listener = listener;
        }

        void setItems(List<GuestAccount> users) {
            items.clear();
            items.addAll(users);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GuestAccount user = items.get(position);
            holder.avatar.setImageResource(Photos.avatar(user.name));
            holder.name.setText(user.name);
            holder.contact.setText(contact(user));
            holder.bookings.setText(user.bookings == 1 ? "1 booking" : user.bookings + " bookings");
            holder.itemView.setOnClickListener(v -> listener.onOpen(user.email));
        }

        private String contact(GuestAccount user) {
            if (user.phone.trim().isEmpty()) {
                return user.email;
            }
            return user.email + " · " + user.phone;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView avatar;
            final TextView name;
            final TextView contact;
            final TextView bookings;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.user_avatar);
                name = itemView.findViewById(R.id.user_name);
                contact = itemView.findViewById(R.id.user_contact);
                bookings = itemView.findViewById(R.id.user_bookings);
            }
        }
    }
}
