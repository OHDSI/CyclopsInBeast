/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_ohdsi_cyclops_CyclopsJniWrapper */

#ifndef _Included_org_ohdsi_cyclops_CyclopsJniWrapper
#define _Included_org_ohdsi_cyclops_CyclopsJniWrapper
#ifdef __cplusplus
extern "C" {
#endif
#undef org_ohdsi_cyclops_CyclopsJniWrapper_DEBUG
#define org_ohdsi_cyclops_CyclopsJniWrapper_DEBUG 1L
/*
 * Class:     org_ohdsi_cyclops_CyclopsJniWrapper
 * Method:    getLogLikelihood
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getLogLikelihood
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_ohdsi_cyclops_CyclopsJniWrapper
 * Method:    getGradient
 * Signature: (J)[D
 */
JNIEXPORT void JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getGradient
  (JNIEnv *, jobject, jlong, jdoubleArray);

/*
 * Class:     org_ohdsi_cyclops_CyclopsJniWrapper
 * Method:    setBeta
 * Signature: (JID)V
 */
JNIEXPORT void JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_setBeta__JID
  (JNIEnv *, jobject, jlong, jint, jdouble);

/*
 * Class:     org_ohdsi_cyclops_CyclopsJniWrapper
 * Method:    setBeta
 * Signature: (J[D)V
 */
JNIEXPORT void JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_setBeta__J_3D
  (JNIEnv *, jobject, jlong, jdoubleArray);

/*
 * Class:     org_ohdsi_cyclops_CyclopsJniWrapper
 * Method:    getBeta
 * Signature: (JI)D
 */
JNIEXPORT jdouble JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getBeta
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     org_ohdsi_cyclops_CyclopsJniWrapper
 * Method:    getBetaSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getBetaSize
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
