package com.android.queue.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.UserAccountsRequester;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class ForgotPassword extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private UserAccountsRequester userAccountsRequester;

    private String TAG = "firebase - FORGOT";
    private String mVerificationId;

    EditText edtNum1, edtNum2, edtNum3, edtNum4, edtNum5, edtNum6;
    TextInputLayout phoneTv;
    Button btnSendOTP;
    Button btnVerify;
    LinearLayout linearLayout;


    String name_intent;
    String phone_intent;
    String pass_intent;

    private String mPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth = FirebaseAuth.getInstance();
        userAccountsRequester = new UserAccountsRequester(this);
        edtNum1 = findViewById(R.id.edtNum1);
        edtNum2 = findViewById(R.id.edtNum2);
        edtNum3 = findViewById(R.id.edtNum3);
        edtNum4 = findViewById(R.id.edtNum4);
        edtNum5 = findViewById(R.id.edtNum5);
        edtNum6 = findViewById(R.id.edtNum6);

        phoneTv = findViewById(R.id.phoneTv);

        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnSendOTP.setEnabled(false);


        btnVerify = findViewById(R.id.btnVerify);
        linearLayout = findViewById(R.id.linear);

        phoneTv.getEditText().addTextChangedListener(textWatcher);

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Vui lòng đợi", Toast.LENGTH_SHORT).show();
                String cv_phone = phoneTv.getEditText().getText().toString().trim().replace("0","+84");
                mPhone = cv_phone;
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
                if (otp.length()==6){
                    verifyCode(otp);
                }
                else {
                    System.out.println("Vui long nhap ma OTP");
                }
            }
        });


    }

    private boolean check_valid;
    private boolean checkPhone;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(phoneTv.getEditText().getText().length() ==0){
                check_valid = false;
                phoneTv.setError("Số điện thoại không được để trống");
            }
            else if (!phoneTv.getEditText().getText().toString().matches("[0-9]+") || phoneTv.getEditText().getText().length() != 10){
                check_valid = false;
                phoneTv.setError("Số điện thoại không hợp lệ");
            }
            else {
                check_valid = true;
                Query query = userAccountsRequester.getmDatabase().orderByChild(QueueDatabaseContract.UserEntry.PHONE_ARM).equalTo(phoneTv.getEditText().getText().toString().trim());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            checkPhone = true;
                            phoneTv.setError(null);
                            linearLayout.setVisibility(View.VISIBLE);
                            btnVerify.setVisibility(View.VISIBLE);
                            String cv_phone = phoneTv.getEditText().getText().toString().trim().replace("0","+84");
                            mPhone = cv_phone;
                        } else {
                            checkPhone = false;
                            phoneTv.setError("Tài khoản không tồn tại!");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            Log.d(TAG, "onTextChanged: "+check_valid + "check phone "+ checkPhone);
            if (check_valid && !checkPhone ){
                btnSendOTP.setEnabled(true);
            }
            else {
                btnSendOTP.setEnabled(false);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

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
            Toast.makeText(ForgotPassword.this, "Thất bại",Toast.LENGTH_SHORT).show();
            System.out.println("That bai");

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(ForgotPassword.this, "yêu cầu thất bại",Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(ForgotPassword.this, "Quota không đủ",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "onCodeSent:" + verificationId);
            System.out.println("Đã gửi OTP");
            Toast.makeText(getApplicationContext(), "Đã gửi OTP", Toast.LENGTH_SHORT).show();

            mVerificationId = verificationId;
            mResendToken = token;
        }
    };

    //code xác thực OTP
    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            sendUserToChangePassword();

                            }
                        else {
                            System.out.println("Xac thuc that bai");
                        }

                    }
                });
    }

    private void sendUserToChangePassword() {
        Intent intent = new Intent(ForgotPassword.this, ChangePassword.class);
        intent.putExtra(QueueDatabaseContract.UserEntry.PHONE_ARM,mPhone.replace("+84","0"));
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