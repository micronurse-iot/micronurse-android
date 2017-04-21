package org.micronurse.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.micronurse.Application;
import org.micronurse.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;

public class ScanQRCodeActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_SCAN_QR_CODE = 0x233;
    public static final int RESULT_CDOE_SCAN_QR_CODE = 0x2333;
    public static String BUNDLE_QR_CODE_STR = "QRCodeStr";

    @BindView(R.id.qrcode_view)
    QRCodeView qrCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        qrCodeView.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onScanQRCodeSuccess(String s) {
                Intent intent = new Intent();
                intent.putExtra(BUNDLE_QR_CODE_STR, s);
                setResult(RESULT_CDOE_SCAN_QR_CODE, intent);
                finish();
            }

            @Override
            public void onScanQRCodeOpenCameraError() {
                AlertDialog ad = new AlertDialog.Builder(ScanQRCodeActivity.this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.alert_error_open_camera)
                        .setCancelable(false)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
                ad.show();
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Application.checkPermission(this, new String[]{Manifest.permission.CAMERA});
        } else{
            qrCodeView.startCamera();
            qrCodeView.startSpotAndShowRect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            qrCodeView.startCamera();
            qrCodeView.startSpotAndShowRect();
        }else{
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        qrCodeView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
