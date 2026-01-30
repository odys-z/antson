include(FetchContent)

# 1. Fetch GoogleTest (the modern standard for 2026)

FetchContent_Declare(
  googletest
  GIT_REPOSITORY https://github.com/google/googletest.git
  GIT_TAG        v1.15.2 # Use latest stable 2026 version
)
# For speed in 2026, we often disable gtest's own tests/install
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# 2. Create the test executable
add_executable(unit_tests 
    # common_test.cpp 
    # utils_test.cpp
    anson_test.cpp
    io/oz/anclient/ipcagent.hpp
    io/oz/anclient/soketier2.h
)

# 3. Link to library and the testing framework
# "MyLib::IO" is the alias we created in the src folder
target_link_libraries(unit_tests 
    PRIVATE 
        anson::anson
        GTest::gtest_main
)

# 4. Register the test with CTest
# This allows you to run 'ctest' from the build directory
add_test(NAME AllLibraryTests COMMAND unit_tests)
