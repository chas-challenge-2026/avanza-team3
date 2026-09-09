cmake_minimum_required(VERSION 3.20)
project(fx CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

add_executable(fx src/fx.cpp)
# add_library(fx SHARED src/fx.cpp)
target_include_directories(fx PUBLIC include)
set_target_properties(fx PROPERTIES PREFIX "")
if(UNIX)
    target_link_libraries(fx PUBLIC m)
endif()