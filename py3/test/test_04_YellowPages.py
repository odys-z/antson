import unittest
from typing import cast

from src.anson.io.odysz.ansons import Anson
from test.io.oz.jserv.docs.syn.singleton import AppSettings
from test.io.oz.syn import SynodeConfig, AnRegistry, Synode, SynOrg


class YellowPagesTests(unittest.TestCase):
    def testAnregistry(self):
        Anson.java_src('test')

        settings: AppSettings = cast(AppSettings, Anson.from_file('test/json/registry/settings.json'))

        self.assertEqual(type(settings), AppSettings)
        self.assertEqual('http://192.168.0.0:8964/jserv-album', settings.jservs['X'])

        diction: AnRegistry = cast(AnRegistry, Anson.from_file('test/json/registry/dictionary.json'))
        self.assertEqual(AnRegistry, type(diction))
        self.assertEqual(SynodeConfig, type(diction.config))
        self.assertEqual(SynOrg, type(diction.config.org))
        self.assertEqual(list, type(diction.config.peers))
        self.assertEqual(2, len(diction.config.peers))
        self.assertEqual(Synode, type(diction.config.peers[0]))
        self.assertEqual(0, diction.config.peers[0].nyq)
        print(diction.toBlock())


if __name__ == '__main__':
    unittest.main()
    t = YellowPagesTests()
    t.testAnregistry()

