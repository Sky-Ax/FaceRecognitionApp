package com.example.facerecognitionapp.camera;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageAnalysis imageAnalysis;
    private Context context;

    public CameraManager(Context context) {
        this.context = context;
    }

    /**
     * 启动摄像头
     */
    public void startCamera(LifecycleOwner lifecycleOwner, PreviewView previewView, 
                           ImageAnalysis.Analyzer analyzer) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                // 获取CameraProvider
                cameraProvider = cameraProviderFuture.get();

                // 配置Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 配置ImageAnalysis（用于实时分析每一帧）
                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer);

                // 选择前置摄像头
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // 解绑所有用例
                cameraProvider.unbindAll();

                // 绑定用例到生命周期
                camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                Log.d(TAG, "Camera started successfully");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to start camera", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * 停止摄像头
     */
    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Log.d(TAG, "Camera stopped");
        }
    }

    /**
     * 获取当前摄像头
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * 获取CameraProvider
     */
    public ProcessCameraProvider getCameraProvider() {
        return cameraProvider;
    }
}
