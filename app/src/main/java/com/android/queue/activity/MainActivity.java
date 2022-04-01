package com.android.queue.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.UserAccountsRequester;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.UserEntry;

public class MainActivity extends AppCompatActivity {

    private MaterialTextView usernameTv;

    private MaterialButton logoutBtn;
    private MaterialButton createRoomBtn;
    private SessionManager sessionManager;
    private MaterialButton lineBtn;

    UserAccountsRequester userAccountsRequester;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Init session manager
        sessionManager = new SessionManager(this);

        userAccountsRequester = new UserAccountsRequester(this);
        if (sessionManager.isLogin()){
            //Init all view in this activity
            usernameTv = findViewById(R.id.usernameTv);
            logoutBtn = findViewById(R.id.logoutBtn);
            createRoomBtn = findViewById(R.id.hostBtn);
            lineBtn = findViewById(R.id.lineBtn);

            usernameTv.setText(sessionManager.getUserData().getString(UserEntry.FULL_NAME_ARM));

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userAccountsRequester.isLogin(sessionManager.getUserData().getString(UserEntry.PHONE_ARM),false);
                    sessionManager.clearUserData();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            });

            createRoomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            });


            lineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,InputkeyActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            });

        }
        else {
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If user is already create or join a room, navigate to room layout
        Bundle userData = sessionManager.getUserData();
        if (userData.getString(UserEntry.CURRENT_ROOM_ARM, null) != null) {
            if (userData.getBoolean(UserEntry.IS_HOST_ARM, false)) {
                Intent intent = new Intent(MainActivity.this, HostActivity.class);
                MainActivity.this.startActivity(intent);
            }else{
                Intent intent = new Intent(MainActivity.this,LinedUpActivity.class);
                intent.putExtra("keyRoom",sessionManager.getUserData().getString(QueueDatabaseContract.UserEntry.CURRENT_ROOM_ARM));
                startActivity(intent);
            }
        }
    }
}