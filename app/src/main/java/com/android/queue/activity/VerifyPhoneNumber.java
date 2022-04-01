package com.android.queue.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.UserAccountsRequester;
import com.android.queue.models.UserAccounts;
import com.android.queue.utils.MD5Encode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private UserAccountsRequester userAccountsRequester;

    private String TAG = "firebase - REGISTER";
    private String mVerificationId;

    EditText edtNum1, edtNum2, edtNum3, edtNum4, edtNum5, edtNum6;
    TextView edtPhone;
    Button btnSendOTP;
    Button btnVerify;

    String name_intent;
    String phone_intent;
    String pass_intent;

    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
        mAuth = FirebaseAuth.getInstance();

        edtNum1 = findViewById(R.id.edtNum1);
        edtNum2 = findViewById(R.id.edtNum2);
        edtNum3 = findViewById(R.id.edtNum3);
        edtNum4 = findViewById(R.id.edtNum4);
        edtNum5 = findViewById(R.id.edtNum5);
        edtNum6 = findViewById(R.id.edtNum6);
        edtPhone = findViewById(R.id.edtPhone);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnVerify = findViewById(R.id.btnVerify);

        userAccountsRequester = new UserAccountsRequester(this);



        Intent intent = getIntent();
         name_intent = intent.getStringExtra(QueueDatabaseContract.UserEntry.FULL_NAME_ARM);
         phone_intent = intent.getStringExtra(QueueDatabaseContract.UserEntry.PHONE_ARM);
         pass_intent = intent.getStringExtra(QueueDatabaseContract.UserEntry.PASSWORD_ARM);

         //
        String cv_phone = phone_intent.replace("0","+84");

        edtPhone.setText(phone_intent);
        Toast.makeText(getApplicationContext(), name_intent+phone_intent+pass_intent, Toast.LENGTH_SHORT).show();

        mPhone = cv_phone;


        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Vui lòng đợi", Toast.LENGTH_SHORT).show();
                sendVerificationCode(mPhone);
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = edtNum1.getText().toString() +
                        edtNum2.getText().toString() +
                        edtNum3.getText().toString() +
                        edtNum4.getText().toString() +
                        edtNum5.getText().toString() +
                        edtNum6.getText().toString();
                verifyCode(otp);
            }
        });
    }

    //đăng nhập bằng sđt
    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // số điện thoại cần xác thực
                        .setTimeout(60L, TimeUnit.SECONDS) //thời gian timeout
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback xác thực sđt
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            //Hàm này được gọi trong hai trường hợp:
            //1. Trong một số trường hợp, điện thoại di động được xác minh tự động mà không cần mã xác minh.
            //2. Trên một số thiết bị, các dịch vụ của Google Play phát hiện SMS đến và thực hiện quy trình xác minh mà không cần người dùng thực hiện bất kỳ hành động nào.
//            Log.d(TAG, "onVerificationCompleted:" + credential);
//            Toast.makeText(VerifyPhoneNumber.this, credential.getSmsCode(),Toast.LENGTH_SHORT).show();
//            System.out.println(credential.getSmsCode());
            //tự động điền mã OTP
//            edtNum1.setText(credential.getSmsCode().substring(0,1));
//            edtNum2.setText(credential.getSmsCode().substring(1,2));
//            edtNum3.setText(credential.getSmsCode().substring(2,3));
//            edtNum4.setText(credential.getSmsCode().substring(3,4));
//            edtNum5.setText(credential.getSmsCode().substring(4,5));
//            edtNum6.setText(credential.getSmsCode().substring(5,6));

//            verifyCode(credential.getSmsCode());
        }

        //fail
        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w(TAG, "onVerificationFailed", e);
            Toast.makeText(VerifyPhoneNumber.this, "Thất bại",Toast.LENGTH_SHORT).show();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(VerifyPhoneNumber.this, "yêu cầu thất bại",Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(VerifyPhoneNumber.this, "Quota không đủ",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "onCodeSent:" + verificationId);
            Toast.makeText(getApplicationContext(), "Đã gửi OTP", Toast.LENGTH_SHORT).show();
            mVerificationId = verificationId;
            mResendToken = token;
        }
    };

    //code xác thực OTP
    private void verifyCode(String code) {
//        Toast.makeText(MainActivity.this, "Đang xác thực",Toast.LENGTH_SHORT).show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
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
                            String encrypts = MD5Encode.endCode(pass_intent);
                            UserAccounts userAccounts = new UserAccounts(name_intent, phone_intent,encrypts);
                            String accKey = userAccountsRequester.createAnUserAccount(userAccounts);
                            if (accKey != null){
                                Toast.makeText(VerifyPhoneNumber.this,"Đăng kí thành công", Toast.LENGTH_SHORT).show();
                                sendUserToLogin();
                            }
                            else{
                                Toast.makeText(VerifyPhoneNumber.this,"Đăng kí thất bại", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(VerifyPhoneNumber.this, "Thành công",Toast.LENGTH_SHORT).show();

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(VerifyPhoneNumber.this, "Lỗi",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendUserToLogin() {
        Intent intent = new Intent(VerifyPhoneNumber.this, LoginActivity.class);
        intent.putExtra(QueueDatabaseContract.UserEntry.PHONE_ARM,phone_intent);
        intent.putExtra(QueueDatabaseContract.UserEntry.PASSWORD_ARM,pass_intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}