package com.example.facerecognitionapp.detection;

import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

public class SimpleImageAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "SimpleImageAnalyzer";

    @Override
    public void analyze(ImageProxy image) {
        // 这里可以处理每一帧图像
        // 目前只是简单地获取帧数据
        
        int width = image.getWidth();
        int height = image.getHeight();
        long timestamp = image.getImageInfo().getTimestamp();
        
        Log.d(TAG, "Frame: " + width + "x" + height + " timestamp: " + timestamp);
        
        // 必须关闭image，否则会导致后续帧无法获取
        image.close();
    }
}
