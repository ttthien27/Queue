package com.android.queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.android.queue.firebase.realtimedatabase.QueueDatabaseContract.UserEntry;
import com.android.queue.models.UserAccounts;
import com.google.android.gms.maps.model.LatLng;

public class SessionManager {
    public static final String TAG = SessionManager.class.getName();
    private final SharedPreferences userSession;
    private final SharedPreferences.Editor userDataEditor;
    private final Context mContext;

    static final public String HOST_ROOM_LATITUDE = "hostRoomLatitude";
    static final public String HOST_ROOM_LONGITUDE = "hostRoomLongitude";
    static final public String HOST_ROOM_ADDRESS = "hostRoomAddress";

    public SessionManager(Context context) {
        mContext = context;
        userSession = mContext.getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userDataEditor = userSession.edit();
    }

    public void initUserSession(String phone, String fullName) {
        userDataEditor.putString(UserEntry.PHONE_ARM, phone);
        userDataEditor.putString(UserEntry.FULL_NAME_ARM, fullName);
        userDataEditor.commit();
    }

    //test init User
    public void initUserSession(UserAccounts userAccounts) {
        userAccounts.setLogin(true);
        userDataEditor.putString(UserEntry.PHONE_ARM, userAccounts.phone);
        userDataEditor.putString(UserEntry.FULL_NAME_ARM, userAccounts.fullName);
        userDataEditor.putString(UserEntry.IS_LOGIN_ARM, userAccounts.getLogin().toString());
        userDataEditor.commit();
    }

    public void initKeyAfterUserJoinRoom(String key){
        userDataEditor.putString(UserEntry.CURRENT_ROOM_ARM, key);
        userDataEditor.commit();
    }

    public void putUserCurrentRoomId(String currentRoomId, boolean isHost) {
        userDataEditor.putString(UserEntry.CURRENT_ROOM_ARM, currentRoomId);
        userDataEditor.putBoolean(UserEntry.IS_HOST_ARM, isHost);
        userDataEditor.commit();
    }

    public String getCurrentRoomKey() {
        return userSession.getString(UserEntry.CURRENT_ROOM_ARM, null);
    }


    public boolean isLogin() {
        String login = userSession.getString(UserEntry.IS_LOGIN_ARM, null);
        boolean log = Boolean.parseBoolean(login);
        return log;
    }

    public Bundle getUserData() {
        String phone = userSession.getString(UserEntry.PHONE_ARM, null);
        String fullName = userSession.getString(UserEntry.FULL_NAME_ARM, null);
        String currentRoomId = userSession.getString(UserEntry.CURRENT_ROOM_ARM, null);
        boolean isHost = userSession.getBoolean(UserEntry.IS_HOST_ARM, false);
        Bundle bundle = new Bundle();
        bundle.putString(UserEntry.PHONE_ARM, phone);
        bundle.putString(UserEntry.FULL_NAME_ARM, fullName);
        bundle.putString(UserEntry.CURRENT_ROOM_ARM, currentRoomId);
        bundle.putBoolean(UserEntry.IS_HOST_ARM, isHost);
        return bundle;
    }



    public void clearUserCurrentRoom() {
        userDataEditor.remove(UserEntry.CURRENT_ROOM_ARM);
        userDataEditor.putBoolean(UserEntry.IS_HOST_ARM, false);
        userDataEditor.commit();
    }

    public void clearKeyRoomAfterLeave(){
        userDataEditor.remove(UserEntry.CURRENT_ROOM_ARM);
        //userDataEditor.putBoolean(UserEntry.IS_HOST_ARM, false);
        userDataEditor.commit();
    }


    public void putHostRoomLocation(LatLng latLng, String address){
        putDouble(userDataEditor, HOST_ROOM_LATITUDE, latLng.latitude);
        putDouble(userDataEditor, HOST_ROOM_LONGITUDE, latLng.longitude);
        userDataEditor.putString(HOST_ROOM_ADDRESS, address);
        userDataEditor.commit();
    }

    public String getHostRoomAddress() {
        return userSession.getString(HOST_ROOM_ADDRESS, null);
    }

    public LatLng getHostRoomLatLng() {
        return new LatLng(getDouble(userSession, HOST_ROOM_LATITUDE, 0),
                getDouble(userSession, HOST_ROOM_LONGITUDE, 0));
    }

    public void clearHostRoomLocation() {
        userDataEditor.remove(HOST_ROOM_LONGITUDE);
        userDataEditor.remove(HOST_ROOM_LATITUDE);
        userDataEditor.remove(HOST_ROOM_ADDRESS);
        userDataEditor.commit();
    }

    public void clearUserData() {

        userDataEditor.clear();
        userDataEditor.commit();
    }

    //Helper function
    private void putDouble(SharedPreferences.Editor editor, final String key, final double value) {
        editor.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(SharedPreferences session, final String key, final double defaultValue) {
        return Double.longBitsToDouble(session.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
