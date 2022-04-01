package com.android.queue.activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.UserAccountsRequester;
import com.android.queue.models.UserAccounts;
import com.android.queue.utils.MD5Encode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.android.queue.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout phoneTv;
    TextInputLayout nameTv;
    TextInputLayout passwordTv;
    TextInputLayout re_passwordTv;
    private MaterialButton regBtn;
    private UserAccountsRequester userAccountsRequester;


    public static boolean validData = true ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        phoneTv = findViewById(R.id.phoneTv);
        nameTv = findViewById(R.id.nameTv);
        passwordTv = findViewById(R.id.passwordTv);
        re_passwordTv = findViewById(R.id.re_passwordTv);
        regBtn = findViewById(R.id.regBtn);
        regBtn.setEnabled(false);

        phoneTv.getEditText().addTextChangedListener(textWatcher);
        nameTv.getEditText().addTextChangedListener(textWatcher);
        passwordTv.getEditText().addTextChangedListener(textWatcher);
        re_passwordTv.getEditText().addTextChangedListener(textWatcher);

        userAccountsRequester = new UserAccountsRequester(this);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRegister();
            }
        });
    }

    //Validate
    private TextWatcher textWatcher = new TextWatcher() {

        boolean a = false;
        boolean b = false;
        boolean c = false;
        boolean d = false;
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (phoneTv.getEditText().getText().toString().length()==0) {
                phoneTv.setError("Số điện thoại không được để trống");
                phoneTv.setEnabled(true);
                a = false;
            }
            else if (!phoneTv.getEditText().getText().toString().matches("[0-9]+") || phoneTv.getEditText().getText().length() != 10) {
                a = false;
                phoneTv.setError("Số điện thoại không hợp lệ");
            }
            else{
                Query query = userAccountsRequester.getmDatabase().orderByChild("phone").equalTo(phoneTv.getEditText().getText().toString().trim());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            a = false;
                            phoneTv.setError("Số điện thoại đã được đăng kí");
                            phoneTv.requestFocus();
                        } else {
                            a = true;
                            phoneTv.setError(null);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            if (nameTv.getEditText().getText().toString().matches("")) {
                nameTv.setError("Họ tên không được để trống");
                b = false;
            } else {
                b = true;
                nameTv.setError(null);
            }
            if (passwordTv.getEditText().getText().toString().matches("")){
                passwordTv.setError("Mật khẩu không được để trống");
                c = false;
            }
            else {
                c = true;

                passwordTv.setError(null);
            }
            if (passwordTv.getEditText().getText().length() < 6){
                passwordTv.setError("Mật khẩu tối thiểu phải 6 kí tự");
                c = false;
            }
            else {
                c = true;
                passwordTv.setError(null);
            }
            if (!re_passwordTv.getEditText().getText().toString().equals(passwordTv.getEditText().getText().toString())){
                re_passwordTv.setError("Mật khẩu không trùng khớp");
                d = false;
            }
            else {
                d = true;
                re_passwordTv.setError(null);
            }

            if (a && b && c && d){
                regBtn.setEnabled(true);
            }
            else {
                regBtn.setEnabled(false);

            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    private void onClickRegister() {
        String phone = phoneTv.getEditText().getText().toString();
        String name = nameTv.getEditText().getText().toString();
        String pass = passwordTv.getEditText().getText().toString();
        String re_pass = re_passwordTv.getEditText().getText().toString();

        sendUserToVerifyPhone(name, phone, pass);
        }






    private void sendUserToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sendUserToVerifyPhone(String name, String phone, String pass) {
        Intent intent = new Intent(RegisterActivity.this, VerifyPhoneNumber.class);
        intent.putExtra(QueueDatabaseContract.UserEntry.FULL_NAME_ARM,name);
        intent.putExtra(QueueDatabaseContract.UserEntry.PHONE_ARM,phone);
        intent.putExtra(QueueDatabaseContract.UserEntry.PASSWORD_ARM,pass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    void setEnable(boolean result){
        validData = result;
    }


}