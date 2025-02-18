import unittest

from src.anson.io.odysz.ansons import Anson
from test.io.oz.jserv.docs.syn.singleton import AppSettings
from test.io.oz.syn import SynodeConfig, AnRegistry


class YellowPagesTests(unittest.TestCase):
    def testAnregistry(self):
        Anson.java_src('test')

        settings = Anson.from_file('json/registry/settings.json')

        self.assertEqual(type(settings), AppSettings)
        self.assertEqual('http://192.168.0.0:8964/jserv-album', settings.jservs['X'])

        diction = Anson.from_file('json/registry/dictionary.json')
        self.assertEqual(type(diction), AnRegistry)
        self.assertEqual(type(diction.config), SynodeConfig)
        # print(diction)
        print(diction.toBlock())


if __name__ == '__main__':
    unittest.main()
    t = YellowPagesTests()
    t.testAnregistry()

