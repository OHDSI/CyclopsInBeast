package org.ohdsi.cyclops;

import java.util.ArrayList;
import java.util.List;

public class CyclopsJniWrapper {

    private static final boolean DEBUG = true;

    private static List<String> libraryNames = new ArrayList<>();

    @SuppressWarnings("unused")
    public static void addLibrary(String name) {
        libraryNames.add(name);
    }

    /**
     * private constructor to enforce singleton instance
     */
    private CyclopsJniWrapper() { }

    protected native double getLogLikelihood(long ptr);

    protected native void getGradient(long ptr, double[] values);

    protected native void setBeta(long ptr, int index, double value);

    protected native void setBeta(long ptr, double[] values);

    protected native double getBeta(long ptr, int index);

    protected native int getBetaSize(long ptr);

    /* Library loading routines */

//    private static final String LIBRARY_NAME = "cyclops_jni";
//
//    private static String getPlatformSpecificLibraryName() {
//        String osName = System.getProperty("os.name").toLowerCase();
//        String osArch = System.getProperty("os.arch").toLowerCase();
//        if (osName.startsWith("windows")) {
//            if(osArch.equals("i386")) return LIBRARY_NAME + "32";
//            if(osArch.startsWith("amd64")||osArch.startsWith("x86_64")) return LIBRARY_NAME + "64";
//        }
//        return LIBRARY_NAME;
//    }

    static CyclopsJniWrapper loadLibrary() throws UnsatisfiedLinkError {

        if (INSTANCE == null) {

            for (String libraryName: libraryNames) {

                if (DEBUG) {
                    System.err.println("Trying to load " + libraryName);

                }

//            String path = "";
//            if (System.getProperty(LIBRARY_PATH_LABEL) != null) {
//                path = System.getProperty(LIBRARY_PATH_LABEL);
//                if (path.length() > 0 && !path.endsWith("/")) {
//                    path += "/";
//                }
//            }

                System.load(libraryName);
//            System.loadLibrary(path + LIBRARY_PLATFORM_NAME);
            }
            INSTANCE = new CyclopsJniWrapper();

            if (DEBUG) {
                System.err.println("CyclopsInBeast JNI library loaded.");
            }
        }

        return INSTANCE;
    }

    private static CyclopsJniWrapper INSTANCE = null;
}
