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

    /** @see {columes()} */
    map<string, vector<glz::generic>> colnames;
    /**
     * current row index, start from 1. If used for {@link #getRowAt(int)}, must - 1.
     * @return current row index
     */
public:
    /** any for java Object? */
    vector<vector<glz::generic>> results;

    int currentRow() { return rowIdx; }

    /** row indices, start at 0 */
    map<string, int> Indices0() { return indices0; }

    /**col-index start at 1, map: [alais(upper-case), [col-index, db-col-name(in raw case)]<br>
     * case 1<pre>
     * String colName = rsMeta.getColumnLabel(i).toUpperCase();
     * colnames.put(colName, new Object[] {i, rsMeta.getColumnLabel(i)});
     * </pre>
     * case 2<pre>
     * for (String coln : colnames.keySet())
     * colnames.put(coln.toUpperCase(), new Object[] {colnames.get(coln), coln});
     * </pre>
     */
    // @AnsonField(valType="[Ljava.lang.Object;")
    map<string, vector<glz::generic>> Colnames() { return colnames; }

    // @AnsonField(ignoreTo = true)
    // private ResultSet rs;


    struct glaze {
        using T = AnResultset;
        static constexpr auto value = glz::object(
            "type",      &T::type,
            "colCnt",    &T::colCnt,
            "rowCnt",    &T::rowCnt,
            "indices0",  &T::indices0,
            "colnames",  &T::colnames,
            "results",   &T::results
        );
    };
};

}

#endif
