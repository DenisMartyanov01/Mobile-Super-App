package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.Notes.NotesFragment;
import com.example.myapplication.Notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new NotificationsFragment());
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.bottom_nav_notifications) {
                            loadFragment(new NotificationsFragment());
                            return true;
                        } else if (id == R.id.bottom_nav_notes) {
                            loadFragment(new NotesFragment());
                            return true;
                        }
                        return false;
                    }
                }
        );
    }
}