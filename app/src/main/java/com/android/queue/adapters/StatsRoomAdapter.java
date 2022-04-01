package com.android.queue.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.queue.R;
import com.android.queue.models.StatsRoomDataContract;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StatsRoomAdapter extends BaseAdapter {
    private HashMap<String, String> mData;
    private String[] mKeys;
    private Context mContext;
    public StatsRoomAdapter(Context context, HashMap<String, String> data){
        mData  = data;
        mKeys = StatsRoomDataContract.collectionKey;
        mContext = context;
    }

    @Override
    public int getCount() {
        if (mData.isEmpty())
            return 0;
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(mKeys[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String key = mKeys[position];
        String value = getItem(position).toString();

        //Create view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.room_stats_item_view, parent, false);
        }

        //Bind value to view
        TextView keyTv = convertView.findViewById(R.id.key);
        TextView valueTv = convertView.findViewById(R.id.value);
        keyTv.setText(key);
        valueTv.setText(value);
        return convertView;
    }
}
