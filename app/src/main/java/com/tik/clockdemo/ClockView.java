package com.tik.clockdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class ClockView extends View {
    //表盘宽度
    private int mWidth;
    //表盘高度
    private int mHeight;
    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;
    private Paint mDegreePaint;
    private Paint mTextPaint;
    //字体缩放参数
    private float mScale = 1f;
    //表盘半径
    private int mRadius;
    //开关
    private boolean mOpen = true;
    //默认刻度文本字体大小
    private static final int DEFAULT_DEGREE_TEXT_SIZE = 30;
    //秒针长度
    private float secondPointerLength;
    //分针长度
    private float minutePointerLength;
    //时针长度
    private float hourPointerLength;
    //控件默认最小宽度
    private static final int DEFAULT_MIN_WIDTH = 300;
    //长刻度线
    private static final int DEFAULT_DEGREE_LONG_LENGTH = 20;
    //短刻度线
    private static final int DEFAULT_DEGREE_SHORT_LENGTH = 10;
    //外圆边框宽度
    private static final int DEFAULT_BORDER_WIDTH = 6;
    //指针反向超过圆点的长度
    private static final float DEFAULT_POINT_BACK_LENGTH =30f;
    private Calendar mCalendar;
    private long mTime;
    //是否自动同步时间
    private boolean isAuto = true;

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureSize(widthMeasureSpec);
        int height = measureSize(heightMeasureSpec);
        mWidth = width - getPaddingLeft() - getPaddingRight();
        mHeight = height - getPaddingTop() - getPaddingBottom();
        Log.i("tag", "width="+width+", height="+height+ "\nmWidth="+mWidth+",mheight="+mHeight);
        mScale = (float)mWidth/DEFAULT_MIN_WIDTH;
        Log.i("tag", "mScale="+mScale);
        setMeasuredDimension(width, height);
    }

    private int measureSize(int measureSpec){
        int result = DEFAULT_MIN_WIDTH;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else if(specMode == MeasureSpec.AT_MOST){
            result = Math.min(result, specSize);
        }
        return result;
    }

    private void init(){
        mDegreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDegreePaint.setStyle(Paint.Style.STROKE);
        mDegreePaint.setColor(Color.BLACK);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(DEFAULT_DEGREE_TEXT_SIZE);

        mHourPaint = new Paint();
        mHourPaint.setAntiAlias(true);
        mHourPaint.setColor(Color.BLACK);
        mHourPaint.setStyle(Paint.Style.FILL);
        mHourPaint.setStrokeWidth(10);

        mMinutePaint = new Paint();
        mMinutePaint.setAntiAlias(true);
        mMinutePaint.setColor(Color.BLACK);
        mMinutePaint.setStyle(Paint.Style.FILL);
        mMinutePaint.setStrokeWidth(5);

        mSecondPaint = new Paint();
        mSecondPaint.setAntiAlias(true);
        mSecondPaint.setColor(Color.RED);
        mSecondPaint.setStyle(Paint.Style.FILL);
        mSecondPaint.setStrokeWidth(3);

        mCalendar = Calendar.getInstance();
        new Thread(mTickRunnable).start();
    }

    /**
     * 计算时针、分针、秒针的长度
     */
    private void reset(){
        mRadius = (Math.min(mWidth / 2, mHeight / 2) - DEFAULT_BORDER_WIDTH / 2);
        secondPointerLength = mRadius * 0.8f;
        minutePointerLength = mRadius * 0.6f;
        hourPointerLength = mRadius * 0.5f;
    }

    private void drawCircle(Canvas canvas){
        mDegreePaint.setStyle(Paint.Style.STROKE);
        mDegreePaint.setStrokeWidth(DEFAULT_BORDER_WIDTH);
        canvas.drawCircle(getWidth()/2, getHeight()/2, mRadius, mDegreePaint);
        int degreeLength = 0;
        for (int i = 1; i <= 60; i++){
            canvas.rotate(6, getWidth()/2, getHeight()/2);
            if(i % 5 == 0){
                mDegreePaint.setStrokeWidth(6);
                degreeLength = DEFAULT_DEGREE_LONG_LENGTH;
            }else{
                mDegreePaint.setStrokeWidth(3);
                degreeLength = DEFAULT_DEGREE_SHORT_LENGTH;
            }
            canvas.drawLine(getWidth()/2, Math.abs(getHeight()/2 - mRadius), getWidth()/2, Math.abs(getHeight()/2 - mRadius) + degreeLength, mDegreePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        reset();
        drawCircle(canvas);
        // get current time and caculate degree
        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);

        float hourRotate = hour * 30.0f + minute / 60.0f * 30.0f;
        float minuteRotate = minute * 6.0f + second / 60.0f * 6.0f;
        float secondRotate = second * 6.0f;

        //刻度数字
        int degressNumberSize = (int) (DEFAULT_DEGREE_TEXT_SIZE * mScale);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        Paint paintDegreeNumber = new Paint();
        paintDegreeNumber.setTextAlign(Paint.Align.CENTER);
        paintDegreeNumber.setTextSize(degressNumberSize);
        paintDegreeNumber.setFakeBoldText(true);
        paintDegreeNumber.setColor(Color.RED);
        for(int i=0;i<12;i++){
            float[] temp = calculatePoint((i+1)*30, mRadius - DEFAULT_DEGREE_LONG_LENGTH - degressNumberSize/2 - 5);
            canvas.drawText((i+1)+"", temp[2], temp[3] + degressNumberSize/2- 4, paintDegreeNumber);
        }

        //时针，分针，秒针
        float[] hourPoints = calculatePoint(hourRotate, hourPointerLength);
        canvas.drawLine(hourPoints[0], hourPoints[1], hourPoints[2], hourPoints[3], mHourPaint);
        float[] minutePoints = calculatePoint(minuteRotate, minutePointerLength);
        canvas.drawLine(minutePoints[0], minutePoints[1], minutePoints[2], minutePoints[3], mMinutePaint);
        float[] secondPoints = calculatePoint(secondRotate, secondPointerLength);
        canvas.drawLine(secondPoints[0], secondPoints[1], secondPoints[2], secondPoints[3], mSecondPaint);

        //中心点
        mDegreePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, 6, mDegreePaint);
        Log.i("tag", Arrays.toString(hourPoints));
    }

    /**
     * 根据角度和长度计算线段的起点和终点的坐标
     * @param angle 以0点为0(360)度  3点为90度 6点为180度 9点为270度， PI/180=1°
     * @param length 指针长度（不包含指针反向超过圆点的长度）
     * @return
     */
    private float[] calculatePoint(float angle, float length){
        float[] points = new float[4];
        if(angle <= 90f){
            points[0] = -(float) Math.sin(angle* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = (float) Math.cos(angle* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = (float) Math.sin(angle* Math.PI/180) * length;
            points[3] = -(float) Math.cos(angle* Math.PI/180) * length;
        }else if(angle <= 180f){
            points[0] = -(float) Math.cos((angle-90)* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = -(float) Math.sin((angle-90)* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = (float) Math.cos((angle-90)* Math.PI/180) * length;
            points[3] = (float) Math.sin((angle-90)* Math.PI/180) * length;
        }else if(angle <= 270f){
            points[0] = (float) Math.sin((angle-180)* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = -(float) Math.cos((angle-180)* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = -(float) Math.sin((angle-180)* Math.PI/180) * length;
            points[3] = (float) Math.cos((angle-180)* Math.PI/180) * length;
        }else if(angle <= 360f){
            points[0] = (float) Math.cos((angle-270)* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[1] = (float) Math.sin((angle-270)* Math.PI/180) * DEFAULT_POINT_BACK_LENGTH;
            points[2] = -(float) Math.cos((angle-270)* Math.PI/180) * length;
            points[3] = -(float) Math.sin((angle-270)* Math.PI/180) * length;
        }
        return points;
    }

    public boolean isAuto(){
        return this.isAuto;
    }

//    public void manual(String time){
//        this.isAuto = false;
//        setTime(time);
//    }

    public void auto(){
        this.isAuto = true;
        mCalendar = Calendar.getInstance();
        mTime = 0;
        postInvalidate();
        start();
    }

    public void start(){
        mOpen = true;
    }

    public void stop(){
        mOpen = false;
    }

    public void setTime(int hour, int minute, int second){
        isAuto = false;
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.HOUR, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        if(second != -1){
            mCalendar.set(Calendar.SECOND, second);
        }
        mTime = mCalendar.getTimeInMillis();
        postInvalidate();
    }

    public void setTime(Date date){
        isAuto = false;
        mCalendar.setTime(date);
        mTime = mCalendar.getTimeInMillis();
        postInvalidate();
    }

    public void setTime(long time){
        Date date = new Date(time);
        setTime(date);
    }

    public void setTime(String time){
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
            setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
//        if(visibility == View.VISIBLE){
//            mOpen = true;
//        }else{
//            mOpen = false;
//        }
    }

    private Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                if(mOpen){
                    if(!isAuto){
                        mTime += 1000;
                        mCalendar.setTimeInMillis(mTime);
                    }else{
                        mCalendar = Calendar.getInstance();
                    }
                    Log.i("tag", "time="+mTime);
                    postInvalidate();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };


}
