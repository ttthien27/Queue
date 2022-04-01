package com.android.queue.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.adapters.MyWaiterAdapter;
import com.android.queue.firebase.realtimedatabase.RoomEntryRequester;
import com.android.queue.models.Participant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.RoomEntry.*;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WaiterListFragment extends Fragment {

    private CheckBox showAll;
    private AutoCompleteTextView filter;
    private ListView listView;
    private SessionManager sessionManager;
    private RoomEntryRequester roomEntryRequester;
    private DatabaseReference roomReference;
    private ArrayList<Participant> participantList;
    private MyWaiterAdapter adapter;

    private long currentWait;

    private static final String[] FILTER_ITEMS = new String[]{"Tất cả", "Đã xong", "Đang chờ", "Mất lượt", "Rời hàng"};

    public static WaiterListFragment newInstance(long currentWait) {
        WaiterListFragment fragment = new WaiterListFragment();
        Bundle args = new Bundle();
        args.putLong(RoomDataEntry.CURRENT_WAIT_ARM, currentWait);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentWait = getArguments().getLong(RoomDataEntry.CURRENT_WAIT_ARM);
        }

        sessionManager = new SessionManager(getContext());
        roomEntryRequester = new RoomEntryRequester(getContext());
        roomReference = roomEntryRequester.find(sessionManager.getCurrentRoomKey());
        roomReference.child(ParticipantListEntry.ROOT_NAME)
                .orderByChild(ParticipantListEntry.WAITER_NUMBER_ARM)
                .addValueEventListener(listListener);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_participant_list, container, false);
        showAll = view.findViewById(R.id.showAll);
        filter = view.findViewById(R.id.filled_exposed_dropdown);
        listView = view.findViewById(R.id.list);
        filter.setText("Tất cả", false);
        //Kick out adapter
        participantList = new ArrayList<>();
        adapter = new MyWaiterAdapter(view.getContext(), participantList);
        listView.setAdapter(adapter);

        //Create layout for wait setting
        ArrayAdapter<String> filterItemsAdapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item_layout, FILTER_ITEMS);
        filter.setAdapter(filterItemsAdapter);

        //Set onclick listener for filter
        filter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.clear();
                adapter.addAll(applyFilter(participantList));
                adapter.notifyDataSetChanged();
            }
        });

        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.clear();
                adapter.addAll(applyFilter(participantList));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Set on click listener for show all
        showAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.clear();
                adapter.addAll(applyFilter(participantList));
                adapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private ArrayList<Participant> applyFilter(ArrayList<Participant> participantList) {
        ArrayList<Participant> filterParticipantList;
        long startWaiter = showAll.isChecked() ? -10 : currentWait;
        String filterData = filter.getText().toString().trim();


        if (filterData.equals(FILTER_ITEMS[1])) {
            filterParticipantList = filterArray(ParticipantListEntry.STATE_IS_DONE, startWaiter, participantList);
        } else if (filterData.equals(FILTER_ITEMS[2])) {
            filterParticipantList = filterArray(ParticipantListEntry.STATE_IS_WAIT, startWaiter, participantList);
        } else if(filterData.equals(FILTER_ITEMS[3])) {
            filterParticipantList = filterArray(ParticipantListEntry.STATE_IS_SKIP, startWaiter, participantList);
        } else if (filterData.equals(FILTER_ITEMS[4])) {
            filterParticipantList = filterArray(ParticipantListEntry.STATE_IS_LEFT, startWaiter, participantList);
        } else {
            filterParticipantList = filterArray("", startWaiter, participantList);
        }

        return filterParticipantList;
    }

    private ArrayList<Participant> filterArray(String state, long startNumber, ArrayList<Participant> participantList) {
        ArrayList<Participant> filterParticipantList = new ArrayList<>();
        for (Participant p:
             participantList) {
            if (p.waiterNumber >= startNumber && (p.waiterState.equals(state) || state.isEmpty())) {
                filterParticipantList.add(p);
            }
        }
        return filterParticipantList;
    }

    private ValueEventListener listListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull  DataSnapshot snapshot) {
            participantList = new ArrayList<>();
            if (snapshot.getChildrenCount() > 0) {
                for (DataSnapshot waiter: snapshot.getChildren()) {
                    participantList.add(waiter.getValue(Participant.class));
                }
            }
            adapter.clear();
            adapter.addAll(applyFilter(participantList));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull  DatabaseError error) {
            Toast.makeText(getContext(), "Lỗi: " + error.getDetails(), Toast.LENGTH_LONG).show();
        }
    };
}
