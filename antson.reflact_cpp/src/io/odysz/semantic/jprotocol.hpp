#ifndef io_odysz_semantic_jprotocol_hpp
#define io_odysz_semantic_jprotocol_hpp

#include "io/odysz/anson.hpp"
#include "io/odysz/module/rs.hpp"

namespace anson {
enum class Port : int {
    heartbeat = 0, //("ping.serv"),
    session, //("login.serv"),
    query, //("r.serv"),
    update, //("u.serv"),
    insert, // ("c.serv"),
    del, //("d.serv"),
    echo, //("echo.less"),

    /** serv port for downloading json/xml file or uploading a file.<br>
         * see io.odysz.semantic.jserv.file.JFileServ in semantic.jserv. */
    file,
    // ("file.serv"),

    /**
         * Any user defined request using message body of subclass of JBody must use this port
         * @deprecated since 1.4.36
         */
    user, //("user.serv11"),

    /** experimental */
    userstier, //("users.tier"),
    /** semantic tree of dataset extensions<br>
         * see io.odysz.semantic.ext.SemanticTree in semantic.jserv. */
    stree, //("s-tree.serv"),

    /** @deprecated replaced by {@link #stree} */
    stree11, //("s-tree.serv11"),

    /** dataset extensions<br>
         * see io.odysz.semantic.ext.Dataset in semantic.jserv. */
    dataset, //("ds.serv"),

    /** @deprecated replaced by {@link #dataset} */
    dataset11, //("ds.serv11"),

    /** ds.tier, dataset's semantic tier */
    datasetier, //("ds.tier"),

    /** document manage's semantic tier */
    docstier, //("docs.tier"),

    /**
         * Synode tier service: sync.tier
         * @since 2.0.0
         */
    syntier //("sync.tier");
};

class AnsonBody : public Anson {
protected:
    string uri;
    string a;

public:
    virtual ~AnsonBody() = default;
};

template <std::derived_from<AnsonBody> T>
class AnsonMsg : public Anson {
public:
    ~AnsonMsg() = default;

    AnsonMsg(Port p) : port(p) { }
    AnsonMsg(Port p, T& body) : port(p), body({body}) {}

    Port port;
    vector<T> body;
};

class AnsonResp : public AnsonBody {
    // string type = "io.odysz.semantic.jprotocol.AnsonResp";

public:
    ~AnsonResp() = default;

    string m;
    vector<AnResultset> rs;
};
}

#endif
