# No project() here either

include(FetchContent)

FetchContent_Declare(
    googletest
    GIT_REPOSITORY https://github.com/google/googletest.git
    GIT_TAG        v1.15.2
    # v1.16.0 or main if you really want bleeding edge, but v1.15 is very safe
)

# Recommended settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
set(INSTALL_GTEST        OFF CACHE BOOL "" FORCE)
set(BUILD_GMOCK          OFF CACHE BOOL "" FORCE)   # if you don't need mocks

FetchContent_MakeAvailable(googletest)

# ────────────────────────────────────────
# Test executable
# ────────────────────────────────────────

add_executable(unit_tests
    hello_rfl_test.cpp
    # anson_test.cpp
    io/oz/anclient/ipcagent.hpp
    io/oz/anclient/soketier.h
)

target_link_libraries(unit_tests
    PRIVATE
        anson::anson
        GTest::gtest
        GTest::gtest_main
)

# If using C++23 in tests too
target_compile_features(unit_tests PRIVATE cxx_std_23)

# Discover tests (very convenient)
# include(GoogleTest)
# gtest_discover_tests(unit_tests)

# Or classic way:
# add_test(NAME Anson.All COMMAND unit_tests)

add_test(NAME AllLibraryTests COMMAND unit_tests)
