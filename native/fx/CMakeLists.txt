cmake_minimum_required(VERSION 3.20)
project(fx C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

add_library(fx SHARED
        src/currency.cpp
        src/http.cpp
        src/fx.cpp
)

if (WIN32)
    set(CURL_USE_SCHANNEL ON CACHE BOOL "" FORCE)
endif ()
set(BUILD_CURL_EXE OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(CURL_USE_LIBPSL OFF CACHE BOOL "" FORCE)
set(CURL_ZSTD OFF CACHE BOOL "" FORCE)
set(CURL_BROTLI OFF CACHE BOOL "" FORCE)

add_subdirectory(../libs/curl-8.22.0 ${CMAKE_BINARY_DIR}/curl-build)

target_link_libraries(fx PRIVATE CURL::libcurl)
target_include_directories(fx PUBLIC include)

set_target_properties(fx PROPERTIES PREFIX "")
if (UNIX)
    target_link_libraries(fx PRIVATE m)
endif ()

option(FX_BUILD_TESTS "Build fx tests" ON)
if(FX_BUILD_TESTS)
    enable_testing()
    add_subdirectory(tests)
endif()