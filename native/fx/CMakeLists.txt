cmake_minimum_required(VERSION 3.20)
project(fx C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

add_executable(fx
        src/fx.cpp
        src/http.cpp
)

if (WIN32)
    set(CURL_USE_SCHANNEL ON CACHE BOOL "" FORCE)
endif ()
set(BUILD_CURL_EXE OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(CURL_USE_LIBPSL OFF CACHE BOOL "" FORCE)

add_subdirectory(../libs/curl-8.22.0 ${CMAKE_BINARY_DIR}/curl-build)

target_link_libraries(fx PRIVATE CURL::libcurl)
target_include_directories(fx PRIVATE include)

set_target_properties(fx PROPERTIES PREFIX "")
if (UNIX)
    target_link_libraries(fx PRIVATE m)
endif ()