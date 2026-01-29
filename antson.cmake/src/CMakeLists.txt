# Anson.cmake source, pure header files

cmake_minimum_required(VERSION 3.30)
project(anson.cmake LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(anson INTERFACE
    io/odysz/anson.h
    io/odysz/jprotocol.h
    io/odysz/json.h
)

add_library(anson::anson ALIAS anson)

# 1. Register the Header Set
target_sources(anson INTERFACE
    FILE_SET HEADERS
    BASE_DIRS ${CMAKE_CURRENT_SOURCE_DIR}
    FILES
        io/odysz/anson.h
        io/odysz/jprotocol.h
        io/odysz/json.h
)

# if(MSVC OR APPLE OR UNIX)
#     # Create a target that is NEVER built, but exists for Clangd
#     # add_library(anson_ide_indexer EXCLUDE_FROM_ALL
#         # ${CMAKE_CURRENT_SOURCE_DIR}/clangd_stub.cpp
#     # )
#     # target_link_libraries(anson_ide_indexer PRIVATE anson)
# endif()


target_include_directories(anson INTERFACE
    ${CMAKE_CURRENT_SOURCE_DIR}
    $<INSTALL_INTERFACE:include>
)

target_link_libraries(anson INTERFACE
    EnTT::EnTT
    nlohmann_json::nlohmann_json
)
