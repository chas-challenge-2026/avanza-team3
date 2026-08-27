cmake_minimum_required(VERSION 3.20)
project(risk C)

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

add_library(risk SHARED src/risk.c)
target_include_directories(risk PUBLIC include)
set_target_properties(risk PROPERTIES PREFIX "")
if(UNIX)
    target_link_libraries(risk PUBLIC m)
endif()

option(RISK_BUILD_TESTS "Build risk tests" ON)
if(RISK_BUILD_TESTS)
    enable_testing()
    add_subdirectory(tests)
endif()
