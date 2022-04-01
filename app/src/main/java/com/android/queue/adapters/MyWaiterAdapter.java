package com.android.queue.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.queue.R;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.models.Participant;

import java.util.ArrayList;
import java.util.List;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.*;

public class MyWaiterAdapter extends ArrayAdapter<Participant> {

    private Context mContext;


    public MyWaiterAdapter(@NonNull Context context, @NonNull List<Participant> objects) {
        super(context, 0, objects);
        mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Create view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_participant, parent, false);
        }

        //Init view
        TextView waiterName = convertView.findViewById(R.id.waiterName);
        TextView waiterNumber = convertView.findViewById(R.id.waiterNumber);
        TextView waiterPhone = convertView.findViewById(R.id.waiterPhone);
        TextView waiterState = convertView.findViewById(R.id.waiterState);
        //BindView
        Participant participant = getItem(position);
        waiterName.setText(participant.waiterName);
        waiterPhone.setText(participant.waiterPhone);
        waiterNumber.setText(participant.waiterNumber + "");
        waiterState.setText(getStateWaiter(participant.waiterState));
        waiterState.setTextColor(ContextCompat.getColor(mContext, getStateWaiterColor(participant.waiterState)));

        return convertView;
    }

    private String getStateWaiter(String waiterStateData) {
        if (waiterStateData.equals(ParticipantListEntry.STATE_IS_DONE)) {
            return "Xong";
        } else if (waiterStateData.equals(ParticipantListEntry.STATE_IS_LEFT)) {
            return "Thoát";
        } else if (waiterStateData.equals(ParticipantListEntry.STATE_IS_SKIP)) {
            return "Rời";
        } else {
            return "Chờ";
        }
    }

    private int getStateWaiterColor(String waiterStateData) {
        if (waiterStateData.equals(ParticipantListEntry.STATE_IS_DONE)) {
            return R.color.reply_green_700;
        } else if (waiterStateData.equals(ParticipantListEntry.STATE_IS_LEFT)) {
            return R.color.reply_red_600;
        } else if (waiterStateData.equals(ParticipantListEntry.STATE_IS_SKIP)) {
            return R.color.reply_black_900;
        } else {
            return R.color.reply_orange_700;
        }
    }

}
