package com.android.queue.firebase.realtimedatabase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.queue.activity.LinedUpActivity;
import com.android.queue.models.Participant;
import com.android.queue.models.Room;
import com.android.queue.models.RoomData;
import com.android.queue.utils.TimestampHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.RoomDataEntry;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.ParticipantListEntry;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RoomEntryRequester {

    private DatabaseReference mDatabase;
    private Context mContext;

    public RoomEntryRequester(Context context) {
        mDatabase = FirebaseDatabase
                .getInstance("https://queue-eb51b-default-rtdb.asia-southeast1.firebasedatabase.app")//Khi khởi tạo nhớ thêm url này vào hàm này nhé.
                .getReference();
        this.mContext = context;
    }

    //Tạo một phòng chờ
    public String createARoom(Room room) {
        //Init a row in firebase realtime, get its id.
        String roomKey = mDatabase.child(RoomEntry.ROOT_NAME).push().getKey();
        if (roomKey != null) {
            //Add object to the new row that we have created.
            mDatabase.child(RoomEntry.ROOT_NAME).child(roomKey).setValue(room);
            return roomKey;
        }
        //Return null when failed to know that we init a row false.
        return null;
    }

    //Thêm một người xếp hàng vào trong list xếp hàng
    public void addParticipant(Participant participant, String roomKey) {
        DatabaseReference room = mDatabase.child(RoomEntry.ROOT_NAME).child(roomKey);
        room.child(RoomDataEntry.ROOT_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Get data transferred from firebase with getValue method from dataSnapshot
                RoomData thisRoom = snapshot.getValue(RoomData.class);
                //If thisRoom is not closed and paused by the host and not full. We add user to this room. Else we show message that this room's state.
                if (thisRoom.totalParticipant >= thisRoom.maxParticipant) {
                    Toast.makeText(mContext, "Phòng chờ hiện tại đã bị đầy. Tổng số người trong phòng chờ là: "
                            + thisRoom.totalParticipant, Toast.LENGTH_LONG).show();
                } else if (thisRoom.isClose) {
                    Toast.makeText(mContext, "Phòng chờ hiện đang bị đóng bởi chủ phòng", Toast.LENGTH_SHORT).show();
                } else if (thisRoom.isPause) {
                    Toast.makeText(mContext, "Phòng chờ hiện đang bị tạm dừng bởi chủ phòng", Toast.LENGTH_SHORT).show();
                } else {
                    participant.waiterNumber = thisRoom.totalParticipant + 1;
                    room.child(RoomEntry.PARTICIPANT_LIST_ARM).push().setValue(participant);
                    room.child(RoomEntry.ROOM_DATA_ARM).child(RoomDataEntry.TOTAL_PARTICIPANT_ARM).setValue(ServerValue.increment(1));
                }
            }
            @Override
            public void onCancelled(@NonNull  DatabaseError error) {
                Toast.makeText(mContext, "Lỗi: " + error.getDetails(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method to update a specific field data of a room
     **/
    public void update(String fieldName, Object data, String roomKey) {
        DatabaseReference thisRoom = find(roomKey);
        thisRoom.child(RoomDataEntry.ROOT_NAME).child(fieldName)
                .setValue(data)
                .addOnFailureListener(e -> Toast.makeText(mContext, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Method to update qr file name of a room
     **/
    public void updateQrFileName(String fileName, String roomKey) {
        DatabaseReference room = find(roomKey);
        room.child(RoomDataEntry.ROOT_NAME).child(RoomDataEntry.QR_ARM).setValue(fileName);
    }

    /**
     * Function to find a room, return its DatabaseReference
     **/
    public DatabaseReference find(String roomKey) {
        return mDatabase.child(RoomEntry.ROOT_NAME).child(roomKey);
    }


    //Giảm totalParticipant xau khi rời phòng hoặc được xử lý
    public void updateTotalParticipantafterChange(String roomKey) {
        DatabaseReference room = mDatabase.child(RoomEntry.ROOT_NAME).child(roomKey);
        room.child(RoomDataEntry.ROOT_NAME).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                RoomData thisRoom = dataSnapshot.getValue(RoomData.class);
                room.child(RoomEntry.ROOM_DATA_ARM).child(RoomDataEntry.TOTAL_PARTICIPANT_ARM).setValue(thisRoom.totalParticipant - 1);
            }
        });
    }


    //Tạo test data, chạy một lần thôi, đừng chạy nữa nha, chạy nữa không sao nhưng nó ra hai cái. Nhờ lên firebase xem data mình tạo sẵn nha, là chạy cái  này đấy.
    public void initTestData() {
        Participant participant1 = new Participant("0123456789", "Tester1", 1L, ParticipantListEntry.STATE_IS_SKIP);
        Participant participant2 = new Participant("0123456788", "Tester2", 2L, ParticipantListEntry.STATE_IS_DONE);
        Participant participant3 = new Participant("0123456787", "Tester3", 3L, ParticipantListEntry.STATE_IS_WAIT);
        Participant participant4 = new Participant("0123456786", "Tester4", 4L, ParticipantListEntry.STATE_IS_WAIT);


        List<Participant> participantList = new ArrayList<>();
        participantList.add(participant1);
        participantList.add(participant2);
        participantList.add(participant3);
        participantList.add(participant4);
        Timestamp timeStart = TimestampHelper.datetimeToTimestamp("2021-12-12 12:13:14");
        Room room = new Room("Test Room 1", "Address test 1", timeStart.getTime(), 100L,
                10D, 2D, RoomDataEntry.CONSTANT_WAIT, 10.801959752147356D, 106.71444878465739D,
                "0123789456", participantList);

        String keyRoom = createARoom(room);
        Log.d("RoomEntryRequester", "Key room test:  " + keyRoom);

    }
}
