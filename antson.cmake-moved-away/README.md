# Setup Qt Creator 6.10 Project

```bash
    $ git clone https://github.com/Microsoft/vcpkg.git
    $ cd vcpkg
    $ ./bootstrap-vcpkg.sh (.bat)
    $ ./vcpkg integrate install
    $ vcpkg install entt
```

In Qt Creator Proejct, Build,

```
    -DCMAKE_TOOLCHAIN_FILE=[...]/vcpkg/scripts/buildsystems/vcpkg.cmake
```
