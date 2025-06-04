package com.example.eventjoy.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eventjoy.fragments.ChatFragment;
import com.example.eventjoy.fragments.EventsFragment;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private String userGroupRole;
    private Group group;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String userGroupRole, Group group) {
        super(fragmentActivity);
        this.userGroupRole = userGroupRole;
        this.group = group;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putString("userGroupRole", userGroupRole);
        args.putSerializable("group", group);

        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new ChatFragment();
                break;
            case 1:
                fragment = new EventsFragment();
                break;
            default:
                fragment = new ChatFragment();
                break;
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
