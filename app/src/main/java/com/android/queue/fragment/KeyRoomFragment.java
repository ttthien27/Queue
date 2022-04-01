package com.android.queue.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.queue.R;
import com.android.queue.SessionManager;
import com.android.queue.firebase.storage.FirebaseStorageRequester;
import com.android.queue.utils.ImageSaver;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KeyRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyRoomFragment extends Fragment {


    //Init view
    private ImageView qrCodeImgView;
    private TextView roomKeyTv;
    private ImageButton downloadBtn;

    //Init session manager
    private SessionManager sessionManager;

    //Init firebase storage requester
    private FirebaseStorageRequester firebaseStorageRequester;
    //Init image saver to help to write qr into user storager
    private ImageSaver imageSaver;
    //Init context and activity
    private Activity mActivity;
    private Context mContext;

    public KeyRoomFragment() {
        // Required empty public constructor
    }

    public static KeyRoomFragment newInstance() {
        KeyRoomFragment fragment = new KeyRoomFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        sessionManager = new SessionManager(getContext());
        firebaseStorageRequester = new FirebaseStorageRequester(getContext());
        imageSaver = new ImageSaver(getContext());
        mActivity = getActivity();
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_key_room, container, false);

        //Hook view
        qrCodeImgView = view.findViewById(R.id.qrcodeImg);
        roomKeyTv = view.findViewById(R.id.roomKeyTv);
        downloadBtn = view.findViewById(R.id.downloadBtn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String roomKey = sessionManager.getCurrentRoomKey();

        //Fetch QR code into view from firebase
        firebaseStorageRequester.loadRoomQrCode(qrCodeImgView, roomKey);

        //Fetch room key into text view
        roomKeyTv.setText(roomKey);


        //On click to download qr code
        downloadBtn.setOnClickListener(v -> {
            BitmapDrawable drawable = (BitmapDrawable) qrCodeImgView.getDrawable();
            Bitmap qrcode = drawable.getBitmap();

            if (ImageSaver.isExternalStorageWritable() && ImageSaver.checkPermission(mContext)) {
                imageSaver.setFileName(firebaseStorageRequester.initAnUniqueString() + ".png")
                        .setExternal(true)
                        .save(qrcode);

                Snackbar.make(view, "Mã qr đã được lưu thành công", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Xem mã", v1 -> {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setType("image/*");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }).show();
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }


        });

    }
}