cmake_minimum_required(VERSION 3.20)
project(risk_module C)

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_STANDARD_REQUIRED ON)

add_library(risk SHARED src/risk.c)
target_include_directories(risk PUBLIC include)
set_target_properties(risk PROPERTIES PREFIX "" OUTPUT_NAME "risk")

add_executable(risk_test src/main.c)
target_link_libraries(risk_test PRIVATE risk)
