#include <gtest/gtest.h>
#include <entt/meta/meta.hpp>
#include <entt/meta/factory.hpp>
#include <nlohmann/json.hpp>
#include <iostream>
#include <io/odysz/jprotocol.h>
#include "io/odysz/json.h"

using json = nlohmann::json;
using namespace entt::literals;
using namespace anson;

// 1. Setup Reflection
void register_meta() {
    entt::meta_factory<EchoReq>()
        .type("EchoReq"_hs)
        .data<&EchoReq::echo>("echo"_hs, "echo");

    entt::meta_factory<AnsonMsg<EchoReq>>()
        .type("AnsonMsg"_hs)
        .data<&AnsonMsg<EchoReq>::body>("body"_hs, "body");
}

template<typename T>
void load_json(const std::string& raw_json, T& out_obj) {
    EnTTSaxParser handler;

    // Wrap our existing C++ instance so the parser can fill it
    handler.set_root(entt::forward_as_meta(out_obj));

    // Execute SAX parse
    if (nlohmann::json::sax_parse(raw_json, &handler)) {
        // Success: out_obj is now populated
    }
}

// 2. Generic Convert
json convert(entt::meta_any instance) {
    json j;
    auto type = instance.type();
    for(auto [id, data] : type.data()) {
        auto value = data.get(instance);
        // if(value.type() == entt::resolve<int>()) {
        //     j[id] = value.cast<int>();
        // } else if(value.type() == entt::resolve<std::string>()) {
        //     j[id] = value.cast<std::string>();
        // }
        if (auto* f = value.try_cast<string>())
            j[id] = value.cast<string>();
    }
    return j;
}

std::string serialize_to_json(entt::meta_any instance) {
    using namespace entt::literals;

    // 1. Resolve the type of the instance provided
    auto type = instance.type();
    if (!type) return "{}";

    nlohmann::json j;

    // 2. Iterate through all data members registered in meta
    for (auto [id, data] : type.data()) {
        auto value = data.get(instance);

        // 3. Convert the meta_any value to a JSON-compatible type
        // We check for types we expect (float, int, etc.)
        if (auto* f = value.try_cast<float>()) {
            j[id] = *f; // EnTT uses the hashed string ID as the key
        } else if (auto* i = value.try_cast<int>()) {
            j[id] = *i;
        }
        // Note: For real-world use, you can expand this to strings, bools, etc.
    }

    return j.dump(2); // 2-space indentation
}


TEST(HELLO, ENTT_META) {
    register_meta();

    AnsonMsg<EchoReq> msg{Port::echo};
    
    // Wrap in meta_any and serialize
    // json result = convert(entt::forward_as_meta(msg));
    // std::cout << "JSON Object: " << result.dump(4) << std::endl;

    std::cout << serialize_to_json(msg);
}
