package com.example.facerecognitionapp.detection;

import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

/**
 * 人脸检测器 - 使用Google ML Kit
 */
public class FaceDetectorHelper {
    private static final String TAG = "FaceDetectorHelper";
    private final FaceDetector detector;
    private FaceDetectionListener listener;

    /**
     * 人脸检测监听器接口
     */
    public interface FaceDetectionListener {
        void onFacesDetected(List<Face> faces, int imageWidth, int imageHeight);
        void onDetectionError(Exception e);
    }

    public FaceDetectorHelper() {
        // 配置人脸检测选项
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                // PERFORMANCE_MODE_ACCURATE/PERFORMANCE_MODE_FAST 高精度/快速模式，高精度对性能不太友好
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // 快速模式
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)         // 检测所有关键点
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // 分类模式
                .setMinFaceSize(0.15f) // 表示只检测占图像 15% 以上的人脸
                .build();

        this.detector = FaceDetection.getClient(options);
    }

    /**
     * 设置人脸检测监听器
     */
    public void setListener(FaceDetectionListener listener) {
        this.listener = listener;
    }

    /**
     * 处理图像帧进行人脸检测
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    public void detectFaces(@NonNull ImageProxy imageProxy) {
        try {
            Image mediaImage = imageProxy.getImage();
            if (mediaImage == null) {
                imageProxy.close();
                return;
            }

            // 创建ML Kit输入图像
            InputImage image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            // 执行人脸检测
            detector.process(image)
                    .addOnSuccessListener(faces -> {
                        Log.d(TAG, "检测到 " + faces.size() + " 张人脸");
                        if (listener != null) {
                            listener.onFacesDetected(faces, imageProxy.getWidth(), imageProxy.getHeight());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "人脸检测失败", e);
                        if (listener != null) {
                            listener.onDetectionError(e);
                        }
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });

        } catch (Exception e) {
            Log.e(TAG, "处理图像异常", e);
            if (listener != null) {
                listener.onDetectionError(e);
            }
            imageProxy.close();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            detector.close();
        } catch (Exception e) {
            Log.e(TAG, "释放检测器失败", e);
        }
    }
}
