include(GoogleTest)

# Create the test executable
add_executable(
    anson_tests
    rttr/node.h
    rttr/node.cpp
    test_01_rttr_tutor.cpp
    test_02_deserializer.cpp
)

add_custom_command(TARGET anson_tests POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
    # "C:/Users/Alice/github/rttr-0.9.6/install/bin/rttr_core.dll"
    "C:/Users/Alice/github/rttr-0.9.6/install/bin/rttr_core_d.dll"
    $<TARGET_FILE_DIR:anson_tests>
)

# Link to our source library and GoogleTest
# gtest_main provides the main() function for the tests
target_link_libraries(anson_tests PRIVATE 
    RTTR::Core
    nlohmann_json::nlohmann_json
    anson
    gtest_main
)

# Discover and add tests to CTest
gtest_discover_tests(anson_tests)
