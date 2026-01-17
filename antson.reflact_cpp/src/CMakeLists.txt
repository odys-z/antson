# src/CMakeLists.txt

# ────────────────────────────────────────────────
# Fetch reflect-cpp (header-only, C++20)
# ────────────────────────────────────────────────
include(FetchContent)

FetchContent_Declare(
    reflect-cpp
    GIT_REPOSITORY https://github.com/getml/reflect-cpp.git
    GIT_TAG        main                    # or a specific tag / commit if you prefer stability
    # GIT_TAG      v0.2.0                  # ← check releases page; use tagged version when available
    # UPDATE_DISCONNECTED ON               # optional: faster re-configure if already cloned
    GIT_SHALLOW    TRUE
)

# Important: tell FetchContent we do NOT want to build tests / examples
set(REFLECT_CPP_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(REFLECT_CPP_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)

FetchContent_MakeAvailable(reflect-cpp)

# ────────────────────────────────────────────────
# Our header-only library
# ────────────────────────────────────────────────
add_library(anson INTERFACE)
add_library(anson::anson ALIAS anson)

# Public headers (your own)
target_sources(anson INTERFACE
    FILE_SET HEADERS
    BASE_DIRS ${CMAKE_CURRENT_SOURCE_DIR}
    FILES
        io/odysz/module/rs.hpp
        io/odysz/semantics.hpp
        io/odysz/anson.hpp
        io/odysz/semantic/jprotocol.hpp
        io/odysz/module/rs.hpp
)

# ─── Include paths ───────────────────────────────────────
target_include_directories(anson INTERFACE
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
    $<INSTALL_INTERFACE:include>
)

# ─── Link / propagate reflect-cpp ─────────────────────────
target_link_libraries(anson INTERFACE
    reflectcpp::reflectcpp          # ← this is the exported target name
)

# Enforce C++20 (reflect-cpp requires C++20)
target_compile_features(anson INTERFACE cxx_std_20)
# If you still want to force C++23 for your own code:
# target_compile_features(anson INTERFACE cxx_std_23)

# Optional: if you use any compile definitions
# target_compile_definitions(anson INTERFACE ANSON_USE_REFLECT_CPP=1)
