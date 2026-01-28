
/**
 * This file is used for suppress Qt Creator error reporting, in editor.
 *
 * Requires CMakeLists.txt:
 *
 * if(MSVC OR APPLE OR UNIX)
 *  add_library(anson_ide_indexer EXCLUDE_FROM_ALL
 *      ${CMAKE_CURRENT_SOURCE_DIR}/clangd_stub.cpp
 *  )
 *  target_link_libraries(anson_ide_indexer PRIVATE anson)
 * endif()
 *
 * CLangd won't compile any header files without used in the target compile.
 */
#include "io/odysz/rttr.hpp"
