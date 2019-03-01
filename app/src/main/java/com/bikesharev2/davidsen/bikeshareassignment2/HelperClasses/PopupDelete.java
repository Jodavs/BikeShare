package com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bikesharev2.davidsen.bikeshareassignment2.Bike;
import com.bikesharev2.davidsen.bikeshareassignment2.R;
import com.bikesharev2.davidsen.bikeshareassignment2.Ride;
import com.bikesharev2.davidsen.bikeshareassignment2.RidesDB;

// The popup delete options the user is shown when a bike is click
// in the bike list in check bike
public class PopupDelete {

    // Helper class to setting up the popup view
    public static void setPopupWindow(final Bike bike, final Context context, ConstraintLayout mConstraintLayoutMain, final RidesDB sRidesDB){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView = inflater.inflate(R.layout.popup_for_deleting, null);
        final PopupWindow mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (Build.VERSION.SDK_INT>=21){
            mPopupWindow.setElevation(5.0f);
        }

        TextView textView = customView.findViewById(R.id.popup_item);
        textView.setText(bike.getBikeName());

        Button cancelBtn = customView.findViewById(R.id.popup_cancel);
        cancelBtn.setOnClickListener(v -> mPopupWindow.dismiss());

        Button okBtn = customView.findViewById(R.id.popup_ok);
        okBtn.setOnClickListener(v -> {
            sRidesDB.deleteBike(bike);
            mPopupWindow.dismiss();
            Toast.makeText(context, "Selected Bike Removed", Toast.LENGTH_SHORT).show();
        });
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAtLocation(mConstraintLayoutMain, Gravity.CENTER, 0, 0);

    }
}
