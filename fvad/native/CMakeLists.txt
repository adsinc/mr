PROJECT(fvad-jni)

set(FVAD_JNI_VERSION 0.1.0)
set(PROJECT_NAME fvad-jni)
cmake_minimum_required(VERSION 3.5)

set(JAVA_AWT_LIBRARY NotNeeded)
set(JAVA_JVM_LIBRARY NotNeeded)
find_package(JNI REQUIRED)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/src
        ${JNI_INCLUDE_DIRS}
    )

set(CMAKE_CXX_STANDARD 11)

add_library(fvad-jni SHARED
        src/signal_processing/division_operations.c
        src/signal_processing/energy.c
        src/signal_processing/get_scaling_square.c
        src/signal_processing/resample_48khz.c
        src/signal_processing/resample_by_2_internal.c
        src/signal_processing/resample_fractional.c
        src/signal_processing/spl_inl.c
        src/vad/vad_core.c
        src/vad/vad_filterbank.c
        src/vad/vad_gmm.c
        src/vad/vad_sp.c
        src/fvad.c
        src/ru_kryptonite_audio_fvad_FVad.cpp)
