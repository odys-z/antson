'''
The package manifest (bin/resource.json): the upgrade tool's version, and the upgrades of an
installation's configuration files (ConfigUpgrades).

working folder: semantics.py3
'''
import json
import os
import sys
import tempfile
import unittest

sys.path.insert(0, 'src')

from anson.io.odysz.anson import Anson
from semanticshare.io.oz.syn.pkgresource import PkgResources
from semanticshare.io.oz.srv import WebConfig

# a manifest as jserv-album/src/main/resource.json
manifest = '''{
  "type": "io.oz.syn.pkgresource.PkgResources",
  "files": {"synode": "bin/jserv-album-0.8.2.jar"},
  "versions": {"jar_ver": "0.8.2"},
  "upgrade_from": {"min": "0.8.0"},
  "upgrader": {"min": "0.8.2"},
  "upgrades": [
    { "since": "0.8.0",
      "files": {
        "WEB-INF/html-service.json": {
          "replace": {"io.oz.srv.ResPath": "io.oz.srv.StaticResource"},
          "add": {
            "dynaResource": {"type": "io.oz.srv.DynaResource", "handler": "io.oz.srv.ResPathsTier",
                             "synode": "http://127.0.0.1:{settings.port}/jserv-album",
                             "pollInterval_snd": 3600, "resource": "respaths", "danyroot": "pub", "allowDir": false},
            "paths": [{"type": "io.oz.srv.StaticResource", "path": "/pub/_t/*", "resource": "web-dist/sdoc-template", "allowDir": false}]}},
        "WEB-INF/settings.json": {"add": {"sdocTemplates": "web-dist/sdoc-template"}}}},
    { "since": "0.9.0",
      "files": {"WEB-INF/settings.json": {"add": {"a09": true}}}}
  ]
}'''

# html-service.json of an installed 0.8.1
html_081 = '''{
  "type": "io.oz.srv.WebConfig",
  "port": 8900,
  "paths": [{
    "type": "io.oz.srv.ResPath",
    "path": "/",
    "resource": "web-dist",
    "allowDir": false
  }],
  "startHandler": null
}'''


def write(path: str, text: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)


def read(path: str) -> dict:
    with open(path, encoding='utf-8') as f:
        return json.load(f)


class PkgResourcesTest(unittest.TestCase):
    def setUp(self):
        Anson.java_src('semanticshare')
        self.root = tempfile.mkdtemp(prefix='pkgres-')
        write(os.path.join(self.root, 'bin', 'resource.json'), manifest)
        write(os.path.join(self.root, 'WEB-INF', 'html-service.json'), html_081)
        write(os.path.join(self.root, 'WEB-INF', 'settings.json'), '{"type": "x.AppSettings", "port": 8964, "sdocTemplates": "/opt/templates"}')
        self.res = PkgResources.load(os.path.join(self.root, 'bin', 'resource.json'))

    def testLoad(self):
        self.assertEqual('0.8.2', self.res.jar_ver())
        self.assertEqual(2, len(self.res.upgrades))
        self.assertEqual('0.8.0', self.res.upgrades[0].since)
        # the values to add stay json, e.g. html-service's types
        self.assertEqual('io.oz.srv.DynaResource',
                self.res.upgrades[0].files['WEB-INF/html-service.json']['add']['dynaResource']['type'])

    def testUpgrader(self):
        self.assertIsNone(self.res.refuses_upgrader('0.8.2'))
        self.assertIsNone(self.res.refuses_upgrader('0.9.0'))
        self.assertIn('older than 0.8.2', self.res.refuses_upgrader('0.8.1'))
        self.assertIsNone(PkgResources().refuses_upgrader('0.7.8'))

    def testUpgradeConfigs(self):
        env = {'settings.port': 8964}

        # an installed 0.8.1: the entry since 0.8.0, not the one since 0.9.0
        changed = self.res.upgrade_configs('0.8.1', self.root, env)
        self.assertEqual(['WEB-INF/html-service.json'], changed)

        html = read(os.path.join(self.root, 'WEB-INF', 'html-service.json'))
        self.assertEqual(8900, html['port'])
        self.assertEqual('io.oz.srv.StaticResource', html['paths'][0]['type'])
        self.assertEqual(['/', '/pub/_t/*'], [p['path'] for p in html['paths']])
        self.assertEqual('http://127.0.0.1:8964/jserv-album', html['dynaResource']['synode'])

        # what the user set is kept, and nothing of 0.9.0
        settings = read(os.path.join(self.root, 'WEB-INF', 'settings.json'))
        self.assertEqual('/opt/templates', settings['sdocTemplates'])
        self.assertNotIn('a09', settings)

        # html-service.json is html-service's, and the installer's
        cfg = Anson.from_file(os.path.join(self.root, 'WEB-INF', 'html-service.json'))
        self.assertIsInstance(cfg, WebConfig)
        self.assertEqual('pub', cfg.dynaResource.danyroot)

        # again: nothing to change
        self.assertEqual([], self.res.upgrade_configs('0.8.1', self.root, env))

        # an installed 0.7.8 is older than every entry's since
        write(os.path.join(self.root, 'WEB-INF', 'html-service.json'), html_081)
        self.assertEqual([], self.res.upgrade_configs('0.7.8', self.root, env))

    def testUnknownValue(self):
        html = os.path.join(self.root, 'WEB-INF', 'html-service.json')
        with self.assertRaises(ValueError):
            self.res.upgrade_configs('0.8.1', self.root, {})
        # nothing is written
        with open(html, encoding='utf-8') as f:
            self.assertEqual(html_081, f.read())


if __name__ == '__main__':
    unittest.main()
