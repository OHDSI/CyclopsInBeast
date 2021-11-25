#include "CyclopsJniWrapper.h"

#include <vector>
#include <limits>
#include <utility>

double jniGetBeta(void* ptr, const int index);
int jniGetBetaSize(void* ptr);
std::pair<double,double> jniGetGradientAndHessianDiagonal(void* ptr, const int index);
double jniGetLogLikelihood(void* ptr);
//int jniGetNumberOfColumns(void* ptr);
void jniSetBeta(void* ptr, const std::vector<double>& beta);
void jniSetBeta(void* ptr, const int index, const double beta);

extern "C"
JNIEXPORT jdouble JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getLogLikelihood
  (JNIEnv *, jobject, jlong ptr) {
	return jniGetLogLikelihood((void*)ptr);
}

extern "C"
JNIEXPORT void JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getGradient
  (JNIEnv* env, jobject, jlong ptr, jdoubleArray jOutput) {

  const int dim = jniGetBetaSize((void*)ptr);
  jsize outputSize = env->GetArrayLength(jOutput);

  jboolean isCopy;
  double *output = env->GetDoubleArrayElements(jOutput, &isCopy);

  if (2 * dim == outputSize) {
    for (int i = 0; i < dim; ++i) {
      std::pair<double,double> gh = jniGetGradientAndHessianDiagonal((void*)ptr, i);
      output[i] = gh.first;
      output[i + dim] = gh.second;
    }
  } else {
    for (int i = 0; i < outputSize; ++i) {
      output[i] = std::numeric_limits<double>::quiet_NaN();
    }
  }

  env->ReleaseDoubleArrayElements(jOutput, output, 0);
}

extern "C"
JNIEXPORT void JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_setBeta__JID
  (JNIEnv *, jobject, jlong ptr, jint index, jdouble value) {
  jniSetBeta((void*)ptr, index, value);
}

extern "C"
JNIEXPORT void JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_setBeta__J_3D
  (JNIEnv* env, jobject, jlong ptr, jdoubleArray values) {

  jsize size = env->GetArrayLength(values);
  std::vector<double> beta(size);
  env->GetDoubleArrayRegion(values, 0, size, &beta[0]);
  jniSetBeta((void*)ptr, beta);
}

extern "C"
JNIEXPORT jdouble JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getBeta
  (JNIEnv *, jobject, jlong ptr, jint index) {
	return jniGetBeta((void*)ptr, index);
}

extern "C"
JNIEXPORT jint JNICALL Java_org_ohdsi_cyclops_CyclopsJniWrapper_getBetaSize
  (JNIEnv *, jobject, jlong ptr) {
  //return jniGetNumberOfColumns((void*)ptr);
  return jniGetBetaSize((void*)ptr);
}
