#ifndef io_odysz_anson_hpp
#define io_odysz_anson_hpp

#include <memory>
#include <string>
#include <sstream>
#include <concepts>
#include <stdexcept>
#include <string_view>
#include <format>

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

        return p.get();
    }

    template<derived_from<Anson> T = Anson>
    static unique_ptr<T> fromPath(const string& path) {
        auto p = std::make_unique<T>();

        return p;
    }

    template<derived_from<Anson> T = Anson>
    void toPath(const string& path);

    template<derived_from<Anson> T = Anson>
    void toPath(const string& path, JsonOpt& opt);
};

class JsonOpt : public Anson {

public:

    virtual ~JsonOpt() = default;

    bool beauty;
    JsonOpt& beautify(bool opt) {
        this->beauty = opt;
        return *this;
    }

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
    std::ofstream fout(path);
    ((IJsonable*)this)->toBlock<T>(fout, opt);
}

template <typename T>
inline IJsonable& IJsonable::toBlock(ostream& os, JsonOpt& opt)
{
    return *this;
}

} // namespace anson

#endif // ANSON_HPP
