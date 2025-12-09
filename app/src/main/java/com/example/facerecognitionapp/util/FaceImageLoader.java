package com.example.facerecognitionapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 人脸图片加载工具类
 * 从assets/face目录读取人脸图片并转换为InputImage
 */
public class FaceImageLoader {
    private static final String TAG = "FaceImageLoader";
    private static final String FACE_ASSETS_DIR = "face";
    private static final String FACE_PREFIX = "face_";

    /**
     * 人脸图片信息
     */
    public static class FaceImage {
        public String userName;      // 用户名（文件后缀）
        public InputImage inputImage; // ML Kit输入图像
        public Bitmap bitmap;         // 原始位图

        public FaceImage(String userName, InputImage inputImage, Bitmap bitmap) {
            this.userName = userName;
            this.inputImage = inputImage;
            this.bitmap = bitmap;
        }
    }

    /**
     * 从assets加载所有人脸图片
     */
    public static List<FaceImage> loadAllFaceImages(@NonNull Context context) {
        List<FaceImage> faceImages = new ArrayList<>();

        try {
            // 获取assets/face目录下的所有文件
            String[] files = context.getAssets().list(FACE_ASSETS_DIR);
            if (files == null || files.length == 0) {
                Log.w(TAG, "assets/face目录为空或不存在");
                return faceImages;
            }

            for (String fileName : files) {
                // 检查文件是否符合命名规范：face_xxx
                if (fileName.startsWith(FACE_PREFIX)) {
                    FaceImage faceImage = loadFaceImage(context, fileName);
                    if (faceImage != null) {
                        faceImages.add(faceImage);
                        Log.d(TAG, "加载人脸图片: " + fileName + " (用户: " + faceImage.userName + ")");
                    }
                }
            }

            Log.d(TAG, "成功加载 " + faceImages.size() + " 张人脸图片");

        } catch (IOException e) {
            Log.e(TAG, "读取assets/face目录失败", e);
        }

        return faceImages;
    }

    /**
     * 加载单个人脸图片
     */
    private static FaceImage loadFaceImage(@NonNull Context context, @NonNull String fileName) {
        try {
            // 从文件名提取用户名（去掉face_前缀和文件后缀）
            String userName = extractUserName(fileName);

            // 从assets读取图片
            String assetPath = FACE_ASSETS_DIR + "/" + fileName;
            InputStream inputStream = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "解码图片失败: " + fileName);
                return null;
            }

            // 转换为InputImage
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

            return new FaceImage(userName, inputImage, bitmap);

        } catch (IOException e) {
            Log.e(TAG, "加载人脸图片失败: " + fileName, e);
            return null;
        }
    }

    /**
     * 从文件名提取用户名
     * 例如: face_john.jpg -> john
     */
    private static String extractUserName(@NonNull String fileName) {
        // 移除face_前缀
        String withoutPrefix = fileName.substring(FACE_PREFIX.length());
        
        // 移除文件后缀
        int lastDotIndex = withoutPrefix.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return withoutPrefix.substring(0, lastDotIndex);
        }
        
        return withoutPrefix;
    }

    /**
     * 释放所有图片资源
     */
    public static void releaseAll(List<FaceImage> faceImages) {
        if (faceImages != null) {
            for (FaceImage faceImage : faceImages) {
                if (faceImage.bitmap != null) {
                    faceImage.bitmap.recycle();
                }
                // InputImage 不需要手动释放，GC会自动处理
            }
            faceImages.clear();
        }
    }
}
