package com.example.myapplication.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.Notifications.MainActivity;
import com.example.myapplication.R;

import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        linearLayout = findViewById(R.id.linearLayout);
        int fragmentCount = 10;

        clearFragments();

        for (int i = 0; i < fragmentCount; i++) {
            addFragment(NotePreviewFragment.newInstance("sadasd", "asdasdasd"), i);
        }
    }

    private void addFragment(Fragment fragment, int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.linearLayout, fragment, "fragment_" + position);
        transaction.commit();
    }

    private void clearFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        if (fragments != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    transaction.remove(fragment);
                }
            }
            transaction.commit();
        }
    }

    public void GoToNotificationsActivity(View v)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}