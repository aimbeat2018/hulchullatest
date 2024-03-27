#include <jni.h>
#include <string>

//std::string SERVER_URL = "https://hunters.co.in/ppv1/rest-api/";
std::string SERVER_URL = "https://hulchul.co.in/ox9_backoffice/rest_api/";
//std::string SERVER_URL = "https://ox9.in/ox9_backoffice/rest_api/";//bigplay

//std::string API_KEY = "2b14kpa3k18nyqbu1sdre1cy";
std::string API_KEY = "2b14kpa3k18nyqbu1sdre1cy4kpa3k18";  //hulchul

//std::string API_KEY = "2b14kpa3k18nyqbu1sdre1cy4kpa3k18";//hulchul

std::string PURCHASE_CODE = "xxxxxxxxxxxxxxx";

//WARNING: ==>> Don't change anything below.
extern "C" JNIEXPORT jstring JNICALL
Java_ott_hulchulandroid_AppConfig_getApiServerUrl(
        JNIEnv *env,
        jclass clazz) {
    return env->NewStringUTF(SERVER_URL.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_ott_hulchulandroid_AppConfig_getApiKey(
        JNIEnv *env,
        jclass clazz) {
    return env->NewStringUTF(API_KEY.c_str());
}


extern "C" JNIEXPORT jstring JNICALL
Java_ott_hulchulandroid_AppConfig_getPurchaseCode(
        JNIEnv *env,
        jclass clazz) {
    return env->NewStringUTF(PURCHASE_CODE.c_str());
}

