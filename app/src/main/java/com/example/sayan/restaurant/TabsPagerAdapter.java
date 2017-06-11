package com.example.sayan.restaurant;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;


/**
 * Created by Sayan on 22-Apr-17.
 */

class TabsPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Place> places;
    TabsPagerAdapter(FragmentManager fm, ArrayList<Place> places) {
        super(fm);
        this.places = places;
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new RestaurantList(places);
            case 1:
                return new UserProfile();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }

}

