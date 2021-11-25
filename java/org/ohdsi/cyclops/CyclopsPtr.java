package org.ohdsi.cyclops;

public class CyclopsPtr {

    private final static boolean DEBUG = true;

    private final long dataPtr;
    private final long fitPtr;

    private final CyclopsJniWrapper cyclopsInterface;

    public CyclopsPtr(String dataString, String fitString) {
        this.dataPtr = Long.decode(dataString);
        this.fitPtr = Long.decode(fitString);
        this.cyclopsInterface = CyclopsJniWrapper.loadLibrary();

        if (DEBUG) {
            System.err.println("Constructed a CyclopsPtr with " + toString());
        }

        if (dataPtr == 0L || fitPtr == 0L) {
            throw new IllegalArgumentException("CyclopsPtr pointers are null");
        }
    }

    public String toString() {
        return Long.toHexString(dataPtr) + " " + Long.toHexString(fitPtr);
    }

    public double getLogLikelihood() {
        return cyclopsInterface.getLogLikelihood(fitPtr);
    }

    public void getGradientAndHessianDiagonal(double[] values) {
        cyclopsInterface.getGradient(fitPtr, values);
    }

    public void setBeta(int index, double value) {
        cyclopsInterface.setBeta(fitPtr, index, value);
    }

    public void setBeta(double[] values) {
        cyclopsInterface.setBeta(fitPtr, values);
    }

    public double getBeta(int index) {
        return cyclopsInterface.getBeta(fitPtr, index);
    }

    public int getBetaSize() {
        return cyclopsInterface.getBetaSize(fitPtr);
    }
}
