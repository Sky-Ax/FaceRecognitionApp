package com.example.facerecognitionapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.face.Face;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸检测结果绘制视图
 */
public class FaceOverlayView extends View {
    private List<Face> faces = new ArrayList<>();
    private Paint boxPaint;
    private Paint textPaint;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    public FaceOverlayView(Context context) {
        super(context);
        init();
    }

    public FaceOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化边框画笔
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3);

        // 初始化文字画笔
        textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(40);
        textPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 更新人脸检测结果
     */
    public void updateFaces(List<Face> detectedFaces, int imageWidth, int imageHeight) {
        this.faces = detectedFaces;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        // 计算缩放比例
        this.scaleX = (float) getWidth() / imageWidth;
        this.scaleY = (float) getHeight() / imageHeight;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (faces == null || faces.isEmpty()) {
            return;
        }

        for (Face face : faces) {
            drawFace(canvas, face);
        }
    }

    /**
     * 绘制单个人脸
     */
    private void drawFace(Canvas canvas, Face face) {
        Rect boundingBox = face.getBoundingBox();

        // 转换坐标到View坐标系
        float left = boundingBox.left * scaleX;
        float top = boundingBox.top * scaleY;
        float right = boundingBox.right * scaleX;
        float bottom = boundingBox.bottom * scaleY;

        // 绘制边框
        canvas.drawRect(left, top, right, bottom, boxPaint);

        // 绘制人脸信息
        String faceInfo = buildFaceInfo(face);
        canvas.drawText(faceInfo, left, top - 10, textPaint);
    }

    /**
     * 构建人脸信息文本
     */
    private String buildFaceInfo(Face face) {
        StringBuilder info = new StringBuilder();
        // info.append("人脸 ");

        // 微笑概率
        if (face.getSmilingProbability() != null) {
            info.append("微笑: ").append(String.format("%.0f%%", face.getSmilingProbability() * 100));
        }

        // 左眼睁开概率
        if (face.getLeftEyeOpenProbability() != null) {
            info.append("左眼: ").append(String.format("%.0f%%", face.getLeftEyeOpenProbability() * 100));
        }

        // 右眼睁开概率
        if (face.getRightEyeOpenProbability() != null) {
            info.append(" 右眼: ").append(String.format("%.0f%%", face.getRightEyeOpenProbability() * 100));
        }

        return info.toString();
    }

    /**
     * 清空人脸数据
     */
    public void clearFaces() {
        this.faces = new ArrayList<>();
        postInvalidate();
    }
}
