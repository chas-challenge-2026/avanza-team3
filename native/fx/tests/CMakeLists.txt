set(BUILD_GMOCK OFF CACHE BOOL "" FORCE)
set(INSTALL_GTEST OFF CACHE BOOL "" FORCE)
add_subdirectory(../../libs/googletest-1.18.0 ${CMAKE_BINARY_DIR}/googletest-build)

add_executable(fx_test fx_test.cpp)
target_link_libraries(fx_test PRIVATE fx gtest_main)

add_test(NAME fx_test COMMAND fx_test)