//
// Created by Peter Schulz on 18/09/16.
//
#include <android/log.h>
#include <android/bitmap.h>

#include "decompressor.h"
#include "turbojpeg.h"

JNIEXPORT void JNICALL Java_edu_is_Decompressor_decompress(JNIEnv *env, jobject instance, jbyteArray source_, jint length, jobject target) {

    jboolean copied;

    LOGI("Decompressing");

    jbyte *source = (*env)->GetByteArrayElements(env, source_, &copied);

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

    tjDecompress2(_jpegDecompressor, sourcePixelsPointer, length, targetPixelsPointer, width, 0/*pitch*/, height, TJPF_RGBX, TJFLAG_FASTDCT);

    tjDestroy(_jpegDecompressor);

    AndroidBitmap_unlockPixels(env, target);

    (*env)->ReleaseByteArrayElements(env, source_, source, 0);

}