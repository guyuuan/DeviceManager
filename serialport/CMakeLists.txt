
cmake_minimum_required(VERSION 3.10.2)

#set(libs ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
#
#add_library(libserial_port SHARED IMPORTED)
#
#set_target_properties(libserial_port PROPERTIES
#        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libserial_port.so")
add_library(
        serial_port
        SHARED
        src/cpp/SerialPort.c
        src/cpp/SerialPort.h
)
find_library(
        log-lib
        log
)
target_link_libraries(
        serial_port
        ${log-lib}
)