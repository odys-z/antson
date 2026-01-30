cmake_minimum_required(VERSION 3.20) # C++23 support requires 3.20+

project(anson LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

enable_testing()

add_subdirectory(src)
add_subdirectory(tests)
