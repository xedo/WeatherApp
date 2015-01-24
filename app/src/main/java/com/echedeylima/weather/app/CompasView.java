package com.echedeylima.weather.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Echedey on 29/10/14.
 */
public class CompasView extends View {

    private final String LOG_TAG = getClass().getSimpleName();

    private Paint mWindmillPaint;
    private Paint mArrowPaint;
    private float mSpeed;
    private float mDegrees;
    private float mRotation = 359f;
    private Bitmap mRotor;
    private Bitmap mStand;

    public CompasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CompasView(Context context) {
        super(context);
        init();
    }

    public CompasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mWindmillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWindmillPaint.setStyle(Paint.Style.FILL);
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStyle(Paint.Style.FILL);
        mArrowPaint.setColor(Color.GRAY);
        mArrowPaint.setStrokeWidth(20f);
        mRotor = BitmapFactory.decodeResource(getResources(), R.drawable.rotor);
        mStand = BitmapFactory.decodeResource(getResources(), R.drawable.windmill);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;
        if (hSpecMode == MeasureSpec.EXACTLY) {
            myHeight = hSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            // wrap content
        }

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;
        if (wSpecMode == MeasureSpec.EXACTLY) {
            myWidth = wSpecSize;
        } else if (wSpecMode == MeasureSpec.AT_MOST) {
            // wrap content
        }

        setMeasuredDimension(myHeight, myWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(LOG_TAG, "Wind speed: " + mSpeed);
        Log.v(LOG_TAG, "Wind direction: " + mDegrees);
        // Draw rotor
        int h = 0;
        int w = 0;
        canvas.drawBitmap(mRotor, rotate(mRotor, h, w), mWindmillPaint);
        canvas.drawBitmap(mStand, 0, 10, mWindmillPaint);
        invalidate();
    }

    public Matrix rotate(Bitmap bitmap, int x, int y) {
        Matrix matrix = new Matrix();
        matrix.postRotate(mRotation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        matrix.postTranslate(x, y);  //The coordinates where we want to put our bitmap
        mRotation -= mSpeed / 2.5; //degree of rotation
        return matrix;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public void setDegrees(float degrees) {
        mDegrees = degrees;
    }
}
