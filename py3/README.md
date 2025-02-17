# Anson.py3

A testing package ...

```code
from ansons.anson import Anson
```

# Install from testpypi

```
pip install --index-url https://test.pypi.org/simple --extra-index-url https://pypi.org/simple Anson.py3
```

# Guide

- Java vs Python package structure

Python packages tree:

```
├── io
│   └── oz
│       ├── jserv
│       │   └── docs
│       │       └── syn
│       │           └── singleton.py
│       └── syn.py

.
└── io
    └── oz
        └── jserv
            └── docs
                └── syn
                    ├── singleton
                    │   └── AppSettings.java
'''

Java packages tree:

'''
.
└── io
    └── oz
        └── syn
            ├── AnRegistry.java
            ├── SynodeConfig.java
            ├── SynOrg.java
            └── YellowPages.java
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
