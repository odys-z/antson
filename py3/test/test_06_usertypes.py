import unittest
from typing import cast

from T06B import T06B
from semanticshare.io.oz.jserv.docs.syn.singleton import AppSettings

from anson.io.odysz.anson import Anson
from src.anson.io.odysz.common import requir_pkg
from test.T06A import T06A


class UserTypesTests(unittest.TestCase):
    '''
    Requires Semantics.py3 0.6.4
    Install T06B:
        cd test/T06B
        python -m build
        pip install dist/T06B-...whl
    '''

    def testUserTypes(self):
        requir_pkg('semantics.py3', '0.6.4')
        requir_pkg('T06B')

        Anson.add_package('T06A')
        Anson.add_package('T06B')

        settings: AppSettings = cast(AppSettings, Anson.from_file('test/json/registry/settings.json'))

        self.assertEqual(type(settings), AppSettings)
        self.assertEqual('http://192.168.0.0:8964/jserv-album', settings.jservs['X'])


        t06a: T06A = cast(T06A, Anson.from_file('test/json/t06b.json'))
        t06b: T06B = cast(T06B, Anson.from_file('test/json/t06b.json'))


if __name__ == '__main__':
    unittest.main()
    t = UserTypesTests()
    t.testUserTypes()

