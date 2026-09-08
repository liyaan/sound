#include <malloc.h>
#include <jni.h>

#ifndef DERRYPARCEL_DPARCEL_H
#define DERRYPARCEL_DPARCEL_H

class DParcel {

    public:
        DParcel();

        virtual ~DParcel();

        void writeInt(int val);

        void setDataPosition(int pos);

        jint readInt();

    private:
        char * mData = 0; // DParcel对象共享内存的首地址(内存地址)
        int mDataPos = 0; // DParcel对象共享内存的首地址(内存地址)的指针地址挪动位置
        void changePos(int val); // 用于改变指针地址挪动位置
        long * stringObj = NULL; // StringObj对象的 首地址(内存地址)
        // jint len = 0;
};


#endif //DERRYPARCEL_DPARCEL_H
