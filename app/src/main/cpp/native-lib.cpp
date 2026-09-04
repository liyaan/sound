#include <jni.h>
#include <string>
#include <unistd.h>
#include "fmod.hpp"
#include <pthread.h>
// 日志输出
#include <android/log.h>
#define TAG "liyaan"

// __VA_ARGS__ 代表 ...的可变参数
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG,  __VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG,  __VA_ARGS__);
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG,  __VA_ARGS__);

using namespace FMOD;
using namespace std;

// 此处代码是，上层六个常量所对应的，六个宏
#undef MODE_NORMAL
#define MODE_NORMAL 0L
#undef MODE_LUOLI
#define MODE_LUOLI 1L
#undef MODE_DASHU
#define MODE_DASHU 2L
#undef MODE_JINGSONG
#define MODE_JINGSONG 3L
#undef MODE_GAOGUAI
#define MODE_GAOGUAI 4L
#undef MODE_KONGLING
#define MODE_KONGLING 5L

extern "C" JNIEXPORT jstring JNICALL
Java_com_liyaan_sound_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

Channel * channel = nullptr; // 通道 音轨

System * mSystem = nullptr; // fmod 音效引擎系统
Sound * sound = nullptr;   // fmod 声音
const char * gAudioFilePath = nullptr;
static jint mode = MODE_NORMAL;

JavaVM* g_vm = nullptr;
static jobject j_object = nullptr;
static pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM * vm, void * args) {
    ::g_vm = vm;
    return JNI_VERSION_1_6; // 一般会使用最新的JNI版本标记
}


extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_initSystem(JNIEnv *env, jobject thiz) {
    if (mSystem){
        mSystem->release();
        mSystem->close();
        mSystem = nullptr;
    }
    FMOD_RESULT result;
    result = System_Create(&mSystem);
    if (result != FMOD_OK) {
        LOGE("Failed to create FMOD system");
        return ;
    }
    // 在 init 之前指定输出类型为 OpenSL ES
//    result = mSystem->setOutput(FMOD_OUTPUTTYPE_OPENSL);
//    if (result != FMOD_OK) {
//        LOGE("Failed to setOutput FMOD system %d",result)
//    }
//    mSystem->setDSPBufferSize(4096, 4);
    //增加是否成功判断
    result = mSystem->init(32, FMOD_INIT_NORMAL | FMOD_INIT_STREAM_FROM_UPDATE, nullptr);
    if (result != FMOD_OK) {
        LOGE("Failed to init FMOD system %d",result)
        mSystem->release();
        mSystem->close();
        mSystem = nullptr;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_releaseSystem(JNIEnv *env, jobject thiz) {

    pthread_mutex_lock(&g_mutex);
    if (j_object) {
        env->DeleteGlobalRef(j_object);
        j_object = nullptr;
    }
    pthread_mutex_unlock(&g_mutex);
    if (gAudioFilePath){
        gAudioFilePath = nullptr;
    }

    if (channel) {
        channel->stop();
        channel = nullptr; // 建议置空，避免悬空指针
    }
    if (sound){
        sound->release();
        sound = nullptr;
    }
    if (mSystem){
        mSystem->close();
        mSystem->release();

        mSystem = nullptr;
    }else{
        LOGI("mSystem = %s","releaseSystem")
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_stopSound(JNIEnv *env, jobject thiz) {
    if (gAudioFilePath){
        gAudioFilePath = nullptr;
    }
    if (channel) {
        channel->stop();
        channel = nullptr; // 建议置空，避免悬空指针
    }
}




void* audio_thread_func(void* args){
    const char * content_ = "默认：播放完毕";
    DSP * dsp = nullptr; // digital signal process  == 数字信号处理

    if (!mSystem){
        LOGI("IS OK  mSystem null content_ %s\n",content_)
        return nullptr;
    }
    mSystem->createSound(gAudioFilePath, FMOD_DEFAULT, nullptr, &sound);
    LOGI("IS OK pthread_create content_ 1%s\n",content_)
    mSystem->playSound(sound, nullptr, false, &channel);
    LOGI("IS OK pthread_create content_ 2%s\n",content_)
    switch (mode) {
        case MODE_NORMAL:
            content_ = "原生：播放完毕";
            break;
        case MODE_LUOLI:
            content_ = "萝莉：播放完毕";
            mSystem->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 2.0f);
            channel->addDSP(0, dsp);
            break;
        case MODE_DASHU:
            content_ = "大叔：播放完毕";
            mSystem->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.7f);
            channel->addDSP(0, dsp);
            break;
        case MODE_JINGSONG:
            content_ = "惊悚音 播放完毕";
            mSystem->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.7f);
            channel->addDSP(0, dsp);

            //  TODO 搞点回声
            mSystem->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 400); // 延时的回音
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 40); // 默认：50  0完全衰减了
            channel->addDSP(1, dsp);

            // TODO 颤抖 Tremolo
            mSystem->createDSPByType(FMOD_DSP_TYPE_TREMOLO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_TREMOLO_FREQUENCY, 0.8f);
            dsp->setParameterFloat(FMOD_DSP_TREMOLO_SKEW, 0.8f);
            channel->addDSP(2, dsp);
            break;
        case MODE_GAOGUAI:
            content_ = "搞怪 小黄人：播放完毕"; // 频率快
            float frequency;
            channel->getFrequency(&frequency);
            channel->setFrequency(frequency * 1.5f);
            break;
        case MODE_KONGLING:
            content_ = "空灵：播放完毕";
            mSystem->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 200); // 延时的回音
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10); // 默认：50  0完全衰减了
            channel->addDSP(0, dsp);
            break;
    }
    mSystem->update();
    LOGI("IS OK audio_thread_func updata %s",content_)
    bool isPalyer = true;
    while (isPalyer) {
        channel->isPlaying(&isPalyer);
//        usleep(10 * 1000); // 每隔一秒
    }

    // 时时刻刻 记得回收
//    channel->stop();
    if (channel){
        channel = nullptr;
    }
    if (sound){
        sound->release();
        sound = nullptr;
    }


//    mSystem = nullptr;


    if (g_vm){
        JNIEnv* env;
        if (g_vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
            return nullptr;
        }
        pthread_mutex_lock(&g_mutex);
        jobject  l_object = j_object;
        pthread_mutex_unlock(&g_mutex);
        if (l_object){
            jstring  value = env->NewStringUTF(content_);
            jclass  cls = env->GetObjectClass(l_object);
            jmethodID methodID = env->GetMethodID(cls,"playerEnd", "(Ljava/lang/String;)V");
            env->CallVoidMethod(l_object, methodID, value); // 反射 完整     Java的发射
        }

        // 分离线程
        g_vm->DetachCurrentThread();
    }else{
        LOGI("vm is null 0x0000000")
    }

    return nullptr;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_voiceChangeNative(JNIEnv *env, jobject thiz, jint mode,
                                                     jstring path) {
    pthread_mutex_lock(&g_mutex);
    if (j_object) {
        env->DeleteGlobalRef(j_object);
    }
    j_object = env->NewGlobalRef(thiz);
    pthread_mutex_unlock(&g_mutex);

    // C认识 char *
    const char * path_ = env->GetStringUTFChars(path, nullptr);
    ::gAudioFilePath = path_;
    ::mode = mode;
    env->ReleaseStringUTFChars(path,path_);

    LOGI("IS OK voiceChangeNative %s\n",gAudioFilePath)
    pthread_t thread;
    int ret = pthread_create(&thread, nullptr,
                             audio_thread_func, nullptr);
    LOGI("IS OK pthread_create %s\n",gAudioFilePath)
    if (ret != 0) {
        LOGE("Failed to create pthread");
    } else {
        // 分离线程，使其在结束后自动释放资源

        ret = pthread_detach(thread);
        if (ret == EINVAL) {
            // 线程可能已经分离，或者已经是 detached 状态
            // 这种情况下通常可以忽略，因为资源最终会被自动回收
            LOGE("Thread was already detached or invalid.");
        } else if (ret == ESRCH) {
            // 找不到该线程，可能线程已经结束并被回收
            LOGE("No thread found with the given ID.");
        } else if (ret != 0) {
            LOGE("pthread_detach failed with error: %d", ret);
        }
    }









}



