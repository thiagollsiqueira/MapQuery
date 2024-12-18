#include <jni.h>	
#include <stdio.h>
#include <iostream>
#include "SBindex.h"
#include "SBindexCplusplus.h"
#include "SpatialQueryWindow.h"

using namespace std;

SBindex sb;

JNIEXPORT void JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_openForCreation(JNIEnv *env, jobject obj, jstring path, jint pagesize) {
	char *str;
	str = (char*)env->GetStringUTFChars(path, NULL);
	sb.openForCreation(str, pagesize);
	return;
}

JNIEXPORT void JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_closeAfterCreation(JNIEnv *env, jobject obj) {
	sb.closeAfterCreation();
	return;
}

JNIEXPORT void JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_write(JNIEnv *env, jobject obj, jintArray k, jdoubleArray xm, jdoubleArray ym, jdoubleArray XM, jdoubleArray YM, jint length) {	
	jint *key;
	jdouble *xMin, *yMin, *xMax, *yMax;

	key = env->GetIntArrayElements(k, NULL);
	xMin = env->GetDoubleArrayElements(xm, NULL);
	yMin = env->GetDoubleArrayElements(ym, NULL);
	xMax = env->GetDoubleArrayElements(XM, NULL);
	yMax = env->GetDoubleArrayElements(YM, NULL);

	//cout<<"sizeof(jint): "<<sizeof(jint)<<endl;
	//cout<<"sizeof(jdouble): "<<sizeof(jdouble)<<endl;
	
	sb.write((int*)key, xMin, yMin, xMax, yMax, length);

	env->ReleaseIntArrayElements(k, key, 0);
	env->ReleaseDoubleArrayElements(xm, xMin, 0);
	env->ReleaseDoubleArrayElements(ym, yMin, 0);
	env->ReleaseDoubleArrayElements(XM, xMax, 0);
	env->ReleaseDoubleArrayElements(YM, yMax, 0);

	return;
}

JNIEXPORT jint JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_sizeSbitvector (JNIEnv *env, jobject obj, jint pagesize)
{
	return sb.sizeSbitvector(pagesize);
};

JNIEXPORT jintArray JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_scanCrq(JNIEnv *env, jobject obj, jstring path, jdoubleArray qWindow/*, jdouble xm, jdouble ym, jdouble XM, jdouble YM*/)
{
	char *str;
	str = (char*)env->GetStringUTFChars(path, NULL);
	jdouble *queryWindow;
	queryWindow = env->GetDoubleArrayElements(qWindow, NULL);
	
	jint *ptr = (jint*) sb.scanCrq(str, (double*)queryWindow/*, (double)xm, (double)ym, (double)XM, (double)YM*/);
	
	env->ReleaseDoubleArrayElements(qWindow, queryWindow, 0);

	//cout<<"retornou"<<endl;
	jintArray arrayCandidates;
	int size = sb.getQttyCandidates();
	//cout <<"\nsize: " <<size<<endl;

	arrayCandidates = env->NewIntArray(size);
	env->SetIntArrayRegion(arrayCandidates, 0, size, ptr);
	//cout<<"converteu"<<endl;

	/*cout<<"ptr[0]: "<<ptr[0]<<endl;
	cout<<"ptr[1]: "<<ptr[1];*/

	return arrayCandidates;
}

JNIEXPORT jintArray JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_scanIrq(JNIEnv *env, jobject obj, jstring path, jdoubleArray qWindow/*, jdouble xm, jdouble ym, jdouble XM, jdouble YM*/)
{
	char *str;
	str = (char*)env->GetStringUTFChars(path, NULL);
	
	jdouble *queryWindow;
	queryWindow = env->GetDoubleArrayElements(qWindow, NULL);
	
	jint *ptr = (jint*) sb.scanIrq(str, (double*)queryWindow/*, (double)xm, (double)ym, (double)XM, (double)YM*/);
	
	env->ReleaseDoubleArrayElements(qWindow, queryWindow, 0);
	
	//cout<<"retornou"<<endl;
	jintArray arrayCandidates;
	int size = sb.getQttyCandidates();
	//cout <<"\nsize: " <<size<<endl;

	arrayCandidates = env->NewIntArray(size);
	env->SetIntArrayRegion(arrayCandidates, 0, size, ptr);
	//cout<<"converteu"<<endl;

	/*cout<<"ptr[0]: "<<ptr[0]<<endl;
	cout<<"ptr[1]: "<<ptr[1]<<endl;*/

	return arrayCandidates;
}


JNIEXPORT jintArray JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_scanErq(JNIEnv *env, jobject obj, jstring path, jdoubleArray qWindow/*, jdouble xm, jdouble ym, jdouble XM, jdouble YM*/)
{
	char *str;
	str = (char*)env->GetStringUTFChars(path, NULL);
	
	jdouble *queryWindow;
	queryWindow = env->GetDoubleArrayElements(qWindow, NULL);
	
	jint *ptr = (jint*) sb.scanErq(str, (double*)queryWindow/*, (double)xm, (double)ym, (double)XM, (double)YM*/);
	
	//cout<<"retornou"<<endl;
	jintArray arrayCandidates;
	int size = sb.getQttyCandidates();
	//cout <<"\nsize: " <<size<<endl;

	arrayCandidates = env->NewIntArray(size);
	env->SetIntArrayRegion(arrayCandidates, 0, size, ptr);
	//cout<<"converteu"<<endl;

	/*cout<<"ptr[0]: "<<ptr[0]<<endl;
	cout<<"ptr[1]: "<<ptr[1]<<endl;*/

	return arrayCandidates;
}


JNIEXPORT jintArray JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_scanPq(JNIEnv *env, jobject obj, jstring path, jdoubleArray qWindow)
{
	char *str;
	str = (char*)env->GetStringUTFChars(path, NULL);
	
	jdouble *queryWindow;
	queryWindow = env->GetDoubleArrayElements(qWindow, NULL);
	
	jint *ptr = (jint*) sb.scanPq(str, (double*)queryWindow);
	
	jintArray arrayCandidates;
	int size = sb.getQttyCandidates();

	arrayCandidates = env->NewIntArray(size);
	env->SetIntArrayRegion(arrayCandidates, 0, size, ptr);

	return arrayCandidates;
}


JNIEXPORT void JNICALL Java_br_ifsp_model_sbindexcplusplus_SBindexCplusplus_SpatialQueryWindow
  (JNIEnv *env, jobject obj, jdoubleArray qWindow)
{
	jdouble *queryWindow;
	queryWindow = env->GetDoubleArrayElements(qWindow, NULL);
	
	SpatialQueryWindow((double*)queryWindow);
	
	env->ReleaseDoubleArrayElements(qWindow, queryWindow, 0);
}