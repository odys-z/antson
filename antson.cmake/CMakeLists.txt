cmake_minimum_required(VERSION 3.15)
project(anson.cmake VERSION 0.0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(RTTR_BUILD_UNIT_TESTS OFF CACHE BOOL "" FORCE)
set(RTTR_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(RTTR_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(RTTR_BUILD_INSTALLER OFF CACHE BOOL "" FORCE)
set(CMAKE_COMPILER_IS_MSVC 1)
set(RTTR_BUILD_UNIT_TESTS OFF CACHE BOOL "" FORCE)
set(BUILD_UNIT_TESTS OFF CACHE BOOL "" FORCE)

add_compile_options(/WX-)

# RTTR needs to be installed locally
# See readme for install RTTR
find_package(RTTR REQUIRED Core)

include(FetchContent)

FetchContent_Declare(
    json
    URL https://github.com/nlohmann/json/releases/download/v3.11.3/json.tar.xz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(json)

# 2. Setup GoogleTest via FetchContent
FetchContent_Declare(
  googletest
  GIT_REPOSITORY https://github.com/google/googletest.git
  GIT_TAG        v1.15.2
)

# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# 3. Add Subdirectories
add_subdirectory(src)
add_subdirectory(tests)

