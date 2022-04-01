package com.android.queue.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.realtimedatabase.RoomEntryRequester;
import com.android.queue.models.RoomData;
import com.android.queue.utils.TimestampHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.RoomDataEntry;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HostRoomSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HostRoomSettingFragment extends Fragment {

    public static final String TAG = HostRoomSettingFragment.class.getName();

    //Init view
    private TextView timeStartTextView;
    private TextInputLayout maxParticipantTextInput;
    private Slider timeWaitSlider;
    private Slider timeDelaySlider;
    private MaterialTextView closeRoomBtn;
    private MaterialButton pauseRoomBtn;
    private TextInputLayout waitSettingInputLayout;
    private AutoCompleteTextView waitSettingTextView;

    //Init session manager and firebase service
    private SessionManager sessionManager;
    private RoomEntryRequester roomEntryRequester;

    //Init context and activity
    private Context mContext;
    private Activity mActivity;

    //Init model
    private RoomData thisRoom;

    //Data ref for this room
    private DatabaseReference currentRoomRef;

    //Init array string for wait setting
    private static final String[] WAIT_SETTING_VIEW_ITEM = new String[]{"Cân bằng", "Mặc định"};

    public static HostRoomSettingFragment newInstance() {
        HostRoomSettingFragment fragment = new HostRoomSettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        mContext = getContext();
        mActivity = getActivity();
        sessionManager = new SessionManager(mContext);
        roomEntryRequester = new RoomEntryRequester(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_host_room_setting, container, false);

        //Hook view
        timeStartTextView = view.findViewById(R.id.timeStartTv);
        timeDelaySlider = view.findViewById(R.id.timeDelaySlider);
        timeWaitSlider = view.findViewById(R.id.timeWaitSlider);
        maxParticipantTextInput = view.findViewById(R.id.maxParticipantTv);
        closeRoomBtn = view.findViewById(R.id.closeBtn);
        pauseRoomBtn = view.findViewById(R.id.pauseBtn);

        //Create layout for wait setting
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.spinner_item_layout, WAIT_SETTING_VIEW_ITEM);
        waitSettingTextView = view.findViewById(R.id.filled_exposed_dropdown);
        waitSettingTextView.setAdapter(adapter);


        //Đóng phòng, nếu trong phòng còn người xếp hàng, không cho đóng. Ngược lại, xác nhận lại việc đóng phòng,
        // khi đóng user sẽ quay lại trang chính và xóa session.
        closeRoomBtn.setOnClickListener(v -> {
            if (thisRoom != null) {
                if (thisRoom.totalParticipant > thisRoom.currentWait) {
                    Snackbar.make(view, "Người chờ còn trong phòng, không thể đóng", Snackbar.LENGTH_SHORT).show();
                } else {

                    MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(mContext)
                            .setTitle("Xác nhận đóng phòng")
                            .setMessage("Bạn có muốn đóng phòng. Khi đóng phòng bạn không thể quay lại phòng nữa.")
                            .setPositiveButton("Có", (dialog, which) -> {
                                currentRoomRef.child(RoomDataEntry.ROOT_NAME).child(RoomDataEntry.IS_CLOSE_ARM).setValue(true)
                                        .addOnSuccessListener(unused -> {
                                            sessionManager.clearUserCurrentRoom();
                                            mActivity.finish();
                                        }).addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            }).setNeutralButton("Không", (dialog, which) -> {

                            });
                    alert.show();
                }
            }
        });

        //Tạm dừng phòng, khi phòng chờ tạm dừng sẽ không cho người xếp hàng tham gia nữa.
        // Khi tạm chờ, chủ phòng phải nhập khoảng thời gian tạm chờ.
        // Khi phòng chờ đang tạm dùng, button này sẽ hiện thị dưới dạng và có chức năng mở phòng chờ.
        pauseRoomBtn.setOnClickListener(v -> {
            if (thisRoom != null) {
                //Nếu phòng chờ chưa bắt đầu, không thể tạm dừng
                if (thisRoom.timeStart >= System.currentTimeMillis()) {
                    Toast.makeText(mContext,
                            "Phòng chờ chưa đến thời gian bắt đầu. Không thể tạm dừng", Toast.LENGTH_LONG).show();
                } else {
                    //Nếu phòng chờ đang pause. Sẽ có chức năng mở phòng chờ.
                    if (thisRoom.isPause) {
                        currentRoomRef.child(RoomDataEntry.ROOT_NAME).child(RoomDataEntry.IS_PAUSE_ARM).setValue(false)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(mContext, "Mở phòng thành công", Toast.LENGTH_SHORT).show();
                                    pauseRoomBtn.setText("Tạm dừng");
                                }).addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    } else {
                        //Bật đồng hồ để user chọn khoảng thời gian tạm dừng
                        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(1)
                                .setMinute(10)
                                .setTitleText("Tạm dừng trong vòng")
                                .build();

                        picker.show(getParentFragmentManager(), TAG);
                        picker.addOnPositiveButtonClickListener(v1 -> {
                            //Lấy giờ tạm dừng
                            Timestamp timeStart = new Timestamp(System.currentTimeMillis() +
                                    TimeUnit.MINUTES.toMillis(picker.getMinute()) + TimeUnit.HOURS.toMillis(picker.getHour()));
                            //Update đồng bộ hai trường timeStart và isPause, chúng ta sẽ tạo một hashmap
                            HashMap<String, Object> updateField = new HashMap<>();
                            updateField.put(RoomDataEntry.IS_PAUSE_ARM, true);
                            updateField.put(RoomDataEntry.TIME_START_ARM, timeStart.getTime());
                            //Update lên firebase
                            currentRoomRef.child(RoomDataEntry.ROOT_NAME).updateChildren(updateField)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(mContext, "Đã tạm dừng phòng thành công", Toast.LENGTH_SHORT).show();
                                        pauseRoomBtn.setText("Tạm dừng");
                                    }).addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        });
                        picker.addOnCancelListener(dialog -> {

                        });

                    }
                }
            }
        });

        //Thêm key end event cho maxParticipant Input Edit Text, để khi user dừng nhập và ấn enter thì sẽ tự động update lên firebase
        final TextInputEditText maxParticipantEditText = (TextInputEditText) maxParticipantTextInput.getEditText();
        maxParticipantEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    long maxParticipant = Long.parseLong(maxParticipantEditText.getText().toString());
                    currentRoomRef.child(RoomDataEntry.ROOT_NAME)
                            .child(RoomDataEntry.MAX_PARTICIPANT_ARM)
                            .setValue(maxParticipant)
                            .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    return true;
                }
                return false;
            }
        });

        //Thêm event cho spinner TimeDelay  để cập nhật data lên firebase
        timeDelaySlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                currentRoomRef.child(RoomDataEntry.ROOT_NAME)
                        .child(RoomDataEntry.TIME_DELAY_ARM)
                        .setValue(slider.getValue())
                        .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        //Thêm event cho spinner TimeWait để cập nhật data lên firebase
        timeWaitSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                currentRoomRef.child(RoomDataEntry.ROOT_NAME)
                        .child(RoomDataEntry.TIME_WAIT_ARM)
                        .setValue(slider.getValue())
                        .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        //Thêm formatter cho slider
        timeWaitSlider.setLabelFormatter(value -> (int)value + " phút");
        timeDelaySlider.setLabelFormatter(value -> value + " phút");

        //Thêm event cho autocomplete textview waitSetting, để update lên firebase.
        waitSettingTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String waitSetting = getWaitSetting(WAIT_SETTING_VIEW_ITEM[position]);
                currentRoomRef.child(RoomDataEntry.ROOT_NAME)
                        .child(RoomDataEntry.WAIT_SETTING_ARM)
                        .setValue(waitSetting)
                        .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentRoomRef = roomEntryRequester.find(sessionManager.getCurrentRoomKey());
        currentRoomRef.child(RoomDataEntry.ROOT_NAME).addValueEventListener(eventListener);
    }

    private final ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            //Get data transferred from firebase with getValue method from dataSnapshot
            thisRoom = snapshot.getValue(RoomData.class);

            timeStartTextView.setText("Bắt đầu vào: " + TimestampHelper.toDatetime(thisRoom.timeStart));

            float timeWait = thisRoom.timeWait.floatValue();
            timeWaitSlider.setValue(timeWait);

            float timeDelay = thisRoom.timeDelay.floatValue();
            timeDelaySlider.setValue(timeDelay);

            maxParticipantTextInput.getEditText().setText(thisRoom.maxParticipant + "");

            waitSettingTextView.setText(WAIT_SETTING_VIEW_ITEM[getWaitIndex()], false);

            if (thisRoom.isPause) {
                pauseRoomBtn.setText("Mở phòng chờ");
            } else {
                pauseRoomBtn.setText("Tạm dừng");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(getView(), "Lỗi đường truyền không ổn định", Snackbar.LENGTH_LONG).show();
        }
    };

    private String getWaitSetting(String selectValue) {
        if (selectValue.equals(WAIT_SETTING_VIEW_ITEM[0])) {
            return RoomDataEntry.BALANCE_WAIT;
        } else {
            return RoomDataEntry.CONSTANT_WAIT;
        }
    }

    private int getWaitIndex() {
        if (thisRoom.waitSetting.equals(RoomDataEntry.CONSTANT_WAIT)) {
            return 1;
        } else {
            return 0;
        }
    }
}