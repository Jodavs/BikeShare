package com.bikesharev2.davidsen.bikeshareassignment2;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.bikesharev2.davidsen.bikeshareassignment2.Tabs.TabFragmentBikeList;
import com.bikesharev2.davidsen.bikeshareassignment2.Tabs.TabFragmentPaymentList;
import com.bikesharev2.davidsen.bikeshareassignment2.Tabs.TabFragmentRideList;

import java.util.ArrayList;
import java.util.List;

// Class is used to create the fragment tabs
public class BikeShareFragmentCheckBike extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_check_bike, container, false);

        // Hides the action bar
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

        // setting pager for changing tabs
        ViewPager pager = v.findViewById(R.id.pager);

        // Creating the adapter and adding the fragments
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new TabFragmentBikeList(), "Your Bikes");
        adapter.addFragment(new TabFragmentRideList(), "Your Rides");
        adapter.addFragment(new TabFragmentPaymentList(), "Your Payments");
        pager.setAdapter(adapter);

        // Setting the tabLayout
        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);

        return v;
    }

    // ViewPager for keeping track of the fragment tabs
    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            mFragments.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }
    }
}



