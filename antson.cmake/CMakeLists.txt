cmake_minimum_required(VERSION 3.30)
project(anson.cmake LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Generate compile_commands.json for clangd/IDE integration (single-config only)
if(NOT CMAKE_CONFIGURATION_TYPES)
  set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
endif()


find_package(EnTT CONFIG REQUIRED)
find_package(nlohmann_json CONFIG REQUIRED)

add_subdirectory(src)
add_subdirectory(tests)
