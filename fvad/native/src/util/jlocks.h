//
// Created by Vyacheslav Baranov on 12/09/2019.
//

#ifndef AMR_JNI_JLOCKS_H
#define AMR_JNI_JLOCKS_H

#include <jni.h>

struct JByteArrayLock {

    JNIEnv *env;
    jbyteArray array;
    jbyte *bytes;
    unsigned char* uchars;

    inline JByteArrayLock(JNIEnv *env, jbyteArray array) : env(env), array(array) {
        jboolean f = 0;
        bytes = env->GetByteArrayElements(array, &f);
        uchars = (unsigned char *) bytes;
    }

    inline ~JByteArrayLock() {
        env->ReleaseByteArrayElements(array, bytes, 0);
    }

};

struct JShortArrayLock {

    JNIEnv *env;
    jshortArray array;
    jshort *shorts;

    inline JShortArrayLock(JNIEnv *env, jshortArray array) : env(env), array(array) {
      jboolean f = 0;
      shorts = env->GetShortArrayElements(array, &f);
    }

    inline ~JShortArrayLock() {
      env->ReleaseShortArrayElements(array, shorts, 0);
    }

};

#endif //AMR_JNI_JLOCKS_H
