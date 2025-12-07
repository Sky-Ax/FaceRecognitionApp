package com.example.facerecognitionapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.example.facerecognitionapp.camera.CameraManager;
import com.example.facerecognitionapp.detection.ImageAnalyzer;
import com.example.facerecognitionapp.permission.PermissionManager;
import com.example.facerecognitionapp.ui.FaceOverlayView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CameraManager cameraManager;
    private PreviewView previewView;
    private FaceOverlayView faceOverlayView;
    private ImageAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置全屏显示
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        previewView = findViewById(R.id.previewView); // 相机预览
        faceOverlayView = findViewById(R.id.faceOverlayView); // 人脸UI
        cameraManager = new CameraManager(this);

        // 设置相机初始化监听器
        cameraManager.setInitializationListener((success, message) -> {
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, message);
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, message);
                }
            });
        });

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
        Log.d(TAG, "正在启动摄像头");
        analyzer = new ImageAnalyzer();

        // 设置帧分析监听器
        analyzer.setListener((faceCount, faces, width, height) -> {
            // 更新UI显示人脸检测结果
            runOnUiThread(() -> {
                faceOverlayView.updateFaces(faces, width, height);
            });
        });

        cameraManager.startCamera(this, previewView, analyzer);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "摄像头权限已授予");
                startCamera();
            } else {
                Log.d(TAG, "摄像头权限被拒绝");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (analyzer != null) {
            analyzer.release();
        }
        cameraManager.stopCamera();
    }
}
