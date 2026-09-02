#include <jni.h>
#include <string>
#include <unistd.h>
#include "fmod.hpp"

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


extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_initSystem(JNIEnv *env, jobject thiz) {
    if (mSystem){
        mSystem->release();
        mSystem->close();
        mSystem = nullptr;
    }
    System_Create(&mSystem);
    //增加是否成功判断
    mSystem->init(32, FMOD_INIT_NORMAL, nullptr);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_releaseSystem(JNIEnv *env, jobject thiz) {
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
    if (channel) {
        channel->stop();
        channel = nullptr; // 建议置空，避免悬空指针
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_liyaan_sound_MainActivity_voiceChangeNative(JNIEnv *env, jobject thiz, jint mode,
                                                     jstring path) {
    const char * content_ = "默认：播放完毕";

    // C认识 char *
    const char * path_ = env->GetStringUTFChars(path, nullptr);

    DSP * dsp = nullptr; // digital signal process  == 数字信号处理

    if (!mSystem){
        return;
    }
    mSystem->createStream(path_, FMOD_DEFAULT, nullptr, &sound);

    mSystem->playSound(sound, nullptr, false, &channel);

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
    bool isPalyer = true;
    while (isPalyer) {
        channel->isPlaying(&isPalyer);
        usleep(1000 * 1000); // 每隔一秒
    }

    // 时时刻刻 记得回收
//    channel->stop();
    sound->release();
//    mSystem->close();
//    mSystem->release();
//    channel = nullptr;
    sound = nullptr;
//    mSystem = nullptr;


    env->ReleaseStringUTFChars(path,path_);

    jstring  value = env->NewStringUTF(content_);
    jclass  cls = env->GetObjectClass(thiz);
    jmethodID methodID = env->GetMethodID(cls,"playerEnd", "(Ljava/lang/String;)V");
    env->CallVoidMethod(thiz, methodID, value); // 反射 完整     Java的发射

}



