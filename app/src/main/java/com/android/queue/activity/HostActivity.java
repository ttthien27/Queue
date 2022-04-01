package com.android.queue.activity;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;


import com.android.queue.R;
import com.android.queue.fragment.HostRoomSettingFragment;
import com.android.queue.fragment.HostRoomWaitingFragment;
import com.android.queue.fragment.KeyRoomFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HostActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        //Hook view from layout
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        //If this room is first create and navigate from CreateRoomActivity, we navigate to KeyRoomFragment. Otherwise we navigate to HostRoomWaitingFragment
        Intent intent = getIntent();
        if (intent.getBooleanExtra("firstCreate", false)) {
            navigateTo(new KeyRoomFragment(), false);
        } else {
            navigateTo(new HostRoomWaitingFragment(), false);
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.roomKey:
                navigateTo(new KeyRoomFragment(), false);
                return true;
            case R.id.roomWait:
                navigateTo(new HostRoomWaitingFragment(), false);
                return true;
            case R.id.roomSetting:
                navigateTo(new HostRoomSettingFragment(), false);
                return true;
        }
        return false;
    }

    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}