cmake_minimum_required(VERSION 3.20.0)

project(${PROJECT_NAME} LANGUAGES CXX)

add_compile_options(/utf-8)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(${PROJECT_NAME} INTERFACE)
add_library(${PROJECT_NAME}::${PROJECT_NAME} ALIAS ${PROJECT_NAME})

# List headers so they appear in IDEs (Visual Studio, Xcode, etc.)
# Not strictly required for build, but very useful
target_sources(${PROJECT_NAME} INTERFACE
    io/odysz/semantics.hpp
    io/odysz/anson.hpp
    io/odysz/semantic/jprotocol.hpp
    io/odysz/module/rs.hpp
)

# Make sure users of this library can find the headers
target_include_directories(${PROJECT_NAME} INTERFACE
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
    $<INSTALL_INTERFACE:include>   # for when you add install() rules later
)

# ── Dependencies ────────────────────────────────────────────────────────────

include(FetchContent)

FetchContent_Declare(
    glaze
    GIT_REPOSITORY https://github.com/stephenberry/glaze.git
    GIT_TAG        main   # consider a specific tag/SHA in production
)

FetchContent_MakeAvailable(glaze)

target_link_libraries(${PROJECT_NAME} INTERFACE glaze::glaze)



