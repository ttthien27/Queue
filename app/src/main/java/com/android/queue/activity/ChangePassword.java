package com.android.queue.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.UserAccountsRequester;
import com.android.queue.models.UserAccounts;
import com.android.queue.utils.MD5Encode;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChangePassword extends AppCompatActivity {
    private TextInputLayout passTv;
    private TextInputLayout re_passTv;
    private Button btnChangePass;
    private UserAccountsRequester userAccountsRequester;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        userAccountsRequester = new UserAccountsRequester(this);

        passTv = findViewById(R.id.passTv);
        re_passTv = findViewById(R.id.re_passTv);
        btnChangePass = findViewById(R.id.btnChangePass);
        btnChangePass.setEnabled(false);

        Intent intent = getIntent();
        mPhone = intent.getStringExtra("phone");
        System.out.println(mPhone);

        passTv.getEditText().addTextChangedListener(textWatcher);
        re_passTv.getEditText().addTextChangedListener(textWatcher);

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassWord();
            }
        });


    }

    private void changePassWord() {
        String pass = passTv.getEditText().getText().toString();
        userAccountsRequester.updateUserAccount(mPhone,pass);
        sendUserToLogin();
    }

    private  boolean check_pass;
    private  boolean check_re_pass;
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String pass = passTv.getEditText().getText().toString();
            String repass = re_passTv.getEditText().getText().toString();
            if(pass.length() ==0){
                check_pass = false;
                passTv.setError("Mật khẩu không được để trống");
            }
            else if(pass.length() < 6){
                check_pass = false;
                passTv.setError("Mật khẩu tối thiểu phải 6 kí tự");
            }
            else {
                check_pass = true;
                passTv.setError(null);
            }

            if (!pass.equals(repass)){
                check_re_pass = false;
                re_passTv.setError("Mật khẩu không trùng khớp");
            }
            else {
                check_re_pass = true;
                re_passTv.setError(null);
            }
            if (check_pass && check_re_pass){
                btnChangePass.setEnabled(true);
            }
            else {
                btnChangePass.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void sendUserToLogin() {
        Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
        intent.putExtra(QueueDatabaseContract.UserEntry.PHONE_ARM,mPhone);
        intent.putExtra(QueueDatabaseContract.UserEntry.PASSWORD_ARM,passTv.getEditText().getText().toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}