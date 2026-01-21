# Anson.cmake source, pure header files

add_library(anson INTERFACE
    io/odysz/anson.hpp
    io/odysz/jprotocol.hpp
    io/odysz/rttr.hpp
)
add_library(anson::anson ALIAS anson)

target_include_directories(anson INTERFACE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(anson INTERFACE 
    RTTR::Core
    nlohmann_json::nlohmann_json
)
