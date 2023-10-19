#include "ru_kryptonite_audio_fvad_FVad.h"

#include "../include/fvad.h"
#include "util/jlocks.h"

JNIEXPORT jlong JNICALL Java_ru_kryptonite_audio_fvad_FVad_init(
        JNIEnv *env,
        jobject self,
        jint sample_rate,
        jint mode)
{
    Fvad* vad_ptr = fvad_new();
    fvad_set_sample_rate(vad_ptr, (int)sample_rate);
    fvad_set_mode(vad_ptr, (int)mode);
    return (jlong)vad_ptr;
}


JNIEXPORT jint JNICALL Java_ru_kryptonite_audio_fvad_FVad_process(
        JNIEnv *env,
        jobject self,
        jlong state_ptr,
        jshortArray data) {
    JShortArrayLock data_lock(env, data);
    size_t num_samples = env->GetArrayLength(data);
    int result = fvad_process((Fvad*)state_ptr, data_lock.shorts, num_samples);
    return result;
}

JNIEXPORT void JNICALL Java_ru_kryptonite_audio_fvad_FVad__1reset(
        JNIEnv *env,
        jobject self,
        jlong state_ptr,
        jint sample_rate,
        jint mode)
{
    Fvad* vad_ptr = (Fvad*) state_ptr;
    fvad_reset(vad_ptr);
    fvad_set_sample_rate(vad_ptr, (int)sample_rate);
    fvad_set_mode(vad_ptr, (int)mode);
}


JNIEXPORT void JNICALL Java_ru_kryptonite_audio_fvad_FVad__1close(
        JNIEnv *env,
        jobject self,
        jlong state_ptr) {
    if (state_ptr != 0) {
        fvad_free((Fvad*)state_ptr);
    }
}
