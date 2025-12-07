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
import java.util.concurrent.Executors;

public class CameraManager {

    private static final String TAG = "CameraManager";
    private ProcessCameraProvider cameraProvider; // 摄像头提供者
    private Camera camera;
    private ImageAnalysis imageAnalysis;
    private Context context;
    private CameraInitializationListener initListener;

    /**
     * 相机初始化监听器接口
     */
    public interface CameraInitializationListener {
        void onCameraInitialized(boolean success, String message);
    }

    public CameraManager(Context context) {
        this.context = context;
    }

    /**
     * 设置相机初始化监听器
     */
    public void setInitializationListener(CameraInitializationListener listener) {
        this.initListener = listener;
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
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 只处理最新帧
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888) // ML Kit 适配格式
                        .build();
//                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer); // 不要放在主线程可能会影响程序运行
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer); // 分析器放到后台线程中进行

                // 选择摄像头：优先前置，不可用则使用后置
                CameraSelector cameraSelector = selectCamera(cameraProvider);
                if (cameraSelector == null) {
                    throw new Exception("设备无可用的前置/后置摄像头");
                }

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
                if (initListener != null) {
                    initListener.onCameraInitialized(true, "摄像头初始化成功");
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "摄像头启动异常", e);
                if (initListener != null) {
                    initListener.onCameraInitialized(false, "摄像头启动异常：" + e.getMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "摄像头启动失败：" + e.getMessage(), e);
                if (initListener != null) {
                    initListener.onCameraInitialized(false, "摄像头启动失败：" + e.getMessage());
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * 选择摄像头：优先前置，不可用则使用后置
     */
    private CameraSelector selectCamera(ProcessCameraProvider cameraProvider) {
        CameraSelector frontCamera = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        CameraSelector backCamera = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            // 缓存查询结果，避免重复调用 hasCamera()
            boolean hasFront = cameraProvider.hasCamera(frontCamera);
            boolean hasBack = cameraProvider.hasCamera(backCamera);

            if (hasFront) {
                Log.d(TAG, "使用前置摄像头");
                return frontCamera;
            } else if (hasBack) {
                Log.i(TAG, "前置摄像头不可用，切换到后置摄像头");
                return backCamera;
            } else {
                Log.e(TAG, "没有可用的摄像头");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "摄像头选择失败", e);
            return frontCamera;
        }
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
