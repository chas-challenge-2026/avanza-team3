add_executable(risk_test risk_test.c ../../third_party/unity/unity.c)
target_include_directories(risk_test PRIVATE ../../third_party/unity)
target_compile_definitions(risk_test PRIVATE UNITY_INCLUDE_DOUBLE)
target_link_libraries(risk_test PRIVATE risk)

add_test(NAME risk_test COMMAND risk_test)
