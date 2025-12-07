package com.example.facerecognitionapp.model;

import android.graphics.Rect;

import java.util.List;

/**
 * 人脸检测结果数据模型
 */
public class FaceDetectionResult {
    public int faceCount;
    public List<FaceInfo> faces;
    public int imageWidth;
    public int imageHeight;
    public long timestamp;

    public FaceDetectionResult(int faceCount, List<FaceInfo> faces, int imageWidth, int imageHeight) {
        this.faceCount = faceCount;
        this.faces = faces;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 单个人脸信息
     */
    public static class FaceInfo {
        public Rect boundingBox;
        public float smileProbability;
        public float leftEyeOpenProbability;
        public float rightEyeOpenProbability;
        public float headEulerAngleX;
        public float headEulerAngleY;
        public float headEulerAngleZ;

        public FaceInfo(Rect boundingBox, float smileProbability,
                       float leftEyeOpenProbability, float rightEyeOpenProbability,
                       float headEulerAngleX, float headEulerAngleY, float headEulerAngleZ) {
            this.boundingBox = boundingBox;
            this.smileProbability = smileProbability;
            this.leftEyeOpenProbability = leftEyeOpenProbability;
            this.rightEyeOpenProbability = rightEyeOpenProbability;
            this.headEulerAngleX = headEulerAngleX;
            this.headEulerAngleY = headEulerAngleY;
            this.headEulerAngleZ = headEulerAngleZ;
        }
    }
}
