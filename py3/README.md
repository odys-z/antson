# Anson.py3

A testing package ...

```code
from ansons.anson import Anson
```

# Install from testpypi

```
pip install --index-url https://test.pypi.org/simple --extra-index-url https://pypi.org/simple Anson.py3
```

# References

- https://packaging.python.org/en/latest/tutorials/packaging-projects/


# Troubleshootings

```
ERROR   InvalidDistribution: Invalid distribution metadata: unrecognized or malformed field
        'license-file'; unrecognized or malformed field 'license-expression'  
```

Install twine 6.1.0 and packaging 24.

```
pip install packaging -U
```

See [issue #1216](https://github.com/pypa/twine/issues/1216#issuecomment-2609745412).
