[![PyPI version](https://img.shields.io/pypi/v/semantics.py3.svg)](https://pypi.org/project/semantics.py3)

# About

Semantics.py3 is a sharing name space for the protocol layer of 
[Semantics-*](), of the Python3 version.

The installed module is

```
    site-packages/semanticshare
```

To import the modules, e.g. in Python source,

```
   from anson.io.odysz.anson import Anson
```

See [anclient.py3 tests](https://github.com/odys-z/Anclient/tree/master/py3/test)
for examples.

## Support Python 3.9.1

  Sematnic.py3 is planned to support Python 3.9.1 as LTS, which is the oldest version compatible with scp module,
  the enssential module of tasks.py.

  Installing a Python 3.9.1 interpreter on Ubuntu 26.04 LTS:

  ```
    sudo apt update
    
    sudo apt install -y build-essential zlib1g-dev libncurses5-dev \
    libgdbm-dev libnss3-dev libssl-dev libreadline-dev libffi-dev \
    libsqlite3-dev wget libbz2-dev
    
    wget https://www.python.org/ftp/python/3.9.1/Python-3.9.1.tgz
    # sha256sum Python-3.9.1.tgz can print sum or SHA
    
    tar -xf Python-3.9.1.tgz
    cd Python-3.9.1
    # --prefix=/opt/python3.9.1 is the important part: this installs it to its own isolated directory
    ./configure --enable-optimizations --prefix=/opt/python3.9.1 
    
    make -j"$(nproc)"    # compile with all the CPU cores
    sudo make altinstall # only creates a version-specific binary: /opt/python3.9.1/bin/python3.9
    
    /opt/python3.9.1/bin/python3.9 --version
  ```

  See semantic-jserv/jserv-alubm README for how to use invoke to deploy with tasks.py.