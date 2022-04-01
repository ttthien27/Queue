package com.android.queue.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.firebase.realtimedatabase.RoomEntryRequester;
import com.android.queue.firebase.storage.FirebaseStorageRequester;
import com.android.queue.models.Room;
import com.android.queue.utils.InputFilterMinMax;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.RoomDataEntry;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.UserEntry;

import net.glxn.qrgen.android.QRCode;

import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CreateRoomActivity extends AppCompatActivity {
    public static final String TAG = CreateRoomActivity.class.getName();
    //Init all view in layout
    private TextInputLayout phoneInputLayout;
    private TextInputLayout roomNameInputLayout;
    private TextInputLayout addressInputLayout;
    private TextInputLayout timeStartInputLayout;
    private TextInputLayout timeWaitInputLayout;
    private TextInputLayout timeDelayInputLayout;
    private TextInputLayout maxParticipantInputLayout;
    private TextInputLayout waitSettingInputLayout;
    private AutoCompleteTextView waitSettingTextView;

    private MaterialButton createRoomBtn;
    private MaterialTextView backBtn;

    //Init array string for wait setting
    private static final String[] WAIT_SETTING_VIEW_ITEM = new String[]{"Cân bằng", "Mặc định"};

    //Init session manager
    private SessionManager sessionManager;

    //Init room requester data on firebase
    private RoomEntryRequester roomEntryRequester;

    //Init firebase cloud requester
    private FirebaseStorageRequester firebaseStorageRequester;
    //Init time start variable
    private int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        //Create session manager
        sessionManager = new SessionManager(this);
        //Create room entry requester on firebase
        roomEntryRequester = new RoomEntryRequester(this);
        firebaseStorageRequester = new FirebaseStorageRequester(this);

        //Find all view needed
        phoneInputLayout = findViewById(R.id.hostPhoneTv);
        roomNameInputLayout = findViewById(R.id.roomNameTv);
        addressInputLayout = findViewById(R.id.addressTv);
        timeStartInputLayout = findViewById(R.id.timeStartTv);
        timeDelayInputLayout = findViewById(R.id.timeDelayTv);
        timeWaitInputLayout = findViewById(R.id.timeWaitTv);
        maxParticipantInputLayout = findViewById(R.id.maxParticipantTv);
        waitSettingInputLayout = findViewById(R.id.waitSettingTv);
        createRoomBtn = findViewById(R.id.createRoomBtn);
        backBtn = findViewById(R.id.backBtn);

        //Add constraint min max for edit text timeWait and timeStart.
        // 0 - 120 minutes for timeWait
        timeWaitInputLayout.getEditText().setFilters(new InputFilter[] {new InputFilterMinMax(0, 120)});
        //0 - 60 minutes for timeDelay
        timeDelayInputLayout.getEditText().setFilters(new InputFilter[] {new InputFilterMinMax(0, 60)});

        //Bind data to phone text from user session
        phoneInputLayout.getEditText().setText(sessionManager.getUserData().getString(UserEntry.PHONE_ARM));

        //Create layout for wait setting
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, WAIT_SETTING_VIEW_ITEM);
        waitSettingTextView = findViewById(R.id.filled_exposed_dropdown);
        waitSettingTextView.setAdapter(adapter);

        //Create address picker in google map for address text view
        addressInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateRoomActivity.this, MapsActivity.class);
                CreateRoomActivity.this.startActivity(intent);
            }
        });


        //Create time picker for time start input layout
        timeStartInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker picker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(1)
                        .setMinute(10)
                        .setTitleText("Bắt đầu trong vòng")
                        .build();

                picker.show(getSupportFragmentManager(), TAG);
                picker.addOnPositiveButtonClickListener(v1 -> {
                    hour = picker.getHour();
                    minute = picker.getMinute();
                    String time = hour + " giờ " + minute + " phút nữa";
                    timeStartInputLayout.getEditText().setText(time);
                });
                picker.addOnCancelListener(dialog -> {

                });
            }
        });

        //On click back button. Click return back to the main activity
        backBtn.setOnClickListener(v -> {
            finish();
        });

        //On click create room button. Click to create a room and navigate to host activity.
        createRoomBtn.setOnClickListener(v -> {
            //Get value from layout
            String roomName = roomNameInputLayout.getEditText().getText().toString().trim();
            String hostPhone = phoneInputLayout.getEditText().getText().toString().trim();
            String address = addressInputLayout.getEditText().getText().toString().trim();
            LatLng roomLocation = sessionManager.getHostRoomLatLng();
            String waitSetting = getWaitSetting();
            Timestamp timeStart = new Timestamp(System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(minute) + TimeUnit.HOURS.toMillis(hour));

            //Valid data in layout before insert to firebase
            boolean checkData = true;
            if (roomName.matches("")) {
                roomNameInputLayout.setError("Tên phòng không thể để trống");
                 checkData = false;
            } else {
                roomNameInputLayout.setError("");
            }
            if (hostPhone.matches("")) {
                phoneInputLayout.setError("Số điện thoại không thể để trống");
                checkData = false;
            } else {
                phoneInputLayout.setError("");
            }
            if (address.matches("")) {
                addressInputLayout.setError("Địa chỉ phòng không thể để trống");
                checkData = false;
            } else {
                addressInputLayout.setError("");
            }
            if (checkNullEditText(timeDelayInputLayout.getEditText())) {
                timeWaitInputLayout.setError("Cần nhập thời gian chờ");
                checkData = false;
            } else {
                timeWaitInputLayout.setError("");
            }
            if (checkNullEditText(timeDelayInputLayout.getEditText())) {
                timeDelayInputLayout.setError("Cần nhập độ trễ");
                checkData = false;
            } else {
                timeDelayInputLayout.setError("");
            }
            if (checkNullEditText(maxParticipantInputLayout.getEditText())) {
                maxParticipantInputLayout.setError("Yêu cầu nhập số lượng người tối đa");
                checkData = false;
            } else {
                maxParticipantInputLayout.setError("");
            }
            if (checkNullEditText(timeStartInputLayout.getEditText())) {
                timeStartInputLayout.setError("Cần thiết lập thời gian bắt đầu");
                checkData = false;
            } else {
                timeStartInputLayout.setError("");
            }



            if (checkData) {
                Double timeWait = Double.parseDouble(timeWaitInputLayout.getEditText().getText().toString());
                Double timeDelay = Double.parseDouble(timeDelayInputLayout.getEditText().getText().toString());
                Long maxParticipant = Long.parseLong(maxParticipantInputLayout.getEditText().getText().toString());
                //Init room model
                Room addingRoom = new Room(roomName, address, timeStart.getTime(), maxParticipant,
                        timeWait, timeDelay, waitSetting, roomLocation.latitude, roomLocation.longitude,
                        hostPhone);
                //Call romm entry requester to insert data to firebase realtime
                String roomKey = roomEntryRequester.createARoom(addingRoom);
                if (roomKey == null) {
                    Toast.makeText(this, "Lỗi tạo room trên cloud. Vui lòng kiểm tra đường mạng", Toast.LENGTH_SHORT).show();
                } else {
                    //Generate a qrcode from roomKey
                    File qrcode = QRCode.from(roomKey).file();
                    //Upload QR image to firebase storage
                    firebaseStorageRequester.uploadFile(Uri.fromFile(qrcode), roomKey);
                    //Update qr file name of the room
                    roomEntryRequester.updateQrFileName(roomKey, roomKey);
                    //Update user session
                    sessionManager.putUserCurrentRoomId(roomKey, true);
                    //Navigate to HostActivity
                    Intent intent = new Intent(CreateRoomActivity.this, HostActivity.class);
                    intent.putExtra("firstCreate", true);
                    CreateRoomActivity.this.startActivity(intent);
                }
            }

        });

        //Set on click to clean error in text input layout
        phoneInputLayout.setOnClickListener(clearError);
        roomNameInputLayout.setOnClickListener(clearError);
        addressInputLayout.setOnClickListener(clearError);
        timeStartInputLayout.setOnClickListener(clearError);
        timeDelayInputLayout.setOnClickListener(clearError);
        timeWaitInputLayout.setOnClickListener(clearError);
        maxParticipantInputLayout.setOnClickListener(clearError);

    }

    private boolean checkNullEditText(EditText editText) {
        return editText.getText().toString().matches("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.getHostRoomAddress() != null) {
            addressInputLayout.getEditText().setText(sessionManager.getHostRoomAddress());
        }
    }

    private String getWaitSetting() {
        String selectValue = waitSettingTextView.getText().toString().trim();
        if (selectValue.equals(WAIT_SETTING_VIEW_ITEM[0])) {
            return RoomDataEntry.BALANCE_WAIT;
        } else {
            return RoomDataEntry.CONSTANT_WAIT;
        }
    }


    private final View.OnClickListener clearError = v -> ((TextInputLayout) v).setError("");
}