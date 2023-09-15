package com.driver_hiring.driver;


import android.os.Bundle;
import androidx.annotation.Nullable;

import com.driver_hiring.driver.rides.CurrentRides;
import com.driver_hiring.driver.rides.PastRides;
import com.driver_hiring.driver.rides.UpcomingRides;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.driver_hiring.driver.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryActivity extends AppCompatActivity {
    public static boolean startMockLocation = false;
    private SectionPagerAdapter sectionPagerAdapter;


    public HistoryActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        getSupportActionBar().setTitle("Ride History");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CurrentRides();
                case 1:
                    return new PastRides();
                default:
                    return new UpcomingRides();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Current";
                case 1:
                    return "Past Rides";
                default:
                    return "Upcoming";
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mock_menu, menu);
        MenuItem item = menu.findItem(R.id.action_mock_location);
        item.setActionView(R.layout.switch_layout);
        final Switch switchMock = item.getActionView().findViewById(R.id.switch_mock_location);
        switchMock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    DriverService.mLocation = null;
                }
                startMockLocation = isChecked;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        startMockLocation = false;
        super.onDestroy();
    }
}
