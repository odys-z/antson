import unittest
from typing import cast

from src.anson.io.odysz import anson
from test.io.oz.jserv.docs.syn.singleton import AppSettings
from test.io.oz.syn import SynodeConfig, AnRegistry, Synode, SynOrg


class YellowPagesTests(unittest.TestCase):
    def setUp(self):
        self.maxDiff = None

    def testAnregistry(self):
        anson.Anson.java_src('test')

        settings: AppSettings = cast(AppSettings, anson.Anson.from_file('test/json/registry/settings.json'))

        self.assertEqual(type(settings), AppSettings)
        self.assertEqual('http://192.168.0.0:8964/jserv-album', settings.jservs['X'])
        self.assertEqual('''{
  "type": "io.oz.jserv.docs.syn.singleton.AppSettings",
  "envars": {"WEBROOT_Album-web": "example.com"},
  "vol_name": "VOLUME_HOME",
  "volume": "~/github/semantic-jserv/jserv-album/vol",
  "port": 8964,
  "jservs": {
    "X": "http://192.168.0.0:8964/jserv-album",
    "Y": "http://192.168.0.0:8965/jserv-album"},
  "installkey": null,
  "rootkey": "0123456789ABCDEF"
}''', settings.toBlock(beautify=True))

        diction: AnRegistry = cast(AnRegistry, anson.Anson.from_file('test/json/registry/dictionary.json'))
        self.assertEqual(AnRegistry, type(diction))
        self.assertEqual(SynodeConfig, type(diction.config))
        self.assertEqual(SynOrg, type(diction.config.org))
        self.assertEqual(list, type(diction.config.peers))
        self.assertEqual(2, len(diction.config.peers))
        self.assertEqual(Synode, type(diction.config.peers[0]))
        self.assertEqual(0, diction.config.peers[0].nyq)
        # print(diction.toBlock())
        self.assertEqual('''{
  "type": "io.oz.syn.AnRegistry",
  "config": {
    "type": "io.oz.syn.SynodeConfig",
    "https": false,
    "synid": "SAMPLE-X",
    "domain": "zsu",
    "admin": "ody",
    "sysconn": "sys",
    "synconn": "docsyn",
    "org": {
      "type": "io.oz.syn.SynOrg",
      "meta": "io.oz.jserv.docs.meta.DocOrgMeta",
      "orgId": "ura",
      "orgName": "URA",
      "orgType": "nation",
      "market": "test",
      "webroot": "$WEBROOT_Album-web-in-settings.json",
      "homepage": "",
      "album0": "album-test"
    },
    "syncIns": 20,
    "peers": [{
      "type": "io.odysz.semantic.syn.Synode",
      "synid": "X",
      "org": "ura",
      "mac": "#URA.X",
      "nyq": 0,
      "domain": "zsu",
      "syn_uid": "X,X"
    }, {
      "type": "io.odysz.semantic.syn.Synode",
      "synid": "Y",
      "org": "ura",
      "mac": "#URA.Y",
      "nyq": 0,
      "domain": "zsu",
      "syn_uid": "Y,Y"
    }]
  },
  "synusers": [{
    "type": "io.odysz.semantic.syn.SyncUser",
    "userId": "ody",
    "userName": null,
    "pswd": "8964",
    "iv": null,
    "org": "ura",
    "domain": "zsu"
  }, {
    "type": "io.odysz.semantic.syn.SyncUser",
    "userId": "syrskyi",
    "userName": null,
    "pswd": "слава україні",
    "iv": null,
    "org": "ura",
    "domain": "zsu"
  }]
}''', diction.toBlock(beautify=True))


if __name__ == '__main__':
    unittest.main()
    t = YellowPagesTests()
    t.testAnregistry()

