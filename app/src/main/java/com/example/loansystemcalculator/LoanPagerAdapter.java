package com.example.loansystemcalculator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LoanPagerAdapter extends FragmentStateAdapter {

    private DatabaseHelper dbHelper;
    private LoanListFragment[] fragments;

    public LoanPagerAdapter(@NonNull FragmentActivity fragmentActivity, DatabaseHelper dbHelper) {
        super(fragmentActivity);
        this.dbHelper = dbHelper;
        this.fragments = new LoanListFragment[4];
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        LoanListFragment fragment = null;

        switch (position) {
            case 0:
                fragment = LoanListFragment.newInstance("all", dbHelper);
                break;
            case 1:
                fragment = LoanListFragment.newInstance("pending", dbHelper);
                break;
            case 2:
                fragment = LoanListFragment.newInstance("approved", dbHelper);
                break;
            case 3:
                fragment = LoanListFragment.newInstance("denied", dbHelper);
                break;
        }

        fragments[position] = fragment;
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4; // All, Pending, Approved, Denied
    }

    public void refreshAllFragments() {
        for (LoanListFragment fragment : fragments) {
            if (fragment != null) {
                fragment.refreshData();
            }
        }
    }

    // Added method to refresh only the fragment at a specific position
    public void refreshFragment(int position) {
        if (position >= 0 && position < fragments.length && fragments[position] != null) {
            fragments[position].refreshData();
        }
    }
}