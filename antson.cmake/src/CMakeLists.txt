# Anson.cmake source, pure header files

add_library(anson INTERFACE
    io/odysz/anson.hpp
    io/odysz/jprotocol.hpp
    io/odysz/rttr.hpp
)

add_library(anson::anson ALIAS anson)

# 1. Register the Header Set
target_sources(anson INTERFACE
    FILE_SET HEADERS
    BASE_DIRS ${CMAKE_CURRENT_SOURCE_DIR}
    FILES
        io/odysz/anson.hpp
        io/odysz/jprotocol.hpp
        io/odysz/rttr.hpp
)

# 2. Add the stub file separately (No FILE_SET keyword for this part)
# target_sources(anson INTERFACE
#     "${CMAKE_CURRENT_SOURCE_DIR}/clangd_stub.cpp"
# )

if(MSVC OR APPLE OR UNIX)
    # Create a target that is NEVER built, but exists for Clangd
    add_library(anson_ide_indexer EXCLUDE_FROM_ALL
        ${CMAKE_CURRENT_SOURCE_DIR}/clangd_stub.cpp
    )
    target_link_libraries(anson_ide_indexer PRIVATE anson)
endif()


target_include_directories(anson INTERFACE
    ${CMAKE_CURRENT_SOURCE_DIR}
    $<INSTALL_INTERFACE:include>
)

target_link_libraries(anson INTERFACE
    RTTR::Core
    nlohmann_json::nlohmann_json
)
