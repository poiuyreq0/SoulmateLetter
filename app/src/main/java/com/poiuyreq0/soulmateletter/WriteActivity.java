package com.poiuyreq0.soulmateletter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.poiuyreq0.soulmateletter.databinding.ActivityWriteBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WriteActivity extends AppCompatActivity {

    private static final String TAG = "WriteActivity";

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^010\\d{8}$");

    private ActivityWriteBinding binding;
    private DatabaseReference mDatabase;

    String sender;
    String receiver;
    String text;

    Boolean exchangeFlag = false;

    List<Letter> lettersA = new ArrayList<>();
    List<Letter> lettersB = new ArrayList<>();
    List<Letter> tempLettersA = new ArrayList<>();
    List<Letter> tempLettersB = new ArrayList<>();
    List<Letter> exchangeLettersA = new ArrayList<>();
    List<Letter> exchangeLettersB = new ArrayList<>();

    GenericTypeIndicator<List<Letter>> t = new GenericTypeIndicator<List<Letter>>() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        sender = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                text = binding.letterEditText.getText().toString();

                receiver = binding.receiverEditText.getText().toString();
                if (!PHONE_NUMBER_PATTERN.matcher(receiver).matches()) {
                    Toast.makeText(getApplicationContext(), "올바른 휴대폰 번호 형식이 아닙니다..!\n(예시: 01012345678)", Toast.LENGTH_LONG).show();
                    return ;
                }

                receiver = "+82" + receiver.substring(1);
                if (sender.equals(receiver)) {
                    Toast.makeText(getApplicationContext(), "스스로에게 보내는 편지는 이미 전달됐습니다..!", Toast.LENGTH_LONG).show();
                    return ;
                }

                Toast.makeText(getApplicationContext(), "편지에 당신의 마음을 연결했습니다..!", Toast.LENGTH_LONG).show();
                
                onSaveClicked(mDatabase);
            }
        });
    }

    private void onSaveClicked(DatabaseReference mDatabaseRef) {

        mDatabaseRef.child("saved").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                exchangeFlag = false;

                tempLettersA.clear();
                tempLettersB.clear();
                exchangeLettersA.clear();
                exchangeLettersB.clear();

                lettersA = mutableData.child(sender).getValue(t);
                if (lettersA == null) {
                    lettersA = new ArrayList<>();
                }
                lettersB = mutableData.child(receiver).getValue(t);
                if (lettersB == null) {
                    lettersB = new ArrayList<>();
                }

                Letter letter = new Letter(sender, text);
                lettersB.add(letter);

                for (int i=0; i<lettersA.size(); i++) {
                    if (lettersA.get(i).getSender().equals(receiver)) {
                        exchangeFlag = true;
                        Letter letterA = new Letter(receiver, lettersA.get(i).getText());
                        tempLettersA.add(letterA);
                        lettersA.remove(i);
                        i--;
                    }
                }
                mutableData.child(sender).setValue(lettersA);

                if (!exchangeFlag) {
                    mutableData.child(receiver).setValue(lettersB);
                    return Transaction.success(mutableData);
                }

                for (int i=0; i<lettersB.size(); i++) {
                    if (lettersB.get(i).getSender().equals(sender)) {
                        Letter letterB = new Letter(sender, lettersB.get(i).getText());
                        tempLettersB.add(letterB);
                        lettersB.remove(i);
                        i--;
                    }
                }
                mutableData.child(receiver).setValue(lettersB);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot currentData) {
                Log.d(TAG, "Upload Transaction: onComplete: " + databaseError);

                mDatabaseRef.child("exchanged").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                        if (!exchangeFlag) {
                            return Transaction.success(mutableData);
                        }

                        exchangeLettersA = mutableData.child(sender).getValue(t);
                        if (exchangeLettersA == null) {
                            exchangeLettersA = new ArrayList<>();
                        }
                        exchangeLettersB = mutableData.child(receiver).getValue(t);
                        if (exchangeLettersB == null) {
                            exchangeLettersB = new ArrayList<>();
                        }

                        exchangeLettersA.addAll(tempLettersA);
                        exchangeLettersB.addAll(tempLettersB);

                        mutableData.child(sender).setValue(exchangeLettersA);
                        mutableData.child(receiver).setValue(exchangeLettersB);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean committed,
                                           DataSnapshot currentData) {
                        Log.d(TAG, "Exchange Transaction: onComplete: " + databaseError);
                    }
                });
            }
        });
    }
}


