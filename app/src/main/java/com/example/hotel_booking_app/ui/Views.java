package com.example.hotel_booking_app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hotel_booking_app.R;
import com.google.android.material.color.MaterialColors;

public final class Views {

    private Views() {
    }

    public static void addDetailRow(ViewGroup container, String label, String value) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View row = inflater.inflate(R.layout.item_detail_row, container, false);
        ((TextView) row.findViewById(R.id.detail_label)).setText(label);
        ((TextView) row.findViewById(R.id.detail_value)).setText(value);
        container.addView(row);
    }

    public static void bindFavorite(ImageView view, boolean favorite) {
        view.setImageResource(favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        int tint = MaterialColors.getColor(view, favorite
                ? com.google.android.material.R.attr.colorError
                : com.google.android.material.R.attr.colorOnSurface);
        view.setColorFilter(tint);
        view.setContentDescription(favorite ? "Remove from saved" : "Save hotel");
    }
}
