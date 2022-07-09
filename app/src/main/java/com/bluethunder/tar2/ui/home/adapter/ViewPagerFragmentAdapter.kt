package com.bluethunder.tar2.ui.home.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    List<Fragment> fragments;

    public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> acquireTabModels) {
        super(fragmentActivity);
        fragments = acquireTabModels;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

}