package com.android.queue.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.activity.HostActivity;
import com.android.queue.adapters.StatsRoomAdapter;
import com.android.queue.firebase.realtimedatabase.RoomEntryRequester;
import com.android.queue.models.Participant;
import com.android.queue.models.Room;
import com.android.queue.models.RoomData;
import com.android.queue.models.StatsRoomDataContract;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.RoomDataEntry;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.ParticipantListEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HostRoomWaitingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HostRoomWaitingFragment extends Fragment {

    //Init view
    private MaterialTextView waiterNameTv;
    private MaterialTextView waiterPhoneTv;
    private MaterialTextView waiterNumberTv;
    private MaterialButton doneBtn;
    private MaterialTextView skipBtn;
    private TextView nextWaiterNameTv;
    private TextView nextWaterPhoneTv;
    private MaterialButton viewListParticipantBtn;
    private ListView statsRoomListView;

    //Init session and firebase service
    private SessionManager sessionManager;
    private RoomEntryRequester roomEntryRequester;

    //Init context and activity
    private Context mContext;
    private Activity mActivity;

    //Init model for this Room
    private Room thisRoom;

    //Init data reference for this room
    private DatabaseReference thisRoomReference;

    //Init data as a hashmap for room stats listview
    private HashMap<String, String> statsRoom;
    //Init adapter for list view
    private StatsRoomAdapter statsRoomAdapter;

    //Init current waiter Id
    private String currentWaiterId;

    public static HostRoomWaitingFragment newInstance() {
        HostRoomWaitingFragment fragment = new HostRoomWaitingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mActivity = getActivity();
        mContext = getContext();
        sessionManager = new SessionManager(mContext);
        roomEntryRequester = new RoomEntryRequester(mContext);
        thisRoomReference = roomEntryRequester.find(sessionManager.getCurrentRoomKey());
        thisRoom = new Room();

        //Init test data for this room
//        Participant participant1 = new Participant("0123456789", "Tester1", 1L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant2 = new Participant("0123456788", "Tester2", 2L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant3 = new Participant("0123456787", "Tester3", 3L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant4 = new Participant("0123456786", "Tester4", 4L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant5 = new Participant("0123456789", "Tester5", 5L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant6 = new Participant("0123456788", "Tester6", 6L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant7 = new Participant("0123456787", "Tester7", 7L, ParticipantListEntry.STATE_IS_WAIT);
//        Participant participant8 = new Participant("0123456786", "Tester8", 8L, ParticipantListEntry.STATE_IS_WAIT);
//        ArrayList<Participant> testData = new ArrayList<>();
//        testData.add(participant1); testData.add(participant2); testData.add(participant3); testData.add(participant4);
//        testData.add(participant5); testData.add(participant6); testData.add(participant7); testData.add(participant8);
//        thisRoomReference.child(ParticipantListEntry.ROOT_NAME).setValue(testData);
//        thisRoomReference.child(RoomDataEntry.ROOT_NAME).child(RoomDataEntry.TOTAL_PARTICIPANT_ARM).setValue(8);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_host_room_waiting, container, false);

        //Hook view
        waiterNameTv = view.findViewById(R.id.waiterNameTv);
        waiterPhoneTv = view.findViewById(R.id.waiterPhoneTv);
        waiterNumberTv = view.findViewById(R.id.waiterNumberTv);
        doneBtn = view.findViewById(R.id.doneBtn);
        skipBtn = view.findViewById(R.id.skipBtn);
        nextWaiterNameTv = view.findViewById(R.id.nextWaiterNameTv);
        nextWaterPhoneTv = view.findViewById(R.id.nextWaiterPhoneTv);
        viewListParticipantBtn = view.findViewById(R.id.viewListParticipantBtn);
        statsRoomListView = view.findViewById(R.id.statsRoomListView);

        //Create adapter and set to list view
        statsRoom = new HashMap<>();
        statsRoomAdapter = new StatsRoomAdapter(mContext, statsRoom);
        statsRoomListView.setAdapter(statsRoomAdapter);


        //Add roomData value listener to update view when the data is change in firebase
        thisRoomReference.child(RoomDataEntry.ROOT_NAME).addValueEventListener(roomDataValueListener);

        //Add participant list children listener. Also query top two current waiter
        thisRoomReference
                .child(ParticipantListEntry.ROOT_NAME)
                .orderByChild(ParticipantListEntry.WAITER_NUMBER_ARM)
                .addValueEventListener(waiterListener);

        //doneBtn on click, update current waiter to state isDone, and increase currentNumber Wait to 1;
        doneBtn.setOnClickListener(v -> {
            //Make sure that this room date was loaded from firebase
            if (currentWaiterId != null) {
                if (thisRoom.roomData.timeStart > System.currentTimeMillis()) {
                    Toast.makeText(mContext, "Chưa đến giờ bắt đầu hàng chờ", Toast.LENGTH_SHORT).show();
                } else if (thisRoom.roomData.currentWait <= thisRoom.roomData.totalParticipant) {
                    //Update current wait to 1
                    roomEntryRequester.update(RoomDataEntry.CURRENT_WAIT_ARM, thisRoom.roomData.currentWait + 1, sessionManager.getCurrentRoomKey());
                    thisRoom.roomData.currentWait += 1;
                    //Update waiter state
                    thisRoomReference
                            .child(ParticipantListEntry.ROOT_NAME)
                            .child(currentWaiterId)
                            .child(ParticipantListEntry.WAITER_STATE_ARM)
                            .setValue(ParticipantListEntry.STATE_IS_DONE)
                            .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } else {
                    Toast.makeText(mContext, "Đã hết người chờ trong phòng", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(mContext, "Chưa có người xếp hàng ở vị trí này", Toast.LENGTH_SHORT).show();
            }
        });

        //skipBtn on click, update current waiter to state isSkip, and increase currentNumber Wait to 1;
        skipBtn.setOnClickListener(v -> {
            //Make sure that this room date was loaded from firebase
            if (currentWaiterId != null) {
                if (thisRoom.roomData.timeStart > System.currentTimeMillis()) {
                    Toast.makeText(mContext, "Chưa đến giờ bắt đầu hàng chờ", Toast.LENGTH_SHORT).show();
                } else if (thisRoom.roomData.currentWait <= thisRoom.roomData.totalParticipant) {
                    //Update current wait to 1
                    roomEntryRequester.update(RoomDataEntry.CURRENT_WAIT_ARM, thisRoom.roomData.currentWait + 1, sessionManager.getCurrentRoomKey());
                    thisRoom.roomData.currentWait += 1;
                    //Update waiter state
                    thisRoomReference
                            .child(ParticipantListEntry.ROOT_NAME)
                            .child(currentWaiterId)
                            .child(ParticipantListEntry.WAITER_STATE_ARM)
                            .setValue(ParticipantListEntry.STATE_IS_SKIP)
                            .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } else {
                    Toast.makeText(mContext, "Đã hết người chờ trong phòng", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, "Chưa có người xếp hàng ở vị trí này", Toast.LENGTH_SHORT).show();
            }
        });

        viewListParticipantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HostActivity) mActivity)
                        .navigateTo(WaiterListFragment.newInstance(thisRoom.roomData.currentWait), false);
            }
        });

        return view;
    }


    //Value event listener to update room data
    private final ValueEventListener roomDataValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            thisRoom.roomData = snapshot.getValue(RoomData.class);
            //Put value into a hash map for the adapter of listview stats
            statsRoom.put(StatsRoomDataContract.TOTAL_PARTICIPANT, thisRoom.roomData.totalParticipant + "/" + thisRoom.roomData.maxParticipant);

            long totalwait = thisRoom.roomData.totalParticipant - thisRoom.roomData.currentWait;
            totalwait = totalwait > 0 ? totalwait : 0;
            statsRoom.put(StatsRoomDataContract.TOTAL_WAIT, totalwait + "");
            statsRoom.put(StatsRoomDataContract.TOTAL_DONE, thisRoom.roomData.totalDone + "");
            statsRoom.put(StatsRoomDataContract.TOTAL_SKIP, thisRoom.roomData.totalSkip + "");
            statsRoom.put(StatsRoomDataContract.TOTAL_LEFT, thisRoom.roomData.totalLeft + "");
            statsRoomAdapter.notifyDataSetChanged();

            //Set current wait text view
            if (thisRoom.roomData.totalParticipant > 0) {
                waiterNumberTv.setText(String.valueOf(thisRoom.roomData.currentWait));
            } else if (thisRoom.roomData.currentWait > thisRoom.roomData.totalParticipant) {
                waiterPhoneTv.setText("");
                waiterNameTv.setText("");
            }
            //If total waiter is 0, show message to host
            if (thisRoom.roomData.totalParticipant == 0) {
                waiterNumberTv.setText("0");
                Toast.makeText(mContext, "Phòng chờ hiện đang trống", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(mContext, "Lỗi: " + error.getDetails(), Toast.LENGTH_LONG).show();
        }
    };

    //even listener to update current participants.
    private final ValueEventListener waiterListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Log.d("TEST", "onDataChange: " + "Some thing change currentWait " + thisRoom.roomData.currentWait);
            thisRoom.participantList = new ArrayList<>();
            for (DataSnapshot waiter : snapshot.getChildren()) {
                Participant participant = waiter.getValue(Participant.class);
                if (participant.waiterNumber == thisRoom.roomData.currentWait) {
                    thisRoom.participantList.add(waiter.getValue(Participant.class));
                    //Assign current waiter for easily update later
                    currentWaiterId = waiter.getKey();
                } else if (thisRoom.participantList.size() == 1) {
                    thisRoom.participantList.add(waiter.getValue(Participant.class));
                }

            }


            if (thisRoom.participantList.size() > 0) {
                //Update current waiter into view
                waiterNameTv.setText(thisRoom.participantList.get(0).waiterName);
                waiterPhoneTv.setText(thisRoom.participantList.get(0).waiterPhone);
            } else {
                waiterNameTv.setText("");
                waiterPhoneTv.setText("");
            }

            //Update next waiter
            if (thisRoom.participantList.size() > 1) {
                nextWaiterNameTv.setText(thisRoom.participantList.get(1).waiterName);
                nextWaterPhoneTv.setText(thisRoom.participantList.get(1).waiterPhone);
            } else {
                nextWaiterNameTv.setText("");
                nextWaterPhoneTv.setText("");
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(mContext, "Lỗi: " + error.getDetails(), Toast.LENGTH_LONG).show();
        }
    };


}