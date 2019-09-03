//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.calib3d;

import org.opencv.core.Rect;

// C++: class StereoBM

/**
 * Class for computing stereo correspondence using the block matching algorithm, introduced and
 * contributed to OpenCV by K. Konolige.
 */
public class StereoBM extends StereoMatcher {

    // C++: enum <unnamed>
    public static final int
            PREFILTER_NORMALIZED_RESPONSE = 0,
            PREFILTER_XSOBEL = 1;

    protected StereoBM(long addr) {
        super(addr);
    }

    // internal usage only
    public static StereoBM __fromPtr__(long addr) {
        return new StereoBM(addr);
    }


    //
    // C++: static Ptr_StereoBM cv::StereoBM::create(int numDisparities = 0, int blockSize = 21)
    //

    /**
     * Creates StereoBM object
     *
     * @param numDisparities the disparity search range. For each pixel algorithm will find the best
     *                       disparity from 0 (default minimum disparity) to numDisparities. The search range can then be
     *                       shifted by changing the minimum disparity.
     * @param blockSize      the linear size of the blocks compared by the algorithm. The size should be odd
     *                       (as the block is centered at the current pixel). Larger block size implies smoother, though less
     *                       accurate disparity map. Smaller block size gives more detailed disparity map, but there is higher
     *                       chance for algorithm to find a wrong correspondence.
     *                       <p>
     *                       The function create StereoBM object. You can then call StereoBM::compute() to compute disparity for
     *                       a specific stereo pair.
     * @return automatically generated
     */
    public static StereoBM create(int numDisparities, int blockSize) {
        return StereoBM.__fromPtr__(create_0(numDisparities, blockSize));
    }

    /**
     * Creates StereoBM object
     *
     * @param numDisparities the disparity search range. For each pixel algorithm will find the best
     *                       disparity from 0 (default minimum disparity) to numDisparities. The search range can then be
     *                       shifted by changing the minimum disparity.
     *                       (as the block is centered at the current pixel). Larger block size implies smoother, though less
     *                       accurate disparity map. Smaller block size gives more detailed disparity map, but there is higher
     *                       chance for algorithm to find a wrong correspondence.
     *                       <p>
     *                       The function create StereoBM object. You can then call StereoBM::compute() to compute disparity for
     *                       a specific stereo pair.
     * @return automatically generated
     */
    public static StereoBM create(int numDisparities) {
        return StereoBM.__fromPtr__(create_1(numDisparities));
    }

    /**
     * Creates StereoBM object
     * <p>
     * disparity from 0 (default minimum disparity) to numDisparities. The search range can then be
     * shifted by changing the minimum disparity.
     * (as the block is centered at the current pixel). Larger block size implies smoother, though less
     * accurate disparity map. Smaller block size gives more detailed disparity map, but there is higher
     * chance for algorithm to find a wrong correspondence.
     * <p>
     * The function create StereoBM object. You can then call StereoBM::compute() to compute disparity for
     * a specific stereo pair.
     *
     * @return automatically generated
     */
    public static StereoBM create() {
        return StereoBM.__fromPtr__(create_2());
    }


    //
    // C++:  Rect cv::StereoBM::getROI1()
    //

    // C++: static Ptr_StereoBM cv::StereoBM::create(int numDisparities = 0, int blockSize = 21)
    private static native long create_0(int numDisparities, int blockSize);


    //
    // C++:  Rect cv::StereoBM::getROI2()
    //

    private static native long create_1(int numDisparities);


    //
    // C++:  int cv::StereoBM::getPreFilterCap()
    //

    private static native long create_2();


    //
    // C++:  int cv::StereoBM::getPreFilterSize()
    //

    // C++:  Rect cv::StereoBM::getROI1()
    private static native double[] getROI1_0(long nativeObj);


    //
    // C++:  int cv::StereoBM::getPreFilterType()
    //

    // C++:  Rect cv::StereoBM::getROI2()
    private static native double[] getROI2_0(long nativeObj);


    //
    // C++:  int cv::StereoBM::getSmallerBlockSize()
    //

    // C++:  int cv::StereoBM::getPreFilterCap()
    private static native int getPreFilterCap_0(long nativeObj);


    //
    // C++:  int cv::StereoBM::getTextureThreshold()
    //

    // C++:  int cv::StereoBM::getPreFilterSize()
    private static native int getPreFilterSize_0(long nativeObj);


    //
    // C++:  int cv::StereoBM::getUniquenessRatio()
    //

    // C++:  int cv::StereoBM::getPreFilterType()
    private static native int getPreFilterType_0(long nativeObj);


    //
    // C++:  void cv::StereoBM::setPreFilterCap(int preFilterCap)
    //

    // C++:  int cv::StereoBM::getSmallerBlockSize()
    private static native int getSmallerBlockSize_0(long nativeObj);


    //
    // C++:  void cv::StereoBM::setPreFilterSize(int preFilterSize)
    //

    // C++:  int cv::StereoBM::getTextureThreshold()
    private static native int getTextureThreshold_0(long nativeObj);


    //
    // C++:  void cv::StereoBM::setPreFilterType(int preFilterType)
    //

    // C++:  int cv::StereoBM::getUniquenessRatio()
    private static native int getUniquenessRatio_0(long nativeObj);


    //
    // C++:  void cv::StereoBM::setROI1(Rect roi1)
    //

    // C++:  void cv::StereoBM::setPreFilterCap(int preFilterCap)
    private static native void setPreFilterCap_0(long nativeObj, int preFilterCap);


    //
    // C++:  void cv::StereoBM::setROI2(Rect roi2)
    //

    // C++:  void cv::StereoBM::setPreFilterSize(int preFilterSize)
    private static native void setPreFilterSize_0(long nativeObj, int preFilterSize);


    //
    // C++:  void cv::StereoBM::setSmallerBlockSize(int blockSize)
    //

    // C++:  void cv::StereoBM::setPreFilterType(int preFilterType)
    private static native void setPreFilterType_0(long nativeObj, int preFilterType);


    //
    // C++:  void cv::StereoBM::setTextureThreshold(int textureThreshold)
    //

    // C++:  void cv::StereoBM::setROI1(Rect roi1)
    private static native void setROI1_0(long nativeObj, int roi1_x, int roi1_y, int roi1_width, int roi1_height);


    //
    // C++:  void cv::StereoBM::setUniquenessRatio(int uniquenessRatio)
    //

    // C++:  void cv::StereoBM::setROI2(Rect roi2)
    private static native void setROI2_0(long nativeObj, int roi2_x, int roi2_y, int roi2_width, int roi2_height);

    // C++:  void cv::StereoBM::setSmallerBlockSize(int blockSize)
    private static native void setSmallerBlockSize_0(long nativeObj, int blockSize);

    // C++:  void cv::StereoBM::setTextureThreshold(int textureThreshold)
    private static native void setTextureThreshold_0(long nativeObj, int textureThreshold);

    // C++:  void cv::StereoBM::setUniquenessRatio(int uniquenessRatio)
    private static native void setUniquenessRatio_0(long nativeObj, int uniquenessRatio);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public Rect getROI1() {
        return new Rect(getROI1_0(nativeObj));
    }

    public void setROI1(Rect roi1) {
        setROI1_0(nativeObj, roi1.x, roi1.y, roi1.width, roi1.height);
    }

    public Rect getROI2() {
        return new Rect(getROI2_0(nativeObj));
    }

    public void setROI2(Rect roi2) {
        setROI2_0(nativeObj, roi2.x, roi2.y, roi2.width, roi2.height);
    }

    public int getPreFilterCap() {
        return getPreFilterCap_0(nativeObj);
    }

    public void setPreFilterCap(int preFilterCap) {
        setPreFilterCap_0(nativeObj, preFilterCap);
    }

    public int getPreFilterSize() {
        return getPreFilterSize_0(nativeObj);
    }

    public void setPreFilterSize(int preFilterSize) {
        setPreFilterSize_0(nativeObj, preFilterSize);
    }

    public int getPreFilterType() {
        return getPreFilterType_0(nativeObj);
    }

    public void setPreFilterType(int preFilterType) {
        setPreFilterType_0(nativeObj, preFilterType);
    }

    public int getSmallerBlockSize() {
        return getSmallerBlockSize_0(nativeObj);
    }

    public void setSmallerBlockSize(int blockSize) {
        setSmallerBlockSize_0(nativeObj, blockSize);
    }

    public int getTextureThreshold() {
        return getTextureThreshold_0(nativeObj);
    }

    public void setTextureThreshold(int textureThreshold) {
        setTextureThreshold_0(nativeObj, textureThreshold);
    }

    public int getUniquenessRatio() {
        return getUniquenessRatio_0(nativeObj);
    }

    public void setUniquenessRatio(int uniquenessRatio) {
        setUniquenessRatio_0(nativeObj, uniquenessRatio);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
