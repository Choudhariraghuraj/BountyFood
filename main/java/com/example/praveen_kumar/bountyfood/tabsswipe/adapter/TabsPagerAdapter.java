package com.example.praveen_kumar.bountyfood.tabsswipe.adapter;

/**
 * Created by PRAVEEN_KUMAR on 18-08-2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public TabsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Delivery Agent";
            case 1:
            default:
                return "Customer";
        }
    }
}