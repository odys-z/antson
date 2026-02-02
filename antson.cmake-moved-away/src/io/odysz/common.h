#include <string>

namespace anson {
using namespace std;

class JavaEnum {
public:
    string v;

    /**
     * Returns the string representation of this share flag,
     * mimicking Java's Enum.name()
     */
    const string& name() const { return v; }
};

}
