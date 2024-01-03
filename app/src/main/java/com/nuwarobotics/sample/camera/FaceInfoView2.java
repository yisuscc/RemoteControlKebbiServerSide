package com.nuwarobotics.sample.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FaceInfoView2 extends View {
    // THis class is provided by Will Lin
    // i only did a few modifications
    // to get the ratios right
    private final String TAG = FaceInfoView2.class.getSimpleName();

    private final Paint mPaint;
    private final Rect mRect;

    /* private String mAge = "";
     private String mGender = "";
     private int mBottom, mLeft, mRight, mTop;*/
    private final List<DataFace> faceList = new ArrayList<>();
    private int horizontalRatio;
    private int verticalRatio;

    public FaceInfoView2(Context context, AttributeSet attr) {
        super(context, attr);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
        horizontalRatio = getResources().getDisplayMetrics().widthPixels / 640;
        verticalRatio = getResources().getDisplayMetrics().heightPixels / 480;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("jsonDraw", "draw the data face" +faceList.toString());
        final float DENSITY = getResources().getDisplayMetrics().density;

        final float FONT_SIZE = 24 * DENSITY;
        synchronized (faceList) {
            for (DataFace face : faceList) {
                mPaint.setTextSize(FONT_SIZE);
                mPaint.setStyle(Paint.Style.FILL);
                final String FORMAT_AGE = "Age: %1$s";
                final String FORMAT_GENDER = "Gender: %1$s";
                int mLeft, mTop, mRight, mBottom;
                //TODO refcator the variable names
                mLeft = face.getLeft();
                mRight = face.getRight();
                mTop = face.getTop();
                mBottom = face.getBottom();
                String mAge, mGender;
                mAge = face.getAge();
                mGender = face.getGender();
                mRect.set(mLeft, mTop, mRight, mBottom);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(mRect, mPaint);

                Log.d("jsonDraw", "drawing the following face" + face.toString());
                canvas.drawText(String.format(Locale.ENGLISH, FORMAT_AGE, mAge), mLeft, (mBottom - FONT_SIZE), mPaint);
                canvas.drawText(String.format(Locale.ENGLISH, FORMAT_GENDER, mGender), mLeft, mBottom, mPaint);
            }
        }

    }


    public void setData(JSONObject data) {
        if (null != data) {
            final String FIELD_FACES = "totalFaces"; //TODO: Change it accordibng to the correct names
            final String FIELD_AGE = "age";
            final String FIELD_GENDER = "gender";
            final String FIELD_RECT = "rect";
            final String FIELD_RECT_BOTTOM = "bottom";
            final String FIELD_RECT_LEFT = "left";
            final String FIELD_RECT_RIGHT = "right";
            final String FIELD_RECT_TOP = "top";
            synchronized (faceList) {
                faceList.clear();
                try {
                    if (data.has(FIELD_FACES)) {
                        JSONArray facesArray = data.getJSONArray(FIELD_FACES);
                        Log.d("json2", "Proceec to transjorm this json:" + facesArray.toString());
                        //for each of the fields in json array
                        for (int i = 0; i < facesArray.length(); i++) {
                            JSONObject faceObject = facesArray.getJSONObject(i);
                            Log.d("json2", "we obtain the following array" + faceObject.toString());
                            if (faceObject.has(FIELD_AGE) && // onoly stores thefaces with the  complete data
                                    faceObject.has(FIELD_GENDER) && faceObject.has(FIELD_RECT)) {
                                Log.d("json3", "ading the following face" + faceObject.toString());
                                DataFace dataFace = new DataFace();
                                dataFace.setAge(faceObject.getString(FIELD_AGE));
                                dataFace.setGender(faceObject.getString(FIELD_GENDER));
                                JSONObject rect = faceObject.getJSONObject(FIELD_RECT);
                                dataFace.setTop(rect.getInt(FIELD_RECT_TOP) * verticalRatio);
                                dataFace.setBottom(rect.getInt(FIELD_RECT_BOTTOM) * verticalRatio);
                                dataFace.setLeft(rect.getInt(FIELD_RECT_LEFT) * horizontalRatio);
                                dataFace.setRight(rect.getInt(FIELD_RECT_RIGHT) * horizontalRatio);
                                faceList.add(dataFace);

                            }
                        }


                    }
                } catch (Exception e) {
                    Log.d("JSON3", "parse data exception message = " + e.getMessage());
                }
            }
            invalidate();// wwhat this does ?
        }
    }

}