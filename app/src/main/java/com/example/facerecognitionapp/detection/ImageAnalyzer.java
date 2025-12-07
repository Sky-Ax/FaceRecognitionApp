package com.example.facerecognitionapp.detection;

import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.face.Face;

import java.util.List;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "ImageAnalyzer";
    private final FaceDetectorHelper faceDetectorHelper;
    private FrameAnalysisListener listener;

    /**
     * 帧分析监听器接口
     */
    public interface FrameAnalysisListener {
        void onFrameAnalyzed(int faceCount, List<Face> faces, int width, int height);
    }

    public ImageAnalyzer() {
        this.faceDetectorHelper = new FaceDetectorHelper();
    }

    /**
     * 设置帧分析监听器
     */
    public void setListener(FrameAnalysisListener listener) {
        this.listener = listener;
        this.faceDetectorHelper.setListener(new FaceDetectorHelper.FaceDetectionListener() {
            @Override
            public void onFacesDetected(java.util.List<com.google.mlkit.vision.face.Face> faces, int imageWidth, int imageHeight) {
                Log.d(TAG, "检测到 " + faces.size() + " 张人脸");
                if (listener != null) {
                    listener.onFrameAnalyzed(faces.size(), faces, imageWidth, imageHeight);
                }
            }

            @Override
            public void onDetectionError(Exception e) {
                Log.e(TAG, "人脸检测错误", e);
            }
        });
    }

    @Override
    public void analyze(ImageProxy image) {
        try {
//            int width = image.getWidth();
//            int height = image.getHeight();
//            long timestamp = image.getImageInfo().getTimestamp();
//
//            Log.d(TAG, "处理帧 - 分辨率: " + width + "x" + height + " 时间戳: " + timestamp);

            // 使用ML Kit进行人脸检测
            faceDetectorHelper.detectFaces(image);

        } catch (Exception e) {
            Log.e(TAG, "分析帧异常", e);
            image.close();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        faceDetectorHelper.release();
    }
}
