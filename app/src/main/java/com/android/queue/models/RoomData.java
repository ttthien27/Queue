package com.android.queue.models;

public class RoomData {
    public String roomName;
    public Long createDate;
    public String address;
    public Long timeStart;
    public Long maxParticipant;
    public String qr;
    public Double timeWait;
    public Double timeDelay;
    public String waitSetting;
    public Double latitude;
    public Double longitude;
    public Long currentWait = 1L;
    public String hostPhone;
    public Long totalLeft = 0L;
    public Long totalSkip = 0L;
    public Long totalDone = 0L;
    public Long totalParticipant = 0L;
    public Boolean isPause = false;
    public Boolean isClose = false;

    public RoomData() {}

    public RoomData(String roomName, String address, Long timeStart,
                    Long maxParticipant, Double timeWait, Double timeDelay,
                    String waitSetting, Double latitude, Double longitude, String hostPhone) {
        this.roomName = roomName;
        this.createDate = System.currentTimeMillis() / 1000;
        this.address = address;
        this.timeStart = timeStart;
        this.maxParticipant = maxParticipant;
        this.timeWait = timeWait;
        this.timeDelay = timeDelay;
        this.waitSetting = waitSetting;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hostPhone = hostPhone;
    }

}
