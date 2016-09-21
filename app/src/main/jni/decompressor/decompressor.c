//
// Created by Peter Schulz on 18/09/16.
//
#include <android/log.h>
#include <android/bitmap.h>

#include "decompressor.h"
#include "turbojpeg.h"

JNIEXPORT jboolean JNICALL Java_edu_is_Decompressor_decompress(JNIEnv *env, jobject instance, jbyteArray source_, jint length, jobject target) {

    LOGI("Decompressing");

    jbyte *source = (*env)->GetDirectBufferAddress(env, source_);

    int jpegSubsamp, width, height, ret;

    unsigned char* sourcePixelsPointer = (unsigned char*) source;
    unsigned char* targetPixelsPointer;

    void* targetPixelsAddressPointer;

    if ((ret = AndroidBitmap_lockPixels(env, target, &targetPixelsAddressPointer)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    targetPixelsPointer = (unsigned char*) targetPixelsAddressPointer;

    tjhandle _jpegDecompressor = tjInitDecompress();

    tjDecompressHeader2(_jpegDecompressor, sourcePixelsPointer, length, &width, &height, &jpegSubsamp);

    LOGD("Width %d, Height: %d, Sub-Samples: %d", width, height, jpegSubsamp);

    if (tjDecompress2(_jpegDecompressor, sourcePixelsPointer, length, targetPixelsPointer, width, 0/*pitch*/, height, TJPF_RGBX, TJFLAG_FASTDCT) < 0) {
        LOGE("Error while decompressing: %s", tjGetErrorStr());
        throwDecompressorException(env, tjGetErrorStr());
    }

    tjDestroy(_jpegDecompressor);

    AndroidBitmap_unlockPixels(env, target);
}