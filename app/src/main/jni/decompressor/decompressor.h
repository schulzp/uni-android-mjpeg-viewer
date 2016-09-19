//
// Created by Peter Schulz on 18/09/16.
//

#ifndef REMOTE_CAMERA_VIEWER_DECOMPRESSOR_H
#define REMOTE_CAMERA_VIEWER_DECOMPRESSOR_H

#include <jni.h>
#include <setjmp.h>

#define  LOG_TAG    "decompressor"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT void JNICALL Java_edu_is_Decompressor_decompress(JNIEnv *env, jobject instance, jbyteArray source_, jint length, jobject target);

#endif //REMOTE_CAMERA_VIEWER_DECOMPRESSOR_H
