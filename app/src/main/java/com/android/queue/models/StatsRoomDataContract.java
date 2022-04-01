package com.android.queue.models;

import java.util.Arrays;
import java.util.HashMap;

public class StatsRoomDataContract {

    static public String TOTAL_PARTICIPANT = "Số người xếp hàng: ";
    static public String TOTAL_LEFT = "Số người đã rời hàng: ";
    static public String TOTAL_DONE = "Số người xếp hàng đã xử lý: ";
    static public String TOTAL_SKIP = "Số người xếp hàng đã bỏ lượt: ";
    static public String TOTAL_WAIT = "Số người xếp hàng đang chờ: ";

    static public String [] collectionKey = new String[] {
            TOTAL_PARTICIPANT,
            TOTAL_LEFT,
            TOTAL_WAIT,
            TOTAL_DONE,
            TOTAL_SKIP
    };

    static public int getKeyIndex(String key) {
        return Arrays.asList(collectionKey).indexOf("key");
    }
    static public String getKey (int index) {
        return collectionKey[index];
    }


}
