package com.android.queue.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract;
import com.android.queue.models.Participant;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>{

    private List<Participant> mListParticipant;
    private SessionManager sessionManager;
    private Context mContext;

    public ParticipantAdapter(List<Participant> mListParticipant) {
        this.mListParticipant = mListParticipant;
    }
    public ParticipantAdapter(Context context,List<Participant> mListParticipant) {
        this.mListParticipant = mListParticipant;
        mContext=context;
        sessionManager = new SessionManager(mContext);
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant,parent,false);
        //ParticipantViewHolder participantViewHolder= new ParticipantViewHolder(view);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        String waiterPhone=sessionManager.getUserData().getString(QueueDatabaseContract.UserEntry.PHONE_ARM);
        Participant participant=mListParticipant.get(position);
        if(participant==null){
            return;
        }
        if(participant.waiterNumber!=-1){
            if(position==0){
                holder.tv_Name.setText(participant.getWaiterName());
                holder.sTT.setText("STT:"+(position+1));
                holder.state.setText("Đang xử lý");
            }else{
                holder.tv_Name.setText(participant.getWaiterName());
                holder.sTT.setText("STT:"+(position+1));
                holder.state.setText("Đang chờ  ");
            }
            if(participant.waiterPhone.equals(waiterPhone)){
                holder.linearLayout.setBackgroundColor(Color.parseColor("#2A9D8F"));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mListParticipant != null) {
            return mListParticipant.size();
        }
        return 0;
    }

    public int getIsWaiterCount(){
        if(mListParticipant!=null){
            int i=0;
            for (Participant temp:mListParticipant) {
                if(temp.waiterNumber!=-1)
                    i++;
            }
            return i;
        }
        return 0;
    }

    public class ParticipantViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_Name;
        private TextView sTT;
        private TextView state;
        private LinearLayout linearLayout;


        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_Name = itemView.findViewById(R.id.tv_name);
            sTT = itemView.findViewById(R.id.soTT);
            state = itemView.findViewById(R.id.stateTv);
            linearLayout= itemView.findViewById(R.id.linearLayout);
        }
    }
}
