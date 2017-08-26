package com.example.praveen_kumar.bountyfood;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by PRAVEEN_KUMAR on 18-08-2017.
 */

public class CustomerFragment extends Fragment {
    Button confirmBtn,checkBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Declare your second fragment here
        LinearLayout mRelativeLayout = (LinearLayout) inflater.inflate(R.layout.customer_layout, container, false);
        confirmBtn = (Button) mRelativeLayout.findViewById(R.id.confirmOrderId);
        checkBtn = (Button) mRelativeLayout.findViewById(R.id.checkId);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBtn.setVisibility(View.GONE);
                checkBtn.setVisibility(View.VISIBLE);
            }
        });
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkBtn.setVisibility(View.GONE); //to make disappear map
                Intent myIntent = new Intent(getActivity(), MapsActivity.class);
                myIntent.putExtra("mode", "customer");
                getActivity().startActivity(myIntent);
            }
        });
        // after you've done all your manipulation, return your layout to be shown
        return mRelativeLayout;
    }
}
