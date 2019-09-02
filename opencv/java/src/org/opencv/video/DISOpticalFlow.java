//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;

// C++: class DISOpticalFlow

/**
 * DIS optical flow algorithm.
 * <p>
 * This class implements the Dense Inverse Search (DIS) optical flow algorithm. More
 * details about the algorithm can be found at CITE: Kroeger2016 . Includes three presets with preselected
 * parameters to provide reasonable trade-off between speed and quality. However, even the slowest preset is
 * still relatively fast, use DeepFlow if you need better quality and don't care about speed.
 * <p>
 * This implementation includes several additional features compared to the algorithm described in the paper,
 * including spatial propagation of flow vectors (REF: getUseSpatialPropagation), as well as an option to
 * utilize an initial flow approximation passed to REF: calc (which is, essentially, temporal propagation,
 * if the previous frame's flow field is passed).
 */
public class DISOpticalFlow extends DenseOpticalFlow {

    // C++: enum <unnamed>
    public static final int
            PRESET_ULTRAFAST = 0,
            PRESET_FAST = 1,
            PRESET_MEDIUM = 2;

    protected DISOpticalFlow(long addr) {
        super(addr);
    }

    // internal usage only
    public static DISOpticalFlow __fromPtr__(long addr) {
        return new DISOpticalFlow(addr);
    }


    //
    // C++: static Ptr_DISOpticalFlow cv::DISOpticalFlow::create(int preset = DISOpticalFlow::PRESET_FAST)
    //

    /**
     * Creates an instance of DISOpticalFlow
     *
     * @param preset one of PRESET_ULTRAFAST, PRESET_FAST and PRESET_MEDIUM
     * @return automatically generated
     */
    public static DISOpticalFlow create(int preset) {
        return DISOpticalFlow.__fromPtr__(create_0(preset));
    }

    /**
     * Creates an instance of DISOpticalFlow
     *
     * @return automatically generated
     */
    public static DISOpticalFlow create() {
        return DISOpticalFlow.__fromPtr__(create_1());
    }


    //
    // C++:  bool cv::DISOpticalFlow::getUseMeanNormalization()
    //

    // C++: static Ptr_DISOpticalFlow cv::DISOpticalFlow::create(int preset = DISOpticalFlow::PRESET_FAST)
    private static native long create_0(int preset);


    //
    // C++:  bool cv::DISOpticalFlow::getUseSpatialPropagation()
    //

    private static native long create_1();


    //
    // C++:  float cv::DISOpticalFlow::getVariationalRefinementAlpha()
    //

    // C++:  bool cv::DISOpticalFlow::getUseMeanNormalization()
    private static native boolean getUseMeanNormalization_0(long nativeObj);


    //
    // C++:  float cv::DISOpticalFlow::getVariationalRefinementDelta()
    //

    // C++:  bool cv::DISOpticalFlow::getUseSpatialPropagation()
    private static native boolean getUseSpatialPropagation_0(long nativeObj);


    //
    // C++:  float cv::DISOpticalFlow::getVariationalRefinementGamma()
    //

    // C++:  float cv::DISOpticalFlow::getVariationalRefinementAlpha()
    private static native float getVariationalRefinementAlpha_0(long nativeObj);


    //
    // C++:  int cv::DISOpticalFlow::getFinestScale()
    //

    // C++:  float cv::DISOpticalFlow::getVariationalRefinementDelta()
    private static native float getVariationalRefinementDelta_0(long nativeObj);


    //
    // C++:  int cv::DISOpticalFlow::getGradientDescentIterations()
    //

    // C++:  float cv::DISOpticalFlow::getVariationalRefinementGamma()
    private static native float getVariationalRefinementGamma_0(long nativeObj);


    //
    // C++:  int cv::DISOpticalFlow::getPatchSize()
    //

    // C++:  int cv::DISOpticalFlow::getFinestScale()
    private static native int getFinestScale_0(long nativeObj);


    //
    // C++:  int cv::DISOpticalFlow::getPatchStride()
    //

    // C++:  int cv::DISOpticalFlow::getGradientDescentIterations()
    private static native int getGradientDescentIterations_0(long nativeObj);


    //
    // C++:  int cv::DISOpticalFlow::getVariationalRefinementIterations()
    //

    // C++:  int cv::DISOpticalFlow::getPatchSize()
    private static native int getPatchSize_0(long nativeObj);


    //
    // C++:  void cv::DISOpticalFlow::setFinestScale(int val)
    //

    // C++:  int cv::DISOpticalFlow::getPatchStride()
    private static native int getPatchStride_0(long nativeObj);


    //
    // C++:  void cv::DISOpticalFlow::setGradientDescentIterations(int val)
    //

    // C++:  int cv::DISOpticalFlow::getVariationalRefinementIterations()
    private static native int getVariationalRefinementIterations_0(long nativeObj);


    //
    // C++:  void cv::DISOpticalFlow::setPatchSize(int val)
    //

    // C++:  void cv::DISOpticalFlow::setFinestScale(int val)
    private static native void setFinestScale_0(long nativeObj, int val);


    //
    // C++:  void cv::DISOpticalFlow::setPatchStride(int val)
    //

    // C++:  void cv::DISOpticalFlow::setGradientDescentIterations(int val)
    private static native void setGradientDescentIterations_0(long nativeObj, int val);


    //
    // C++:  void cv::DISOpticalFlow::setUseMeanNormalization(bool val)
    //

    // C++:  void cv::DISOpticalFlow::setPatchSize(int val)
    private static native void setPatchSize_0(long nativeObj, int val);


    //
    // C++:  void cv::DISOpticalFlow::setUseSpatialPropagation(bool val)
    //

    // C++:  void cv::DISOpticalFlow::setPatchStride(int val)
    private static native void setPatchStride_0(long nativeObj, int val);


    //
    // C++:  void cv::DISOpticalFlow::setVariationalRefinementAlpha(float val)
    //

    // C++:  void cv::DISOpticalFlow::setUseMeanNormalization(bool val)
    private static native void setUseMeanNormalization_0(long nativeObj, boolean val);


    //
    // C++:  void cv::DISOpticalFlow::setVariationalRefinementDelta(float val)
    //

    // C++:  void cv::DISOpticalFlow::setUseSpatialPropagation(bool val)
    private static native void setUseSpatialPropagation_0(long nativeObj, boolean val);


    //
    // C++:  void cv::DISOpticalFlow::setVariationalRefinementGamma(float val)
    //

    // C++:  void cv::DISOpticalFlow::setVariationalRefinementAlpha(float val)
    private static native void setVariationalRefinementAlpha_0(long nativeObj, float val);


    //
    // C++:  void cv::DISOpticalFlow::setVariationalRefinementIterations(int val)
    //

    // C++:  void cv::DISOpticalFlow::setVariationalRefinementDelta(float val)
    private static native void setVariationalRefinementDelta_0(long nativeObj, float val);

    // C++:  void cv::DISOpticalFlow::setVariationalRefinementGamma(float val)
    private static native void setVariationalRefinementGamma_0(long nativeObj, float val);

    // C++:  void cv::DISOpticalFlow::setVariationalRefinementIterations(int val)
    private static native void setVariationalRefinementIterations_0(long nativeObj, int val);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    /**
     * Whether to use mean-normalization of patches when computing patch distance. It is turned on
     * by default as it typically provides a noticeable quality boost because of increased robustness to
     * illumination variations. Turn it off if you are certain that your sequence doesn't contain any changes
     * in illumination.
     * SEE: setUseMeanNormalization
     *
     * @return automatically generated
     */
    public boolean getUseMeanNormalization() {
        return getUseMeanNormalization_0(nativeObj);
    }

    /**
     * getUseMeanNormalization SEE: getUseMeanNormalization
     *
     * @param val automatically generated
     */
    public void setUseMeanNormalization(boolean val) {
        setUseMeanNormalization_0(nativeObj, val);
    }

    /**
     * Whether to use spatial propagation of good optical flow vectors. This option is turned on by
     * default, as it tends to work better on average and can sometimes help recover from major errors
     * introduced by the coarse-to-fine scheme employed by the DIS optical flow algorithm. Turning this
     * option off can make the output flow field a bit smoother, however.
     * SEE: setUseSpatialPropagation
     *
     * @return automatically generated
     */
    public boolean getUseSpatialPropagation() {
        return getUseSpatialPropagation_0(nativeObj);
    }

    /**
     * getUseSpatialPropagation SEE: getUseSpatialPropagation
     *
     * @param val automatically generated
     */
    public void setUseSpatialPropagation(boolean val) {
        setUseSpatialPropagation_0(nativeObj, val);
    }

    /**
     * Weight of the smoothness term
     * SEE: setVariationalRefinementAlpha
     *
     * @return automatically generated
     */
    public float getVariationalRefinementAlpha() {
        return getVariationalRefinementAlpha_0(nativeObj);
    }

    /**
     * getVariationalRefinementAlpha SEE: getVariationalRefinementAlpha
     *
     * @param val automatically generated
     */
    public void setVariationalRefinementAlpha(float val) {
        setVariationalRefinementAlpha_0(nativeObj, val);
    }

    /**
     * Weight of the color constancy term
     * SEE: setVariationalRefinementDelta
     *
     * @return automatically generated
     */
    public float getVariationalRefinementDelta() {
        return getVariationalRefinementDelta_0(nativeObj);
    }

    /**
     * getVariationalRefinementDelta SEE: getVariationalRefinementDelta
     *
     * @param val automatically generated
     */
    public void setVariationalRefinementDelta(float val) {
        setVariationalRefinementDelta_0(nativeObj, val);
    }

    /**
     * Weight of the gradient constancy term
     * SEE: setVariationalRefinementGamma
     *
     * @return automatically generated
     */
    public float getVariationalRefinementGamma() {
        return getVariationalRefinementGamma_0(nativeObj);
    }

    /**
     * getVariationalRefinementGamma SEE: getVariationalRefinementGamma
     *
     * @param val automatically generated
     */
    public void setVariationalRefinementGamma(float val) {
        setVariationalRefinementGamma_0(nativeObj, val);
    }

    /**
     * Finest level of the Gaussian pyramid on which the flow is computed (zero level
     * corresponds to the original image resolution). The final flow is obtained by bilinear upscaling.
     * SEE: setFinestScale
     *
     * @return automatically generated
     */
    public int getFinestScale() {
        return getFinestScale_0(nativeObj);
    }

    /**
     * getFinestScale SEE: getFinestScale
     *
     * @param val automatically generated
     */
    public void setFinestScale(int val) {
        setFinestScale_0(nativeObj, val);
    }

    /**
     * Maximum number of gradient descent iterations in the patch inverse search stage. Higher values
     * may improve quality in some cases.
     * SEE: setGradientDescentIterations
     *
     * @return automatically generated
     */
    public int getGradientDescentIterations() {
        return getGradientDescentIterations_0(nativeObj);
    }

    /**
     * getGradientDescentIterations SEE: getGradientDescentIterations
     *
     * @param val automatically generated
     */
    public void setGradientDescentIterations(int val) {
        setGradientDescentIterations_0(nativeObj, val);
    }

    /**
     * Size of an image patch for matching (in pixels). Normally, default 8x8 patches work well
     * enough in most cases.
     * SEE: setPatchSize
     *
     * @return automatically generated
     */
    public int getPatchSize() {
        return getPatchSize_0(nativeObj);
    }

    /**
     * getPatchSize SEE: getPatchSize
     *
     * @param val automatically generated
     */
    public void setPatchSize(int val) {
        setPatchSize_0(nativeObj, val);
    }

    /**
     * Stride between neighbor patches. Must be less than patch size. Lower values correspond
     * to higher flow quality.
     * SEE: setPatchStride
     *
     * @return automatically generated
     */
    public int getPatchStride() {
        return getPatchStride_0(nativeObj);
    }

    /**
     * getPatchStride SEE: getPatchStride
     *
     * @param val automatically generated
     */
    public void setPatchStride(int val) {
        setPatchStride_0(nativeObj, val);
    }

    /**
     * Number of fixed point iterations of variational refinement per scale. Set to zero to
     * disable variational refinement completely. Higher values will typically result in more smooth and
     * high-quality flow.
     * SEE: setGradientDescentIterations
     *
     * @return automatically generated
     */
    public int getVariationalRefinementIterations() {
        return getVariationalRefinementIterations_0(nativeObj);
    }

    /**
     * getGradientDescentIterations SEE: getGradientDescentIterations
     *
     * @param val automatically generated
     */
    public void setVariationalRefinementIterations(int val) {
        setVariationalRefinementIterations_0(nativeObj, val);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
