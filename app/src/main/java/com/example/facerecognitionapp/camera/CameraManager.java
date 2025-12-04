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
    private ProcessCameraProvider cameraProvider; // 摄像头提供者
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
        // 向系统请求摄像头-异步-等待所有初始化
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                // 获取CameraProvider实例，拿到摄像头控制权
                cameraProvider = cameraProviderFuture.get();

                // 配置Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider()); // 把相机内容挂载到UI

                // 配置ImageAnalysis（用于实时分析每一帧）
                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer);

                // 选择前置摄像头LENS_FACING_FRONT
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // 先清理旧的绑定
                cameraProvider.unbindAll();

                // 绑定用例到Activity 
                camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                Log.d(TAG, "摄像头启动成功");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "摄像头启动失败", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * 停止摄像头
     */
    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Log.d(TAG, "摄像头已停止");
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
