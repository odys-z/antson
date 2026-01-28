/**
 * The eqivalent of gen.antlr.json + JSONAnsonListener
 */
#pragma once
#include <nlohmann/json.hpp>
#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>
#include <entt/entt.hpp>
#include <vector>
#include <string>

// using json = nlohmann::json;
// using namespace nlohmann;

namespace anson {

using namespace entt::literals;

class EnTTSaxParser : public nlohmann::json_sax<nlohmann::json> {
private:
    std::vector<entt::meta_any> stack;
    entt::id_type active_key{0};

    // Helper to set values on the current object in the stack
    template<typename T>
    void set_value(T&& val) {
        if (!stack.empty() && active_key != 0) {
            auto data = stack.back().type().data(active_key);
            if (data) {
                data.set(stack.back(), std::forward<T>(val));
            }
        }
    }

public:
    bool start_object(std::size_t size) override {
        if (active_key != 0 && !stack.empty()) {
            auto data = stack.back().type().data(active_key);
            if (data) {
                stack.push_back(data.get(stack.back()));
            }
        } else if (stack.empty()) {
            // This is the root object, we assume the caller set it up
            return true;
        }
        return true;
    }

    bool key(string_t& val) override {
        active_key = entt::hashed_string{val.c_str()};
        return true;
    }

    bool end_object() override {
        if (stack.size() > 1) stack.pop_back();
        active_key = 0;
        return true;
    }

    // 2. Data Type Handling
    bool number_float(number_float_t val, const string_t&) override {
        set_value(static_cast<float>(val));
        return true;
    }

    bool number_integer(number_integer_t val) override {
        set_value(static_cast<int>(val));
        return true;
    }

    bool string(string_t& val) override {
        set_value(val);
        return true;
    }

    bool boolean(bool val) override {
        set_value(val);
        return true;
    }

    // 3. Boilerplate requirements
    bool null() override { return true; }
    bool number_unsigned(number_unsigned_t val) override { set_value(static_cast<int>(val)); return true; }
    bool binary(binary_t&) override { return true; }
    bool start_array(std::size_t) override { return true; }
    bool end_array() override { return true; }
    bool parse_error(std::size_t, const std::string&, const nlohmann::detail::exception&) override { return false; }

    // Root Management
    void set_root(entt::meta_any instance) { stack.push_back(instance); }
};
}
