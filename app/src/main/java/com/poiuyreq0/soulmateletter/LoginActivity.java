package com.poiuyreq0.soulmateletter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.poiuyreq0.soulmateletter.databinding.ActivityLoginBinding;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^010\\d{8}$");

    private ActivityLoginBinding binding;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("ko");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);

//                signInWithPhoneAuthCredential(credential);  // 이걸로 인해 인증번호를 입력하지 않아도 자동 인증됨
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                binding.codeEditText.setVisibility(View.VISIBLE);
                binding.verifyButton.setVisibility(View.VISIBLE);

                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhoneNumberVerification(binding.phoneEditText.getText().toString());
            }
        });

        binding.verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhoneNumberWithCode(mVerificationId, binding.codeEditText.getText().toString());
            }
        });


    }

    private void startPhoneNumberVerification(String phoneNumber) {

        Log.d(TAG, "startPhoneNumberVerification");

        if (!PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
            Toast.makeText(getApplicationContext(), "올바른 휴대폰 번호 형식이 아닙니다..!\n(예시: 01012345678)", Toast.LENGTH_LONG).show();
            return ;
        }

        String convertedNumber = "+82" + phoneNumber.substring(1);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(convertedNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        Log.d(TAG, "verifyPhoneNumberWithCode");

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            Intent intent = new Intent(getApplicationContext(), SoulmateActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(), "인증번호가 유효하지 않습니다..!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
