[![CMake - FetchContent](https://img.shields.io/badge/CMake-FetchContent-blue?logo=cmake)](https://github.com/antson/antson#integration)

# About

The C++ 17 version of Anclient. 

  *This module is supposed to evolved to C++ 26, which extended the static reflection, once it's industrial available and of couse, the devloper's bandwidth is allowed.*

# Quick Start

### Prerequisite

  * C++ 17 Compiler, CMake 3.30
  * [rttrorg/RTTR](https://github.com/rttrorg/rttr) ([Install it shouldn't take hours](#build--install-rttr))

### 📦 Installation (CMake)

  ```cmake
  include(FetchContent)
  FetchContent_Declare(
    anson
    GIT_REPOSITORY [https://github.com/antson/antson.git](https://github.com/antson/antson.git)
    GIT_TAG        cmake-v0.1.0 # replace with latest
    SOURCE_SUBDIR  antson.cmake
  )
  FetchContent_MakeAvailable(antson)
  ```

# If you need dive deep

## A Cheap survey on C++ reflection

* [Glaze](https://github.com/stephenberry/glaze.git) 

    With template an macro preprocessing, and not possible to forward request according to envelope types.
    The read / write customize can not adapted easily to deserialize envelope.type. 

    antson.cmake tag: glaze-deprecate

* [getml/Reflect-cpp](https://github.com/getml/reflect-cpp)

    Also by tempalte, and uses *flatten* for super class's fields. If implement with non-aggregate types,
    that makes even instancing a class is complicate, not to mention that envelopes cannot be subclasses
    of Anson.

```
    // subclass makes compile errors.
    struct EchoReq : public AnsonBody {
        rfl::Flatten<AnsonBody> base;
        string echo;

        // Not here, requires aggregate types. 
        // EchoReq() { this->base.value_.anson.value_.type = "io.oz.echo.EchoReq"; }
        // EchoReq(string& echo) : EchoReq() {
        //     this->echo = echo;
        // }
    };
```

The compiler errors:

```
    string json = rfl::json::write(echobd);

    ~\github\antson\antson.reflact_cpp\build\MSVC2022_64\_deps\reflect-cpp-src\include\rfl\internal\bind_to_tuple.hpp:43: error: C3643: "EchoReq": 无法分解 "EchoReq" 和 "AnsonBody" 中具有非静态数据成员的类型
    ~\github\antson\antson.reflact_cpp\build\MSVC2022_64\_deps\reflect-cpp-src\include\rfl\internal\bind_to_tuple.hpp(43): error C3643: "EchoReq": 无法分解 "EchoReq" 和 "AnsonBody" 中具有非静态数据成员的类型
    ~\github\antson\antson.reflact_cpp\tests\hello_rfl_test.cpp(84): note: 参见“EchoReq::echo”的声明
    ~\github\antson\antson.reflact_cpp\tests\hello_rfl_test.cpp(68): note: 参见“AnsonBody::anson”的声明
    ~\github\antson\antson.reflact_cpp\build\MSVC2022_64\_deps\reflect-cpp-src\include\rfl\internal\bind_to_tuple.hpp(43): note: 模板实例化上下文(最早的实例化上下文)为
    ~\github\antson\antson.reflact_cpp\tests\hello_rfl_test.cpp(103): note: 查看对正在编译的函数 模板 实例化“std::string rfl::json::write<,EchoReq>(const _T0 &,const yyjson_write_flag)”的引用
```

    antson.cmake tag: reflect-cpp-deprecate

* [RTTR](https://www.rttr.org/)

   This is the dynamic way. And Unreal Engine has reflection functions based on it.

   One of the restrictions of RTTR is it requirs one and only one RTTR_REGISTRATION block.
   See [#106](https://github.com/rttrorg/rttr/issues/106).


<a id="build-install-rttr"></a>
## Build & Install RTTR

Download from the [download page](https://www.rttr.org/doc/master/building_install_page.html).

Open the *x64 Native Tools Command Prompt for VS", 

```
    cmake --build . --config Release
    dir lib\Release
    rttr-0.9.6\lib\Release 的目录
    01/19/2026  11:46 PM    <DIR>          .
    01/19/2026  11:46 PM    <DIR>          ..
    01/19/2026  11:46 PM           312,952 rttr_core.exp
    01/19/2026  11:46 PM           519,660 rttr_core.lib

    cmake --install .
```

Set this project's environment as

```
    RTTR_DIR = .../rttr/install/cmake
```

the path where the rttr-config.cmake exists.

## References

1. [rttorg/RTTR @ Github](https://github.com/rttrorg/rttr)

```
    struct MyStruct { MyStruct() {}; void func(double) {}; int data; };

    RTTR_REGISTRATION
    {
        registration::class_<MyStruct>("MyStruct")
            .constructor<>()
            .property("data", &MyStruct::data)
            .method("func", &MyStruct::func);
    }
```

1. [RTTR Building & Installation](https://www.rttr.org/doc/master/building_install_page.html) RTTR Home page

```
    # Failed
    find_package(RTTR CONFIG REQUIRED Core)

    # looks downloading...
    FetchContent_Declare(
        rttr
        GIT_REPOSITORY  https://github.com/rttrorg/rttr.git
        GIT_TAG v0.9.6
    )

    FetchContent_MakeAvailable(rttr)

```

1. [nlohmann/json](https://github.com/nlohmann/json)

1. [JSON for Modern C++ version 3.12.0, nlohmann/json Issue #190](https://github.com/rttrorg/rttr/issues/190)

1. [JSON for Moden C++, SAX Interface](https://json.nlohmann.me/features/parsing/sax_interface/), nlohmann/json documents

1. [getml/reflect-cpp](https://github.com/getml/reflect-cpp/tree/main)

1. [ShumWengSang Reflection-Json-Serializer](https://github.com/ShumWengSang/Reflection-Json-Serializer)
   
   Only the serialization.

1. [JSONify All Things, Extending the nlohmann/json Library](https://www.kdab.com/jsonify-with-nlohmann-json/#:~:text=7%20comments,extending%20the%20library%20a%20bit.),
Nicolas Arnaud-Cormos, 14 April 2022

1. [RTTR Issue #106](https://github.com/rttrorg/rttr/issues/106), one and only one RTTR_REGISTRATION block

1. RTTR fork at [berkaysahiin/rttr](https://github.com/berkaysahiin/rttr/tree/master),
   who declares that is compiltable with C++ 20, see [RTTR #389](https://github.com/rttrorg/rttr/pull/389).