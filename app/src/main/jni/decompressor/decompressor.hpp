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

#ifdef __cplusplus
extern "C" {
#endif

jint throwNoClassDefError(JNIEnv *env, char *msg) {
    jclass exClass;
    char *className = "java/lang/NoClassDefFoundError";
    exClass = env->FindClass(className);
    return env->ThrowNew(exClass, className);
}

jint throwDecompressorException(JNIEnv *env, char *message) {
    jclass exClass;
    char *className = "edu/is/jpeg/Decompressor$Exception";

    exClass = env->FindClass(className);
    if (exClass == NULL) {
        return throwNoClassDefError(env, className);
    }

    return env->ThrowNew(exClass, message);
}

jint throwIllegalArgumentException(JNIEnv *env, char *message) {
    jclass exClass;
    char *className = "java/lang/IllegalArgumentException";

    exClass = env->FindClass(className);
    if (exClass == NULL) {
        return throwNoClassDefError(env, className);
    }

    return env->ThrowNew(exClass, message);
}

jint throwUnsupportedOperationException(JNIEnv *env, char *message) {
    jclass exClass;
    char *className = "java/lang/UnsupportedOperationException";

    exClass = env->FindClass(className);
    if (exClass == NULL) {
        return throwNoClassDefError(env, className);
    }

    return env->ThrowNew(exClass, message);
}

jint JNI_OnLoad(JavaVM *vm, void *reserved);

JNIEXPORT void JNICALL Java_edu_is_jpeg_Decompressor_decompress(JNIEnv *env, jobject instance, jobject source, jint length, jobject target);

void JNI_OnUnload(JavaVM *vm, void *reserved);

#ifdef __cplusplus
}
#endif
#endif //REMOTE_CAMERA_VIEWER_DECOMPRESSOR_H
