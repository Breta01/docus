package com.bretahajek.scannerapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.core.Point;

public class PageSurface extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final Paint paint;
    private final Path path;
    private final SurfaceHolder holder;
    private final Context context;
    private boolean mRunning;
    private Thread mThread = null;
    private Point[] corners = new Point[4];
    private int viewWidth;
    private int viewHeight;


    public PageSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        setZOrderOnTop(true);

        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);

        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void updateCorners(Point[] points) {
        corners = points;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (mRunning) {
            if (holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                    // Draw lines between corners
                    if (corners[0] != null) {
                        path.moveTo((int) corners[0].x, (int) corners[0].y);
                        for (int i = 1; i < 4; i++)
                            path.lineTo((int) corners[i].x, (int) corners[i].y);
                        path.close();
                        canvas.drawPath(path, paint);
                    }

                    path.rewind();
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (width != 0 && height != 0) {
            viewHeight = height;
            viewWidth = width;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        pause();
    }

    public void pause() {
        mRunning = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        mRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }
}
