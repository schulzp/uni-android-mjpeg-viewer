//
// Created by Peter Schulz on 18/09/16.
//
#include <android/log.h>
#include <android/bitmap.h>

#include "decompressor.hpp"
#include "turbojpeg.h"

tjhandle _jpegDecompressor;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    _jpegDecompressor = tjInitDecompress();
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_edu_is_jpeg_Decompressor_decompress(JNIEnv *env, jobject instance, jobject source, jint length, jobject target) {

    int ret;

    unsigned char* sourcePixelsPointer = (unsigned char*) env->GetDirectBufferAddress(source);
    unsigned char* targetPixelsPointer;

    void* targetPixelsAddressPointer;

    if (sourcePixelsPointer == NULL) {
        throwIllegalArgumentException(env, "Source is not a direct buffer");
        return;
    }

    AndroidBitmapInfo targetInfo;
    AndroidBitmap_getInfo(env, target, &targetInfo);
    if ((ret = AndroidBitmap_lockPixels(env, target, &targetPixelsAddressPointer)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        throwUnsupportedOperationException(env, "Unable to lock target bitmap pixels, see log for details");
        return;
    }

    targetPixelsPointer = (unsigned char*) targetPixelsAddressPointer;

    if (tjDecompress2(_jpegDecompressor, sourcePixelsPointer, length, targetPixelsPointer, targetInfo.width, 0/*pitch*/, targetInfo.height, TJPF_RGBX, TJFLAG_FASTDCT) < 0) {
        LOGE("Error while decompressing: %s", tjGetErrorStr());
        throwDecompressorException(env, tjGetErrorStr());
    }

    AndroidBitmap_unlockPixels(env, target);
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    tjDestroy(_jpegDecompressor);
}