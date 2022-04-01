package com.android.queue.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.UserAccountsRequester;
import com.android.queue.models.UserAccounts;
import com.android.queue.utils.MD5Encode;
import com.android.queue.utils.NotificationDevice;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private MaterialButton loginBtn;
    private TextView regTv;
    private TextView forgotPassTv;
    TextInputLayout phoneTv;
    TextInputLayout passwordTv;
    UserAccountsRequester userAccountsRequester;
    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Init all view in this activity
        phoneTv = findViewById(R.id.phoneTv);
        passwordTv = findViewById(R.id.passwordTv);
        loginBtn = findViewById(R.id.loginBtn);
        regTv = findViewById(R.id.regTv);
        forgotPassTv = findViewById(R.id.forgotPassTv);

        Intent intent = getIntent();
        if (intent!=null){
            phoneTv.getEditText().setText(intent.getStringExtra(QueueDatabaseContract.UserEntry.PHONE_ARM));
            passwordTv.getEditText().setText(intent.getStringExtra(QueueDatabaseContract.UserEntry.PASSWORD_ARM));
        }

        phoneTv.getEditText().addTextChangedListener(textWatcher);
        passwordTv.getEditText().addTextChangedListener(textWatcher);

        userAccountsRequester = new UserAccountsRequester(this);
        sessionManager = new SessionManager(this);

//        //test noti
//        NotificationDevice.headsUpNotification(LoginActivity.this);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogin();
            }
        });
        regTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });
        forgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                LoginActivity.this.startActivity(intent);
            }
        });
    }

    //Validate
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(phoneTv.getEditText().getText().length() ==0){
                phoneTv.setError("Số điện thoại không được để trống");
            }
            else if (!phoneTv.getEditText().getText().toString().matches("[0-9]+") || phoneTv.getEditText().getText().length() != 10){
                phoneTv.setError("Số điện thoại không hợp lệ");
            }
            else {
                phoneTv.setError(null);
            }
            if(passwordTv.getEditText().getText().length() ==0){
                passwordTv.setError("Mật khẩu không được để trống");
            }
            else {
                passwordTv.setError(null);
            }
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private void onClickLogin(){
        String phone = phoneTv.getEditText().getText().toString();
        String pass = passwordTv.getEditText().getText().toString();
        String encrypts = MD5Encode.endCode(pass);
//        userAccountsRequester.isLogin(phone);

        DatabaseReference databaseReference = userAccountsRequester.getmDatabase();
        Query query = databaseReference.orderByChild(QueueDatabaseContract.UserEntry.PHONE_ARM).equalTo(phone.trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        UserAccounts userAccounts = user.getValue(UserAccounts.class);
                        if (userAccounts.getLogin() == true){
                            Toast.makeText(LoginActivity.this,"Tài khoản đã được đăng nhập trên một thiết bị khác", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (userAccounts.password.equals(encrypts)) {
                                userAccountsRequester.isLogin(phone,true);
                                Toast.makeText(LoginActivity.this, " Đăng nhập thành công", Toast.LENGTH_LONG).show();
                                sessionManager.initUserSession(userAccounts);
                                sendUserToHome();
                            } else {
                                passwordTv.setError("Sai mật khẩu");
                                passwordTv.requestFocus();
                            }
                        }
                    }
                } else {
                    phoneTv.setError("Tài khoản không tồn tại");
                    phoneTv.requestFocus();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }





    private void sendUserToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}