import dataclasses
import os
import unittest

from src.anson.io.odysz.common import Utils

"""
    PyCharm Debug Configuration:
    run as module: test.test
    working folder: py3
"""

def run_script(script_path):
    python = 'py' if Utils.iswindows() else 'python3'
    os.system(f'{python} {script_path}')

test_loader = unittest.TestLoader()
# test_suite = test_loader.discover(start_dir='.', pattern='test_*.py')
# test_suite = test_loader.discover(start_dir='.', pattern='test_00_Anson.py')
# test_suite = test_loader.discover(start_dir='.', pattern='test_01_Instance4Name.py')
# test_suite = test_loader.discover(start_dir='.', pattern='test_02_Ping.py')
# test_suite = test_loader.discover(start_dir='.', pattern='test_05_*.py')
test_suite = test_loader.discover(start_dir='.', pattern='test_*.py')

# Run the tests
def tryModulename():
    module_name = 'test.io.odysz.semantic.jprotocol'
    class_name = 'AnsonBody'

    # Dynamically import the module
    module = __import__(module_name, fromlist=[class_name])

    # Get the class
    cls = getattr(module, class_name)

    # Create an instance
    obj = cls()  # Adjust arguments as needed
    cls.__type__=f'{module_name}.{class_name}'
    # obj.uri = 'uri'

    _FIELDS = '__dataclass_fields__'  # see dataclasses.fields()
    fds = getattr(cls, _FIELDS)
    missingAttrs = []
    for k in fds:
        if not hasattr(obj, k) and fds[k].default is dataclasses.MISSING: #_MISSING_TYPE:
            missingAttrs.append(k)
            setattr(obj, k, None)

    if len(missingAttrs) > 0:
        Utils.warn(f'Missing attributes in {module_name}.{class_name}: {missingAttrs}. Anson expect a __init__() for all initialize the none default fields.')

    print(obj)

if __name__ == '__main__':

    # tryModulename()
    unittest.TextTestRunner(verbosity=2).run(test_suite)