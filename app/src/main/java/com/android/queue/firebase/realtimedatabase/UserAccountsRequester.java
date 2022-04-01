package com.android.queue.firebase.realtimedatabase;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.queue.models.Room;
import com.android.queue.models.UserAccounts;
import com.android.queue.utils.MD5Encode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserAccountsRequester {
    private DatabaseReference mDatabase;
    private Context mContext;
    private List<Boolean> stateLogin;


    public UserAccountsRequester(Context context){
        mDatabase = FirebaseDatabase
                .getInstance("https://queue-eb51b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();
        this.mContext = context;
        stateLogin = new ArrayList<>();
    }

    //Tạo một tài khoản
    public String createAnUserAccount(UserAccounts userAccount) {
        String accKey =  mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME).push().getKey();
        if (accKey != null) {
            mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME).child(accKey).setValue(userAccount);
            return accKey;
        }
        return null;
    }


    //Cập nhật
    public void updateUserAccount(String phone, String pass){
        String encrypt = MD5Encode.endCode(pass);
        Query query = mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME)
                .orderByChild(QueueDatabaseContract.UserEntry.PHONE_ARM)
                .equalTo(phone.trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        String key = user.getKey();
                        mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME)
                                .child(key).child(QueueDatabaseContract.UserEntry.PASSWORD_ARM)
                                .setValue(encrypt);
                        System.out.println("cập nhật thành công");
                    }
                } else {
                    System.out.println("cập nhật thất bại");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Function to find an account, return its DatabaseReference
     * **/
    public DatabaseReference find(String key) {
        return mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME).child(key);
    }

    public DatabaseReference getmDatabase(){
        return mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME);
    }

    //change State_Login on Firebase
    public void isLogin( String phone,boolean state){
        Query query = mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME)
                .orderByChild(QueueDatabaseContract.UserEntry.PHONE_ARM)
                .equalTo(phone.trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String key = dataSnapshot.getKey();
                        mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME)
                                .child(key).child(QueueDatabaseContract.UserEntry.IS_LOGIN_ARM)
                                .setValue(state);
                        //
//                        readData(new StateLoginFirebaseCallback() {
//                            @Override
//                            public void onCallBack(List<Boolean> list) {
//                                System.out.println("TEST: "+list.toString());
//                            }
//                        },phone);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



    //Test data
    public void initTestData(){
        UserAccounts userAccounts = new UserAccounts("User1","0987654321","abc123456");
        createAnUserAccount(userAccounts);
    }

//    public void readData(StateLoginFirebaseCallback stateLoginFirebaseCallback, String phone){
//        Query query = mDatabase.child(QueueDatabaseContract.UserEntry.ROOT_NAME)
//                .orderByChild(QueueDatabaseContract.UserEntry.PHONE_ARM)
//                .equalTo(phone.trim());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                        String key = dataSnapshot.getKey();
//                        boolean a = dataSnapshot.child(QueueDatabaseContract.UserEntry.IS_LOGIN_ARM).getValue(Boolean.class);
//                        stateLogin.add(a);
//                    }
//                    stateLoginFirebaseCallback.onCallBack(stateLogin);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
//
//    }
//
//    public  interface StateLoginFirebaseCallback{
//        void onCallBack(List<Boolean> list);
//    }


}
