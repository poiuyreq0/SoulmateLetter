package com.poiuyreq0.soulmateletter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.poiuyreq0.soulmateletter.databinding.ActivitySoulmateBinding;

import java.util.ArrayList;
import java.util.List;

public class SoulmateActivity extends AppCompatActivity {

    private static final String TAG = "SoulmateActivity";

    private ActivitySoulmateBinding binding;
    private DatabaseReference mDatabase;

    private String sender;

    private RecyclerView.LayoutManager mLayoutManager;
    private LetterAdapter mAdapter;

    List<Letter> exchangedLetters = new ArrayList<>();

    GenericTypeIndicator<List<Letter>> t = new GenericTypeIndicator<List<Letter>>() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySoulmateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        sender = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        mDatabase.child("saved").child(sender).addValueEventListener(savedListener);
        mDatabase.child("exchanged").child(sender).addValueEventListener(exchangedListener);

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                startActivity(intent);
            }
        });
    }

    private ValueEventListener savedListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            Long count = dataSnapshot.getChildrenCount();

            binding.countTextView.setText(count.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "letterListener: onCancelled", databaseError.toException());
        }
    };

    private ValueEventListener exchangedListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            exchangedLetters = dataSnapshot.getValue(t);
            if (exchangedLetters == null) {
                exchangedLetters = new ArrayList<>();
            }

            mLayoutManager = new LinearLayoutManager(SoulmateActivity.this);
            binding.recyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new LetterAdapter(exchangedLetters);
            binding.recyclerView.setAdapter(mAdapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "letterListener: onCancelled", databaseError.toException());
        }
    };
}

