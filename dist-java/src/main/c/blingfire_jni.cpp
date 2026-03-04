#include <jni.h>
#include "blingfiretokdll.h"

using namespace BlingFire;

extern "C" {

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_GetBlingFireTokVersion
  (JNIEnv *env, jclass cls) {
    return GetBlingFireTokVersion();
}

JNIEXPORT jlong JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_LoadModel
  (JNIEnv *env, jclass cls, jstring jModelPath) {
    const char *modelPath = env->GetStringUTFChars(jModelPath, 0);
    void* handle = LoadModel(modelPath);
    env->ReleaseStringUTFChars(jModelPath, modelPath);
    return (jlong) handle;
}

JNIEXPORT void JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_FreeModel
  (JNIEnv *env, jclass cls, jlong handle) {
    FreeModel((void*) handle);
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToSentencesWithModel
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jbyteArray outBytes, jint maxOutLen, jlong handle) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);

    int res = TextToSentencesWithModel((char*)inBuf, (int)inLen, (char*)outBuf, (int)maxOutLen, (void*)handle);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToSentencesWithOffsetsWithModel
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jbyteArray outBytes, jintArray startOffsets, jintArray endOffsets, jint maxOutLen, jlong handle) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);
    jint* startBuf = env->GetIntArrayElements(startOffsets, NULL);
    jint* endBuf = env->GetIntArrayElements(endOffsets, NULL);

    int res = TextToSentencesWithOffsetsWithModel((char*)inBuf, (int)inLen, (char*)outBuf, (int*)startBuf, (int*)endBuf, (int)maxOutLen, (void*)handle);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);
    env->ReleaseIntArrayElements(startOffsets, startBuf, 0);
    env->ReleaseIntArrayElements(endOffsets, endBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToWordsWithModel
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jbyteArray outBytes, jint maxOutLen, jlong handle) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);

    int res = TextToWordsWithModel((char*)inBuf, (int)inLen, (char*)outBuf, (int)maxOutLen, (void*)handle);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToWordsWithOffsetsWithModel
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jbyteArray outBytes, jintArray startOffsets, jintArray endOffsets, jint maxOutLen, jlong handle) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);
    jint* startBuf = env->GetIntArrayElements(startOffsets, NULL);
    jint* endBuf = env->GetIntArrayElements(endOffsets, NULL);

    int res = TextToWordsWithOffsetsWithModel((char*)inBuf, (int)inLen, (char*)outBuf, (int*)startBuf, (int*)endBuf, (int)maxOutLen, (void*)handle);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);
    env->ReleaseIntArrayElements(startOffsets, startBuf, 0);
    env->ReleaseIntArrayElements(endOffsets, endBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToIds
  (JNIEnv *env, jclass cls, jlong handle, jbyteArray inBytes, jint inLen, jintArray idsArr, jint maxIds, jint unkId) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jint* idsBuf = env->GetIntArrayElements(idsArr, NULL);

    int res = TextToIds((void*)handle, (const char*)inBuf, (int)inLen, (int32_t*)idsBuf, (int)maxIds, (int)unkId);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseIntArrayElements(idsArr, idsBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToIdsWithOffsets
  (JNIEnv *env, jclass cls, jlong handle, jbyteArray inBytes, jint inLen, jintArray idsArr, jintArray startOffsets, jintArray endOffsets, jint maxIds, jint unkId) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jint* idsBuf = env->GetIntArrayElements(idsArr, NULL);
    jint* startBuf = env->GetIntArrayElements(startOffsets, NULL);
    jint* endBuf = env->GetIntArrayElements(endOffsets, NULL);

    int res = TextToIdsWithOffsets((void*)handle, (const char*)inBuf, (int)inLen, (int32_t*)idsBuf, (int*)startBuf, (int*)endBuf, (int)maxIds, (int)unkId);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseIntArrayElements(idsArr, idsBuf, 0);
    env->ReleaseIntArrayElements(startOffsets, startBuf, 0);
    env->ReleaseIntArrayElements(endOffsets, endBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_IdsToText
  (JNIEnv *env, jclass cls, jlong handle, jintArray idsArr, jint idsCount, jbyteArray outBytes, jint maxOutLen, jboolean skipSpecialTokens) {

    jint* idsBuf = env->GetIntArrayElements(idsArr, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);

    int res = IdsToText((void*)handle, (const int32_t*)idsBuf, (int)idsCount, (char*)outBuf, (int)maxOutLen, (bool)skipSpecialTokens);

    env->ReleaseIntArrayElements(idsArr, idsBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_NormalizeSpaces
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jbyteArray outBytes, jint maxOutLen, jint spaceChar) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);

    int res = NormalizeSpaces((const char*)inBuf, (int)inLen, (char*)outBuf, (int)maxOutLen, (int)spaceChar);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_TextToHashes
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jintArray hashArr, jint maxHashes, jint wordNgrams, jint bucketSize) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jint* hashBuf = env->GetIntArrayElements(hashArr, NULL);

    int res = TextToHashes((const char*)inBuf, (int)inLen, (int32_t*)hashBuf, (int)maxHashes, (int)wordNgrams, (int)bucketSize);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseIntArrayElements(hashArr, hashBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_WordHyphenationWithModel
  (JNIEnv *env, jclass cls, jbyteArray inBytes, jint inLen, jbyteArray outBytes, jint maxOutLen, jlong handle, jint hyphenChar) {

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    jbyte* outBuf = env->GetByteArrayElements(outBytes, NULL);

    int res = WordHyphenationWithModel((const char*)inBuf, (int)inLen, (char*)outBuf, (int)maxOutLen, (void*)handle, (int)hyphenChar);

    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    env->ReleaseByteArrayElements(outBytes, outBuf, 0);

    return res;
}

JNIEXPORT jint JNICALL Java_com_github_lfoppiano_blingfire_BlingFire_SetNoDummyPrefix
  (JNIEnv *env, jclass cls, jlong handle, jboolean noDummyPrefix) {

    return SetNoDummyPrefix((void*)handle, (bool)noDummyPrefix);
}

}
