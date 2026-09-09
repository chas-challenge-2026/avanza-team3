cmake_minimum_required(VERSION 3.20)
project(fx CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

set(HTTPLIB_INSTALL OFF CACHE BOOL "" FORCE)
set(HTTPLIB_USE_NON_BLOCKING_GETADDRINFO OFF CACHE BOOL "" FORCE)
add_subdirectory(../libs/httplib httplib-build)

add_executable(fx src/fx.cpp)
target_compile_definitions(fx PRIVATE
        WINVER=0x0A00
        _WIN32_WINNT=0x0A00
        NTDDI_VERSION=0x0A000000
)
target_include_directories(fx PRIVATE include)
target_link_libraries(fx PRIVATE httplib::httplib)
set_target_properties(fx PROPERTIES PREFIX "")
if(UNIX)
    target_link_libraries(fx PRIVATE m)
endif()