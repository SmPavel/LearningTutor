#ifndef LEARNINGTUTOR_LLAMA_JNI_H
#define LEARNINGTUTOR_LLAMA_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong
extern "C" JNICALL
Java_com_learningtutor_core_ml_LLMGenerator_nativeInit(
        JNIEnv *env,
        jobject thiz,
        jstring model_path
);

JNIEXPORT jstring
extern "C" JNICALL
Java_com_learningtutor_core_ml_LLMGenerator_nativeGenerate(
        JNIEnv *env,
        jobject thiz,
        jlong context_ptr,
        jstring prompt,
        jint max_tokens
);

JNIEXPORT void JNICALL
Java_com_learningtutor_core_ml_LLMGenerator_nativeFree(
        JNIEnv *env,
jobject thiz,
        jlong context_ptr
);

#ifdef __cplusplus
}
#endif

#endif // LEARNINGTUTOR_LLAMA_JNI_H