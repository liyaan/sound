#include "DParcel.h"

DParcel::DParcel() {
    this->mData = reinterpret_cast<char *>(malloc(1024)); // 1024 1kb
}

DParcel::~DParcel() {
    if (this->mData) {
        free(this->mData);
    }
    if (this->mDataPos) {
        this->mDataPos = NULL;
    }
}

void getLength(char * content, jint & len) {
    char * contentTemp = const_cast<char *>(content);
    int count = 0;
    while (*contentTemp != '\0') {
        contentTemp ++;
        count ++;
    }
    len = count;
}

void DParcel::writeInt(int val) {
    * reinterpret_cast<int *>(this->mData + this->mDataPos) = val;
    changePos(sizeof(val));
}

void DParcel::changePos(int val) {
    this->mDataPos += val;
}

void DParcel::setDataPosition(int pos) {
    this->mDataPos = pos;
}

jint DParcel::readInt() {
    jint ret = * reinterpret_cast<int *>(this->mData + this->mDataPos);
    changePos(sizeof(int));
    return ret;
}
