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

public class AgentFragment extends Fragment {
        Button confirmBtn;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                // Declare your first fragment here
                LinearLayout mRelativeLayout = (LinearLayout) inflater.inflate(R.layout.agent_layout, container, false);
                confirmBtn = (Button) mRelativeLayout.findViewById(R.id.confirmId);
                confirmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //confirmBtn.setVisibility(View.GONE); //to make disappear map
                            //nearLayout.setVisibility(View.VISIBLE);
                            Intent myIntent = new Intent(getActivity(), MapsActivity.class);
                            myIntent.putExtra("mode", "agent");
                            getActivity().startActivity(myIntent);

                        }
                });
                // after you've done all your manipulation, return your layout to be shown
                return mRelativeLayout;
        }
}
