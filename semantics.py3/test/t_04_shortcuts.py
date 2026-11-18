'''
ShortcutConfig: resolving the desktop's shortcut names, literal or {prj}<path>#<key> / #/<pointer>.

working folder: semantics.py3
'''
import json
import os
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, 'src')

from anson.io.odysz.anson import Anson
from semanticshare.io.oz.invoke import SynodeTask, ShortcutConfig


class ShortcutConfigTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        root = Path(self.tmp.name)
        (root / 'desk' / 'app' / 'res').mkdir(parents=True)
        (root / 'synode.py').mkdir()
        with open(root / 'desk' / 'app' / 'res' / 'shortcuts.json', 'w', encoding='utf-8') as f:
            json.dump({'type': 'io.oz.anclient.app.UIResources', 'lang': 'en',
                       'langs': {'en': {'desktop': 'Portfolio Desktop', 'setup': 'Portfolio Setup'},
                                 'zh-CN': {'desktop': '相册桌面'}}}, f, ensure_ascii=False)
        with open(root / 'synode.py' / 'synode.json', 'w', encoding='utf-8') as f:
            json.dump({'type': 'io.oz.anclient.app.UIResources', 'lang': 'en',
                       'langs': {'en': {'app_name': 'Synode Setup'}},
                       'apps': [{'a/b': 'Pointer 0'}]}, f)
        self.desk = root / 'desk'
        self.task = SynodeTask()
        self.task.github = str(root)
        self.task.gitprjs = {'synode.py': '{github}/synode.py'}

    def tearDown(self):
        self.tmp.cleanup()

    def test_defaults(self):
        t = Anson.from_json('{"type": "io.oz.invoke.SynodeTask", "version": "0.8.0"}')
        sc = t.shortcuts
        self.assertEqual('Portfolio', sc.name(self.task, 'root', 'x', self.desk))
        self.assertEqual('Portfolio Desktop', sc.name(self.task, 'desktop', 'album_gui', self.desk))
        self.assertEqual('Portfolio Setup', sc.name(self.task, 'setup', 'setup-gui', self.desk))

    def test_config(self):
        t = Anson.from_json('''{"type": "io.oz.invoke.SynodeTask", "version": "0.8.0",
            "shortcuts": {"type": "io.oz.invoke.ShortcutConfig", "root": "Album 相册",
              "res": {"type": "io.oz.anclient.app.UIResources", "lang": "zh-CN",
                "langs": {"en": {"desktop": "{}/app/res/shortcuts.json#desktop",
                                 "setup": "{synode.py}/synode.json#app_name"},
                          "zh-CN": {"desktop": "{}/app/res/shortcuts.json#desktop"}}}}}''')
        sc = t.shortcuts
        self.assertIsInstance(sc, ShortcutConfig)
        self.assertEqual('Album 相册', sc.name(self.task, 'root', 'x', self.desk))
        self.assertEqual('相册桌面', sc.name(self.task, 'desktop', 'album_gui', self.desk))
        self.assertEqual('Synode Setup', sc.name(self.task, 'setup', 'setup-gui', self.desk))

        self.assertEqual('Pointer 0', ShortcutConfig.resolve(self.task, '{synode.py}/synode.json#/apps/0/a~1b'))
        self.assertEqual('Kedai Gambar', ShortcutConfig.resolve(self.task, 'Kedai Gambar'))

    def test_fallback_invalid(self):
        sc = ShortcutConfig()
        sc.res.langs = {'en': {'desktop': '{}/missing.json#desktop', 'setup': '{nope}/x.json#y',
                               'bad': 'a:b'}}
        self.assertEqual('album_gui', sc.name(self.task, 'desktop', 'album_gui', self.desk))
        self.assertEqual('setup-gui', sc.name(self.task, 'setup', 'setup-gui', self.desk))
        with self.assertRaises(ValueError):
            sc.name(self.task, 'bad', 'x', self.desk)


if __name__ == '__main__':
    unittest.main()
