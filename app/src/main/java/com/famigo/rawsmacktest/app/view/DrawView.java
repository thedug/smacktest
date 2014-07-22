package com.famigo.rawsmacktest.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class DrawView extends View {

    private static final String TAG = DrawView.class.getSimpleName();

    private List<StrokeEvent> localStroke = new ArrayList<StrokeEvent>();
    private List<StrokeEvent> remoteStroke = new ArrayList<StrokeEvent>();
    private OnStrokeEventListener eventListener = null;
    private Paint localPaint;
    private Paint remotePaint;


    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        localPaint = new Paint();
        localPaint.setAntiAlias(true);
        localPaint.setColor(Color.BLUE);
        localPaint.setStrokeWidth(10);
        localPaint.setStrokeCap(Paint.Cap.ROUND);
        localPaint.setStrokeJoin(Paint.Join.ROUND);
        localPaint.setStyle(Paint.Style.STROKE);

        remotePaint = new Paint();
        remotePaint.setAntiAlias(true);
        remotePaint.setColor(Color.RED);
        remotePaint.setStrokeWidth(10);
        remotePaint.setStrokeCap(Paint.Cap.ROUND);
        remotePaint.setStrokeJoin(Paint.Join.ROUND);
        remotePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawStroke(canvas, localStroke, localPaint);
        drawStroke(canvas, remoteStroke, remotePaint);
    }

    private void drawStroke(Canvas canvas, List<StrokeEvent> stroke, Paint paint) {
        Path path = new Path();

        for ( StrokeEvent se: stroke ){
            float scaledX = ((float) getWidth()) * se.x;
            float scaledY = ((float) getHeight()) * se.y;

            switch ( se.action ){
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(scaledX, scaledY);
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    path.lineTo(scaledX, scaledY);
                    path.moveTo(scaledX, scaledY);
            }
        }
        canvas.drawPath(path,paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        event.offsetLocation(-getLeft(), -getTop());

        float width = getRight() - getLeft();
        float height = getBottom() - getTop();

        float x =  event.getX() / width;
        float y =  event.getY() / height;


        StrokeEvent se = null;
        switch ( event.getAction() ){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                se = new StrokeEvent(x,y,event.getAction(), localStroke.size());
                localStroke.add(se);
                Log.d(TAG, se.toString());
                invalidate();
                if ( eventListener!= null ){
                    eventListener.newStrokeEvent(se);
                }
                return true;
            default:
                return false;
        }

    }

    public void setOnStrokeEventListener( OnStrokeEventListener listener ){
        eventListener = listener;
    }

    public void addRemoteEvent(StrokeEvent strokeEvent) {
        remoteStroke.add(strokeEvent);

        Collections.sort( remoteStroke, new Comparator<StrokeEvent>() {
            @Override
            public int compare(StrokeEvent lhs, StrokeEvent rhs) {
                return lhs.index-rhs.index;
            }
        });

        invalidate();
    }

    public static interface OnStrokeEventListener {
        public void newStrokeEvent( StrokeEvent strokeEvent);
    }
}
