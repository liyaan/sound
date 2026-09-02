
cmake_minimum_required(VERSION 3.22.1)


project("sound")
# TODO  第一步：导入头文件
include_directories("inc")

# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)
# TODO  第二步：设置库文件路径  设置CMAKE_CXX_FLAGS环境变量(库的路径)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        fmod  # 具体的一定要链接到 native-lib里面去
        fmodL # 具体的一定要链接到 native-lib里面去
        log)