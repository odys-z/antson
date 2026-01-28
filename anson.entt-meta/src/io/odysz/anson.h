#pragma once
#include <string>
#include <iostream>
#include <nlohmann/json.hpp>
#include <entt/meta/meta.hpp>
#include <entt/entt.hpp>

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

};

class SemanticObject : public Anson {

};
}

