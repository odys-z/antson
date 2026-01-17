cmake_minimum_required(VERSION 3.22)   # 3.20 is ok, but 3.22+ is nicer in 2026
project(anson LANGUAGES CXX)

set(CMAKE_CXX_STANDARD    23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS  OFF)
set(FETCHCONTENT_QUIET OFF CACHE BOOL "" FORCE)

# Optional but recommended
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Main library (header-only)
add_subdirectory(src)

# Tests (off by default in many projects - easier for consumers)
option(ANSON_BUILD_TESTS "Build unit tests" ON)
if(ANSON_BUILD_TESTS AND (PROJECT_IS_TOP_LEVEL OR ANSON_BUILD_TESTS_FORCE))
    enable_testing()
    add_subdirectory(tests)
endif()

# Optional: make package installable later
# include(GNUInstallDirs)
# ...
