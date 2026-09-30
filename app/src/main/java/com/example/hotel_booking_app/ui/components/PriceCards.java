package com.example.hotel_booking_app.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.PriceBreakdown;
import com.example.hotel_booking_app.ui.Format;
import com.example.hotel_booking_app.ui.Views;
import com.google.android.material.color.MaterialColors;

public final class PriceCards {

    private PriceCards() {
    }

    public static void bind(View card, PriceBreakdown price) {
        LinearLayout rows = card.findViewById(R.id.price_rows);
        rows.removeAllViews();

        Views.addDetailRow(rows,
                Format.money(price.nightlyRate) + " × " + Format.nightsLabel(price.nights),
                Format.money(price.subtotal));
        if (price.breakfast > 0) {
            Views.addDetailRow(rows, "Breakfast", Format.money(price.breakfast));
        }
        if (price.discount > 0) {
            Views.addDetailRow(rows, "Promo discount", "-" + Format.money(price.discount));
        }
        Views.addDetailRow(rows, "Taxes & fees (" + BookingRules.TAX_PERCENT + "%)",
                Format.money(price.taxes));

        View divider = new View(rows.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(rows, 1));
        params.topMargin = dp(rows, 4);
        params.bottomMargin = dp(rows, 4);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(MaterialColors.getColor(rows,
                com.google.android.material.R.attr.colorOutline));
        rows.addView(divider);

        View total = LayoutInflater.from(rows.getContext())
                .inflate(R.layout.item_price_total, rows, false);
        ((TextView) total.findViewById(R.id.price_total)).setText(Format.money(price.total));
        rows.addView(total);
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}
