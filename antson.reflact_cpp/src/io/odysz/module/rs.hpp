#ifndef io_odysz_module_rs_hpp
#define io_odysz_module_rs_hpp

#include <vector>
#include <string>
#include <map>
#include "io/odysz/anson.hpp"

namespace anson {
class AnResultset : public Anson {
    string type = "io.odysz.module.rs.AnResultset";

private:
    int colCnt = 0;
    /**current row index, start at 1. */
    int rowIdx = -1;

    int rowCnt = 0;

    map<string, int> indices0;

public:

    int currentRow() { return rowIdx; }

    /** row indices, start at 0 */
    map<string, int> Indices0() { return indices0; }

};

}

#endif
