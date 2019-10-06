package com.bretahajek.docus.ocr;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class PageDetector {
    public static final int SMALL_HEIGHT = 600;

    private static final String TAG = "PageDetector";
    private static int E_min = 80;
    private static int E_max = 120;
    private static int BF_diameter = 5;
    private static int BF_sigmaColor = 75;
    private static int BF_sigmaSpace = 75;
    private static int AT_blockSize = 101;
    private static int AT_C = 4;
    private static int MB_ksize = 5;
    private static Size ME_kernel = new Size(5, 5);
    private static int borderSize = 5;


    public PageDetector() {
    }

    public static Mat getPage(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        MatOfPoint pageContour = getPageCorners(grayImage);
        return perspTransform(image, pageContour);
    }

    public static MatOfPoint getPageCorners(Mat image) {
        Mat smallImage = new Mat();
        double ratio = image.height() / (double) SMALL_HEIGHT;

        Imgproc.resize(image, smallImage, new Size(image.width() / ratio, SMALL_HEIGHT));

        Mat imageEdges = edges(smallImage, E_min, E_max);
        Imgproc.morphologyEx(imageEdges, imageEdges,
                Imgproc.MORPH_CLOSE,
                Mat.ones(ME_kernel, CvType.CV_8U));

        MatOfPoint pageContour = findPageContour(imageEdges, smallImage);

        Core.multiply(pageContour, new Scalar(ratio, ratio), pageContour);
        return pageContour;
    }

    private static Mat edges(Mat image, double minVal, double maxVal) {
        Mat tmpImage = new Mat();
        Imgproc.bilateralFilter(image, tmpImage,
                BF_diameter,
                BF_sigmaColor,
                BF_sigmaSpace);

        Imgproc.medianBlur(tmpImage, image,
                MB_ksize);

        Integer[] center = new Integer[]{image.width() / 2, image.height() / 2};
        Rect centerRect = new Rect(center[0] - 20, center[1] - 20, 40, 40);
        Scalar targetMean = Core.mean(image.submat(centerRect));
        Scalar lower = new Scalar(targetMean.val[0] - 30);
        Scalar upper = new Scalar(targetMean.val[0] + 30);

        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);
        Core.inRange(image, lower, upper, mask);
        Core.compare(mask, new Scalar(0), mask, Core.CMP_GE);

        Imgproc.adaptiveThreshold(image, tmpImage,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                AT_blockSize,
                AT_C);

        tmpImage.copyTo(image, mask);
        Core.copyMakeBorder(image, image,
                borderSize,
                borderSize,
                borderSize,
                borderSize,
                Core.BORDER_CONSTANT);
        return image;
    }

    private static MatOfPoint findPageContour(Mat edges, Mat img) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(edges, contours, new Mat(),
                Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);

        int height = edges.rows();
        int width = edges.cols();
        double minArea = height * width * 0.4;
        double maxArea = (width - 2 * borderSize) * (height - 2 * borderSize);

        double area = minArea;
        MatOfPoint pageContour = new MatOfPoint(
                new Point(0, 0),
                new Point(0, height - borderSize),
                new Point(width - borderSize, height - borderSize),
                new Point(width - borderSize, 0));

        for (Mat cnt : contours) {
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint2f cntMOP2f = new MatOfPoint2f();
            cnt.convertTo(cntMOP2f, CvType.CV_32FC2);

            double perimeter = Imgproc.arcLength(cntMOP2f, true);
            Imgproc.approxPolyDP(cntMOP2f, approx, 0.03 * perimeter, true);

            MatOfPoint approxMOP = new MatOfPoint();
            approx.convertTo(approxMOP, CvType.CV_32S);

            if (approx.rows() == 4 &&
                    Imgproc.isContourConvex(approxMOP) &&
                    area < Imgproc.contourArea(approx) &&
                    Imgproc.contourArea(approx) < maxArea) {
                Log.i(TAG, "Found page contour");
                area = Imgproc.contourArea(approx);
                pageContour = approxMOP;
            }
        }

        return cornerSortAndOffset(pageContour, new Point(-borderSize, -borderSize));
    }

    private static MatOfPoint cornerSortAndOffset(MatOfPoint pts, Point offset) {
        Point[] arr = pts.toArray();
        Point[] res = arr.clone();

        Arrays.sort(arr, new Comparator<Point>() {
            @Override
            public int compare(Point a, Point b) {
                return Double.valueOf(a.x + a.y).compareTo(b.x + b.y);
            }
        });

        if (arr[1].x - arr[1].y < arr[2].x - arr[2].y) {
            res[1] = arr[1];
            res[3] = arr[2];
        } else {
            res[1] = arr[2];
            res[3] = arr[1];
        }
        res[0] = arr[0];
        res[2] = arr[3];

        for (int i = 0; i < res.length; i++) {
            res[i].x = (res[i].x + offset.x > 0) ? res[i].x + offset.x : 0;
            res[i].y = (res[i].y + offset.y > 0) ? res[i].y + offset.y : 0;
        }

        return new MatOfPoint(res);
    }

    private static Mat perspTransform(Mat img, MatOfPoint sPoints) {
        double height = Math.max(
                distance(sPoints.get(0, 0), sPoints.get(1, 0)),
                distance(sPoints.get(2, 0), sPoints.get(3, 0)));
        double width = Math.max(
                distance(sPoints.get(0, 0), sPoints.get(3, 0)),
                distance(sPoints.get(1, 0), sPoints.get(2, 0)));

        MatOfPoint tPoints = new MatOfPoint(
                new Point(0, 0),
                new Point(0, height),
                new Point(width, height),
                new Point(width, 0));

        sPoints.convertTo(sPoints, CvType.CV_32F);
        tPoints.convertTo(tPoints, CvType.CV_32F);

        Mat perspTransform = Imgproc.getPerspectiveTransform(sPoints, tPoints);
        Imgproc.warpPerspective(img, img,
                perspTransform,
                new Size((int) (width), (int) (height)));

        return img;
    }

    private static double distance(double[] p1, double p2[]) {
        return Math.sqrt(Math.pow((p2[0] - p1[0]), 2) + Math.pow((p2[1] - p1[1]), 2));
    }
}
