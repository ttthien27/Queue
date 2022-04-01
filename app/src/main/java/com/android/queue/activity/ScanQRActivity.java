package com.android.queue.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.android.queue.R;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.firebase.database.annotations.NotNull;
import com.google.zxing.Result;

public class ScanQRActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    boolean CameraPermission = false;
    final int CAMERA_PERM = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this,scannerView);

        askPermission();
        if (CameraPermission)
        {
            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCodeScanner.startPreview();
                }
            });
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull @NotNull Result result) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ScanQRActivity.this, result.getText(), Toast.LENGTH_LONG).show();
                            Intent intent=new Intent(ScanQRActivity.this,InputkeyActivity.class);
                            intent.putExtra("Key",result.getText().toString());
                            startActivity(intent);
                        }
                    });
                }
            });
        }
    }


    //Hỏi quyền truy cập camera
    private void askPermission(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
                ActivityCompat.requestPermissions(ScanQRActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM);
            }else {
                mCodeScanner.startPreview();
                CameraPermission = true;
            }
        }
    }

    @Override
    protected void onPause() {
        if (CameraPermission){
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }
}