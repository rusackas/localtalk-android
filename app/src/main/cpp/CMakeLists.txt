# whisper.cpp must be cloned as a submodule before building:
#   git submodule add https://github.com/ggml-org/whisper.cpp app/src/main/cpp/whisper.cpp
#   git submodule update --init
cmake_minimum_required(VERSION 3.22.1)
project(localtalk)

set(WHISPER_DIR "${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp")

add_subdirectory(${WHISPER_DIR} whisper_build)

add_library(localtalk SHARED whisper_jni.cpp)

target_link_libraries(localtalk
    whisper
    android
    log)
