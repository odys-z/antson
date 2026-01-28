
include(FetchContent)

FetchContent_Declare(
  googletest
  GIT_REPOSITORY https://github.com/google/googletest.git
  GIT_TAG        v1.15.2
)
FetchContent_MakeAvailable(googletest)

include(GoogleTest)

add_executable(unit_tests
  semantier.h
  t01_enttmeta.cpp
)

find_package(nlohmann_json CONFIG REQUIRED)

target_link_libraries(unit_tests 
    PRIVATE 
        anson 
        nlohmann_json::nlohmann_json
        gtest_main
)
