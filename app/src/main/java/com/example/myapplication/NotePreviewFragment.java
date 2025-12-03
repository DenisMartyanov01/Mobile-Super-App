package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.EventLogTags;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class NotePreviewFragment extends Fragment {

    private String Title;
    private String Description;

    public static NotePreviewFragment newInstance(String title, String description) {
        NotePreviewFragment fragment = new NotePreviewFragment();
        fragment.Title = title;
        fragment.Description = description;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.Title)).setText(Title);
        ((TextView)view.findViewById(R.id.Description)).setText(Description);
    }

}