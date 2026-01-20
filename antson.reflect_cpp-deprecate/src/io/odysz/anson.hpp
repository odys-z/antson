#ifndef io_odysz_anson_hpp
#define io_odysz_anson_hpp

#include <memory>
#include <string>
#include <sstream>
#include <fstream>
#include <concepts>
#include <stdexcept>
#include <string_view>
#include <format>
#include <rfl/json.hpp>
#include <rfl.hpp>

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

    IJsonable& toBlock(ostream& stream, JsonOpt& opt);

    string toBlock(JsonOpt& opt)
    {
        ostringstream bos;// = new ByteArrayOutputStream();
        this->toBlock(bos, opt);
        return bos.str();
    }

    // template<std::derived_from<IJsonable> T = IJsonable>
    string toBlock();

    IJsonable& toJson(ostringstream& buf) ;
};

class Anson : public IJsonable {
    string type;
public:
    virtual ~Anson() = default;

    template<std::derived_from<Anson> T = Anson>
    static T* fromJson(const string& json) {
        return rfl::json::read<T>(json);
    }

    template<std::derived_from<Anson> T = Anson>
    static T* fromPath(const string& path) {
        ifstream fs(path);
        return rfl::json::read<T>(fs);
    }

    void toPath(const string& path);

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

inline string IJsonable::toBlock()
{
    ostringstream bos;
    JsonOpt opt;
    this->toBlock(bos, opt);
    return bos.str();
}

inline IJsonable& IJsonable::toBlock(ostream& os, JsonOpt& opt)
{
    this->toBlock(os, opt);
    return *this;
}

inline IJsonable& IJsonable::toJson(ostringstream& buf)
{
    JsonOpt opt;
    ((IJsonable*)this)->toBlock(buf, opt);
    return *this;
}

inline void Anson::toPath(const string& path) {
    JsonOpt defaultopt;
    this->toPath(path, defaultopt);
}

inline void Anson::toPath(const string& path, JsonOpt& opt) {
    ofstream fout(path);
    ((IJsonable*)this)->toBlock(fout, opt);
}

} // namespace anson

#endif // ANSON_HPP
