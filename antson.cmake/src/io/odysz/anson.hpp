#pragma once
#include <string>
#include <iostream>
#include <rttr/registration>
#include <nlohmann/json.hpp>

#define NL '\n'

using namespace  std ;

namespace anson {
/**
 * @brief The Anson class
 * java type: io.odysz.anson.Anson
 */
class Anson {
public:
    std::string type;

    Anson() { cout << "defalut contructor" << NL ; }
    Anson(string t) : type(t) { cout << "override contructor, type = " << t << NL ; }

    RTTR_ENABLE()
};

class SemanticObject : public Anson {

    RTTR_ENABLE(Anson)
};
}

