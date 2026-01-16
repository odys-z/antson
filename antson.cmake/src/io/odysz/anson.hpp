#ifndef io_odysz_anson_hpp
#define io_odysz_anson_hpp

#include <memory>
#include <string>
#include <sstream>
#include <concepts>
#include <stdexcept>
#include <glaze/glaze.hpp>
#include <glaze/util/type_traits.hpp>
#include <string_view>
#include <format>
// #include <bits/stl_map.h>

using namespace std;

namespace anson {

class JsonOpt;

class IJsonable {
public:
    virtual ~IJsonable() = default;

public:
    class JsonableFactory {
        virtual unique_ptr<IJsonable> fromJson(const string& json);
    };

    template <typename T>
    IJsonable& toBlock(ostream& stream, JsonOpt& opt);

    template <typename T>
    string toBlock(JsonOpt& opt)
    {
        ostringstream bos;// = new ByteArrayOutputStream();
        this->toBlock<T>(bos, opt);
        return bos.str();
    }

    template <typename T>
    string toBlock();

    template <typename T>
    IJsonable& toJson(ostringstream& buf) ;
};

class Anson : public IJsonable {
    string type;
public:
    virtual ~Anson() = default;

    template<std::derived_from<Anson> T = Anson>
    static T* fromJson(const string& json) {
        auto p = make_unique<T>();

        constexpr auto options = glz::opts{
            .error_on_unknown_keys = false,
            .error_on_missing_keys = false,
            .partial_read = true
        };

        auto ec = glz::read<options>(*p, json);

        if (ec) {
            throw std::runtime_error("Glaze parse error: " + json);
        }

        // return p;
        return p.get();
    }

    template<derived_from<Anson> T = Anson>
    static unique_ptr<T> fromPath(const string& path) {
        auto p = std::make_unique<T>();

        constexpr auto options = glz::opts{
            .error_on_unknown_keys = false,
            .error_on_missing_keys = false,
            .partial_read = true
        };

        auto ec = glz::read_file_json<options>(*p, path, string{});

        if (ec) {
            throw runtime_error("Glaze parse error at file: " + path);
        }

        return p;
    }

    template<derived_from<Anson> T = Anson>
    void toPath(const string& path);

    template<derived_from<Anson> T = Anson>
    void toPath(const string& path, JsonOpt& opt);

    // string toBlock();

// private:
//     template <typename T, typename V>
//     T* try_get_animal(V& variant_obj) {
//         return std::get_if<T>(&variant_obj);
//     }
};

class JsonOpt : public Anson {

public:

    virtual ~JsonOpt() = default;

    bool beauty;
    JsonOpt& beautify(bool opt) {
        this->beauty = opt;
        return *this;
    }

    struct glaze {
        using T = JsonOpt;
        static constexpr auto value = glz::object(
            "type", [](auto&&) { return "io.odysz.anson.JsonOpt"; },
            "beauty", &T::beauty);
    };
};

///////////////////////////////////////////////////////////////////////////////////
/// \brief IJsonable::toBlock
/// \return
///
///
template <typename T>
inline string IJsonable::toBlock()
{
    JsonOpt defaultopt;
    return ((IJsonable*)this)->toBlock<T>(defaultopt);
}

template <typename T>
inline IJsonable& IJsonable::toJson(ostringstream& buf)
{
    JsonOpt opt;
    ((IJsonable*)this)->toBlock<T>(buf, opt);
    return *this;
}

template <std::derived_from<Anson> T>
inline void Anson::toPath(const string& path) {
    JsonOpt defaultopt;
    this->toPath<T>(path, defaultopt);
}

template <std::derived_from<Anson> T>
inline void Anson::toPath(const string& path, JsonOpt& opt) {
    // JsonOpt defaultopt; // = new JsonOpt();
    std::ofstream fout(path);
    ((IJsonable*)this)->toBlock<T>(fout, opt);
}

template <typename T>
inline IJsonable& IJsonable::toBlock(ostream& os, JsonOpt& opt)
{
    glz::ostream_buffer<> buffer{os};

    if (opt.beauty)
        auto ec = glz::write<glz::opts{.prettify=true}>(static_cast<T&>(*this), buffer);
    else
        auto ec = glz::write<glz::opts{.prettify=false}>(static_cast<T&>(*this), buffer);
    return *this;
}

} // namespace anson

// Expected signature for write callback: returns the value to serialize
template <std::derived_from<anson::Anson> T>
std::string_view write_message_type(const T&) noexcept {
    static_assert(false, "No type tag defined for this message type");
    return "Only incorrect of _antype_() in {} usage can reach here.";
}

template <class T>
constexpr std::string_view get_expected_type_tag() noexcept {
    static_assert(false, "You must specialize get_expected_type_tag for this type");
}

// Default: no-op / skip on read (or validate if you want)
template <std::derived_from<anson::Anson> T>
void read_message_type(T&, glz::is_context auto&& ctx, auto&& it, auto&& end) noexcept {
    std::string_view tag{};
    glz::parse<glz::JSON>::op<glz::opts{}>(tag, ctx, it, end);
    std::string_view expected = get_message_type_tag(T{});
    if (tag != expected) {
        ctx.error = format("expecting {} but is {}", expected, tag);
        return;
    }
}

// void ignore_type_tag(anson::Anson&, glz::is_context auto&& ctx, auto&& it, auto&& end) noexcept {
//     std::string_view ignored{};
//     glz::parse<glz::JSON>::op<glz::opts{}>(ignored, ctx, it, end);
//     if (it != end && *it == ',') ++it;
// }

#define _ANSON_TYPE_(Class, ExpectedStr) \
glz::custom<\
  &read_message_type<Class, ExpectedStr>, \
  &write_message_type<Class> >


#endif // ANSON_HPP
