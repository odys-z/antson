'''
Upgrading an installed synode 0.8.0 with the package 0.8.2, as synodepy3's upgrade does it
(commands.update_srv()): the package brings bin/resource.json, the manifest, and its html-service.json,
the template; the installed html-service.json is made of the template and the installed values its
config_picks tell (ConfigPicks).

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

# the package 0.8.2's manifest, as jserv-album/src/main/resource.json
manifest_082 = '''{
  "type": "io.oz.syn.pkgresource.PkgResources",
  "files": {"synode": "bin/jserv-album-0.8.2.jar", "html-web": "bin/html-web-0.2.1.jar"},
  "versions": {"jar_ver": "0.8.2", "html_srver": "0.2.1"},
  "upgrade_from": {"min": "0.8.0"},
  "upgrader": {"min": "0.8.2"},
  "config_picks": {
    "WEB-INF/html-service.json": {
      "welcomepages":                  ["welcomepages"],
      "dynaResource.syn_jserv":        ["dynaResource.syn_jserv", "dynaResource.synode"],
      "dynaResource.pollInterval_snd": ["dynaResource.pollInterval_snd"]}}
}'''

# the package 0.8.2's html-service.json, as jserv-album/src/main/webapp/WEB-INF-0.8
template_082 = '''{
  "type": "io.oz.srv.WebConfig",
  "port": 8900,
  "paths": [{"type": "io.oz.srv.StaticResource", "path": "/*", "resource": "web-dist", "allowDir": false}],
  "startHandler": ["io.oz.srv.JservLocalHandler", "private/host.json"],
  "welcomepages": ["index.html"],
  "dynaResource": {"type": "io.oz.srv.DynaResource", "handler": "io.oz.srv.ResPathsTier",
                   "syn_jserv": null, "pollInterval_snd": 3600, "resource": "respaths",
                   "danyroot": "pub", "allowDir": false}
}'''

# the installed 0.8.0's, written by its installer, and edited by the user
html_080 = '''{
  "type": "io.oz.srv.WebConfig",
  "port": 8900,
  "paths": [{"type": "io.oz.srv.ResPath", "path": "/", "resource": "web-dist", "allowDir": false}],
  "startHandler": null,
  "welcomepages": ["home.html"],
  "json_path": "a field no version knows now"
}'''


def write(path: str, text: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)


class UpgradeTest(unittest.TestCase):

    def testUpgrade080to082(self):
        Anson.java_src('semanticshare')

        # the installed 0.8.0, no manifest yet; the upgrade keeps its html-service.json in a backup
        root = tempfile.mkdtemp(prefix='synode-0.8.0-')
        backup = os.path.join(root, 'backup', 'WEB-INF', 'html-service.json')
        write(backup, html_080)
        self.assertFalse(os.path.exists(os.path.join(root, 'bin', 'resource.json')))

        # 1. the package 0.8.2 is unpacked: bin/resource.json, and the template
        resource_json = os.path.join(root, 'bin', 'resource.json')
        html_json = os.path.join(root, 'WEB-INF', 'html-service.json')
        write(resource_json, manifest_082)
        write(html_json, template_082)

        pkg = PkgResources.load(resource_json)
        self.assertIsNone(pkg.refuses_upgrader('0.8.2'))
        self.assertIn('older than 0.8.2', pkg.refuses_upgrader('0.8.1'))
        self.assertIsNone(pkg.refuses('0.8.0'))

        # 2. html-service.json: the template, with the user's values picked
        kept, skipped = pkg.pick_config('WEB-INF/html-service.json', html_json, backup)
        self.assertEqual(['welcomepages'], kept)
        self.assertEqual([], skipped)

        with open(html_json, encoding='utf-8') as f:
            html = json.load(f)
        # the user's value
        self.assertEqual(['home.html'], html['welcomepages'])
        # the new version's: the paths, with the new type, the start handler
        self.assertEqual('io.oz.srv.StaticResource', html['paths'][0]['type'])
        self.assertEqual('/*', html['paths'][0]['path'])
        self.assertEqual('io.oz.srv.JservLocalHandler', html['startHandler'][0])
        # a missing field, with its default values
        self.assertEqual('io.oz.srv.ResPathsTier', html['dynaResource']['handler'])
        self.assertEqual(3600, html['dynaResource']['pollInterval_snd'])
        self.assertEqual('pub', html['dynaResource']['danyroot'])
        # a field no version knows now is gone
        self.assertNotIn('json_path', html)

        # 3. it's html-web 0.2.1's configuration, for the installer to fill the computed values
        cfg = Anson.from_file(html_json)
        self.assertIsInstance(cfg, WebConfig)
        self.assertIsNone(cfg.dynaResource.syn_jserv)

        # 4. a renamed field: a developer's 0.8.2 with dynaResource.synode, and a value of another kind
        mine = json.loads(template_082)
        mine['dynaResource'].pop('syn_jserv')
        mine['dynaResource']['synode'] = 'http://127.0.0.1:8964/jserv-album'
        mine['dynaResource']['pollInterval_snd'] = '1h'
        write(backup, json.dumps(mine))
        write(html_json, template_082)

        kept, skipped = pkg.pick_config('WEB-INF/html-service.json', html_json, backup)
        self.assertEqual(['welcomepages', 'dynaResource.syn_jserv (was dynaResource.synode)'], kept)
        self.assertEqual(['dynaResource.pollInterval_snd (yours is text, now number)'], skipped)
        with open(html_json, encoding='utf-8') as f:
            html = json.load(f)
        self.assertEqual('http://127.0.0.1:8964/jserv-album', html['dynaResource']['syn_jserv'])
        self.assertEqual(3600, html['dynaResource']['pollInterval_snd'])


if __name__ == '__main__':
    unittest.main()
