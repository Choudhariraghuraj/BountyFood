package com.example.praveen_kumar.bountyfood;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.praveen_kumar.bountyfood.tabsswipe.adapter.TabsPagerAdapter;

import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Initializing tab and pager views
        tabLayout = (TabLayout) findViewById(R.id.my_tab_layout);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.my_view_pager);

// Making new tabs and adding to tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Delivery Agent"));
        tabLayout.addTab(tabLayout.newTab().setText("Customer"));

// Adding fragments to a list
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, AgentFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, CustomerFragment.class.getName()));

// Attaching fragments into tabLayout with ViewPager
        viewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
    }
}