package com.android.queue.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public RoomData roomData;
    public List<Participant> participantList;

    public Room(String roomName, String address, Long timeStart, Long maxParticipant,
                 Double timeWait, Double timeDelay, String waitSetting,
                Double latitude, Double longitude, String hostPhone, List<Participant> participantList) {

        this.roomData = new RoomData(roomName, address, timeStart, maxParticipant,
                timeWait, timeDelay, waitSetting, latitude, longitude, hostPhone);
        this.participantList = participantList;
        this.roomData.totalParticipant = (long)participantList.size();
    }

    public Room(String roomName, String address, Long timeStart, Long maxParticipant,
                 Double timeWait, Double timeDelay, String waitSetting,
                Double latitude, Double longitude, String hostPhone) {
        this.roomData = new RoomData(roomName, address, timeStart, maxParticipant,
                timeWait, timeDelay, waitSetting, latitude, longitude, hostPhone);
        participantList = new ArrayList<>();
    }

    public Room(){
        this.roomData = new RoomData();
        this.participantList = new ArrayList<>();
    }
}
