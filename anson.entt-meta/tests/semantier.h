#pragma once

#include <io/odysz/anson.h>
#include <io/odysz/jprotocol.h>


namespace anson {

class PathsPage : public Anson {
public:
    PathsPage(string device) : Anson(), device(device) {}

    string device;
    size_t start;
    size_t end;

    map<string, vector<string>>clientPaths;

};

/**
 * @brief The DocSyncReq class
 * java type: io.odysz.semantics.SemanticObject
 */
class DocsReq : public UserReq {
public:
    string synuri;
    string docTabl;
    DocsReq(string a) : UserReq(a) {}

};
}
