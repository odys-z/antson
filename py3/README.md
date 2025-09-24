
[![PyPI version](https://img.shields.io/pypi/v/anson.py3.svg)](https://pypi.org/project/anson.py3)

# Anson.py3

Json (de)serialize module in Python 3.

```code
from anson.io.odysz.anson import Anson
```

# Install from testpypi

[//]: # (pip install --index-url https://test.pypi.org/simple --extra-index-url https://pypi.org/simple anson.py3)
```
pip install anson.py3
```

# Guide

- Mapping Java vs Python package structure

Python packages tree is in format of *path/to/module/class*, while java has no node of *module*:

```
├── io
│   └── oz
│       ├── jserv
│       │   └── docs
│       │       └── syn
│       │           └── singleton.py "class AppSettings"
│       └── syn.py "class AnRegistry, SynodeConfig, SynOrg, YellowPages"
```

Java packages tree:

```
.
└── io
    └── oz
        └── jserv
            └── docs
                └── syn
                    ├── singleton
                    │   └── AppSettings.java
```

```
.
└── io
    └── oz
        └── syn
            ├── AnRegistry.java
            ├── SynodeConfig.java
            ├── SynOrg.java
            └── YellowPages.java
```

Anson.py3 is using package name path with a top level path. Say, if the json envelope
define as

```
{ type: io.oz.syn.AnRegistry,
  ...
}
```

and the user's project include two parts:

* The semantics.py3 name space for json protocol package, the user types.

Which is currently need to be manually keep all fields the
same with the java end. Start the project from the source:

    github.com/odys-z/antson/semantics.py3

You need use the following commands to build and install it,
so it can be imported to the application project.

```code
    python -m build
    pip install dist/semantics_py3-#.#.#-py3-none-any.whl
```

* The (client end) source project for applications.

In main.py, call

```code
    from semanticshare.your.package import Your_class
```

# Issues

- Printing Anson subclasses with non-default field without value initialization will result in errors

If SynOrg.parent is defined as

```
class SynOrg(Anson)
    parent: str

    def __init__(self):
        super().__init__()

```

```
org = Anson.from_file(...)
print (org)

Error
Traceback (most recent call last):
  File "/home/antson/py3/test/testYellowPages.py", line 18, in testAnregistry
    print(diction)
  File "/usr/lib/python3.12/dataclasses.py", line 262, in wrapper
    result = user_function(self)
             ^^^^^^^^^^^^^^^^^^^
  File "<string>", line 3, in __repr__
  File "/usr/lib/python3.12/dataclasses.py", line 262, in wrapper
    result = user_function(self)
             ^^^^^^^^^^^^^^^^^^^
  File "<string>", line 3, in __repr__
  File "/usr/lib/python3.12/dataclasses.py", line 262, in wrapper
    result = user_function(self)
             ^^^^^^^^^^^^^^^^^^^
  File "<string>", line 3, in __repr__
AttributeError: 'SynOrg' object has no attribute 'parent'
```

# References

- https://packaging.python.org/en/latest/tutorials/packaging-projects/

# Troubleshootings

.pypirc

```
[testpypi]
repository: https://test.pypi.org/legacy/
username = __token__
password = pypi-zzz
```

```
python3 -m twine --version
twine version 6.1.0 (keyring: 25.6.0, packaging: 24.2, requests: 2.31.0, requests-toolbelt: 1.0.0,
urllib3: 2.0.7, id: 1.5.0)
python3 -m build
python3 -m twine upload --repository testpypi dist/*
```

```
ERROR   InvalidDistribution: Invalid distribution metadata: unrecognized or malformed field
        'license-file'; unrecognized or malformed field 'license-expression'  
```

Install twine 6.1.0 and packaging 24.

```
pip install packaging -U
```

See [issue #1216](https://github.com/pypa/twine/issues/1216#issuecomment-2609745412).
