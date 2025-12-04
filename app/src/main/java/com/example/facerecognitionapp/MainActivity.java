package com.example.facerecognitionapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.example.facerecognitionapp.camera.CameraManager;
import com.example.facerecognitionapp.detection.SimpleImageAnalyzer;
import com.example.facerecognitionapp.permission.PermissionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CameraManager cameraManager;
    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        cameraManager = new CameraManager(this);

        // 检查摄像头权限
        if (PermissionManager.hasCameraPermission(this)) {
            startCamera();
        } else {
            // 请求权限
            PermissionManager.requestCameraPermission(this);
        }
    }

    /**
     * 启动摄像头
     */
    private void startCamera() {
        Log.d(TAG, "Starting camera");
        SimpleImageAnalyzer analyzer = new SimpleImageAnalyzer();
        cameraManager.startCamera(this, previewView, analyzer);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                startCamera();
            } else {
                Log.d(TAG, "Camera permission denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.stopCamera();
    }
}
