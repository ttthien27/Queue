package com.android.queue.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.RoomEntryRequester;
import com.android.queue.models.Participant;
import com.android.queue.models.RoomData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.RoomDataEntry;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.ParticipantListEntry;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class InputkeyActivity extends AppCompatActivity {

    private ImageButton scanBtn;
    private TextInputLayout txtKey;
    private static MaterialTextView roomName;
    private static MaterialTextView roomTotal;
    private Button btnJoin;
    private Button btnHome;
    private RoomEntryRequester roomEntryRequester;
    private DatabaseReference databaseReference;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputkey);
        scanBtn = findViewById(R.id.scanBtn);
        txtKey = findViewById(R.id.iKey);
        btnJoin = findViewById(R.id.joinBtn);
        btnHome = findViewById(R.id.homeBtn);
        roomName = findViewById(R.id.welcomeRoom);
        roomTotal = findViewById(R.id.statusRoom);



        Intent intent=getIntent();
        String str = intent.getStringExtra("Key");
        txtKey.getEditText().setText(str);

        if(str!=null){
            roomEntryRequester = new RoomEntryRequester(InputkeyActivity.this);
            databaseReference= roomEntryRequester.find(str);
        }
        setWelcomeRoomName(str);


        sessionManager = new SessionManager(this);


        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key= txtKey.getEditText().getText().toString();
                Bundle userData = sessionManager.getUserData();
                if (userData.getString(QueueDatabaseContract.UserEntry.CURRENT_ROOM_ARM, null) != null) {
                    /*if (userData.getBoolean(QueueDatabaseContract.UserEntry.IS_HOST_ARM, false)) {
                        Intent intent = new Intent(InputkeyActivity.this, HostActivity.class);
                        InputkeyActivity.this.startActivity(intent);
                    }else{*/
                    Intent intent = new Intent(InputkeyActivity.this, LinedUpActivity.class);
                    intent.putExtra("keyRoom", sessionManager.getUserData().getString(QueueDatabaseContract.UserEntry.CURRENT_ROOM_ARM));
                    startActivity(intent);
                    //}
                }
                if(key.length()!=0){
                    roomEntryRequester = new RoomEntryRequester(InputkeyActivity.this);
                    databaseReference= roomEntryRequester.find(key);
                    Query query = databaseReference.child("roomData");
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                RoomData thisRoom = snapshot.getValue(RoomData.class);
                                if (thisRoom.totalParticipant >= thisRoom.maxParticipant) {
                                    setWelcomeRoomName(key);
                                    Toast.makeText(InputkeyActivity.this, "Phòng chờ hiện tại đã bị đầy. Tổng số người trong phòng chờ là: "
                                            + thisRoom.totalParticipant, Toast.LENGTH_LONG).show();
                                } else if (thisRoom.isClose) {
                                    Toast.makeText(InputkeyActivity.this, "Phòng chờ hiện đang bị đóng bởi chủ phòng", Toast.LENGTH_SHORT).show();
                                } else if (thisRoom.isPause) {
                                    Toast.makeText(InputkeyActivity.this, "Phòng chờ hiện đang bị tạm dừng bởi chủ phòng", Toast.LENGTH_SHORT).show();
                                } else {
                                    //Add Partipant
                                    String waiterPhone=sessionManager.getUserData().getString(QueueDatabaseContract.UserEntry.PHONE_ARM);
                                    String waiterName=sessionManager.getUserData().getString(QueueDatabaseContract.UserEntry.FULL_NAME_ARM);
                                    String waiterState= "IsWait";
                                    Participant participant = new Participant(waiterPhone,waiterName,waiterState);
                                    roomEntryRequester.addParticipant(participant,key);
                                    //Chuyển qua activity xếp hàng
                                    Intent intent = new Intent(InputkeyActivity.this,LinedUpActivity.class);
                                    intent.putExtra("keyRoom",key);
                                    startActivity(intent);
                                }
                            }else{
                                roomName.setText("Phòng không tồn tại");
                                roomTotal.setText("");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(InputkeyActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    txtKey.setError("Khóa không được để trống");
                }
            }
        });


        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InputkeyActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(InputkeyActivity.this,ScanQRActivity.class);
                startActivity(intent);
            }
        });
    }


    public void setWelcomeRoomName(String str){
        if(str!=null){
            Query query = databaseReference.child("roomData");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String str1 = snapshot.child("roomName").getValue(String.class);
                            Long long1 = snapshot.child("totalParticipant").getValue(Long.class);
                            Long long2 = snapshot.child("maxParticipant").getValue(Long.class);
                            roomName.setText("Phòng bạn sẽ đến là: "+str1);
                            roomTotal.setText("SS: "+long1.toString()+"/"+long2.toString());
                        }else{
                            roomName.setText("Phòng không tồn tại");
                            roomTotal.setText("");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InputkeyActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
