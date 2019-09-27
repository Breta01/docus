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
    private int viewWidth;
    private int viewHeight;

    private double velocity = 0.009;
    private Point[] targetCorners;
    private Point[] corners = new Point[]{
            new Point(0, 0),
            new Point(0, 1.0),
            new Point(1.0, 1.0),
            new Point(1.0, 0)};

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
        targetCorners = points;
    }

    private void moveCorners() {
        if (targetCorners != null) {
            for (int i = 0; i < 4; i++) {
                if (corners[i].x != targetCorners[i].x) {
                    int xDir = (corners[i].x > targetCorners[i].x) ? -1 : 1;
                    corners[i].x = Math.abs(Math.min(
                            xDir * (corners[i].x + xDir * velocity), xDir * targetCorners[i].x));
                }

                if (corners[i].y != targetCorners[i].y) {
                    int yDir = (corners[i].y > targetCorners[i].y) ? -1 : 1;
                    corners[i].y = Math.abs(Math.min(
                            yDir * (corners[i].y + yDir * velocity), yDir * targetCorners[i].y));
                }
            }
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        while (mRunning) {
            if (holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    synchronized (holder) {
                        // Clean previous canvas
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                        // Draw lines between corners
                        if (corners[0] != null) {
                            path.moveTo(
                                    (int) (corners[0].x * viewWidth),
                                    (int) (corners[0].y * viewHeight));

                            for (int i = 1; i < 4; i++)
                                path.lineTo(
                                        (int) (corners[i].x * viewWidth),
                                        (int) (corners[i].y * viewHeight));
                            path.close();

                            canvas.drawPath(path, paint);
                        }
                        path.rewind();
                        moveCorners();
                    }
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
        corners = new Point[]{
                new Point(0, 0),
                new Point(0, 1.0),
                new Point(1.0, 1.0),
                new Point(1.0, 0)};
        targetCorners = null;
    }
}
