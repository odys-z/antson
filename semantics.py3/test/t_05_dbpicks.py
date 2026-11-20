'''
A db's picking list (io.oz.syn.dbpicks), the json shared with Java's io.oz.syn.dbpicks (doctree.jserv),
and its marks: each way a list and its marks can differ.

working folder: semantics.py3
'''
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, 'src')

from anson.io.odysz.anson import Anson
from semanticshare.io.oz.syn import dbpicks
from semanticshare.io.oz.syn.dbpicks import DbPicks, DbPick, DbInitRow, DbLogic

# as jserv-album/src/main/doctree-picks.json
doctree_picks = '''{
  "type": "io.oz.syn.dbpicks.DbPicks",
  "picks": [
    { "type": "io.oz.syn.dbpicks.DbPick",
      "mark": "DB-PICKING-0.8.2 t.a",
      "tabl": "t", "col": "a", "names": ["a", "a0"] },
    { "type": "io.oz.syn.dbpicks.DbPick",
      "mark": "DB-PICKING-0.8.2 t.b",
      "tabl": "t", "col": "b", "names": ["b"],
      "dflt": "v",
      "when": { "type": "io.oz.syn.dbpicks.DbLogic", "op": "isNotnull", "l": "builtAt" } }
  ],
  "inits": [
    { "type": "io.oz.syn.dbpicks.DbInitRow",
      "mark": "DB-PICKING-0.8.2 oz_autoseq:t.id",
      "tabl": "oz_autoseq", "key": ["sid"],
      "nvs": [["sid", "t.id"], ["seq", "0"]] }
  ]
}'''


def write(p: Path, obj) -> str:
    p.write_text(obj if isinstance(obj, str) else json.dumps(obj), encoding='utf-8')
    return str(p)


class DbPicksTest(unittest.TestCase):
    def setUp(self):
        Anson.java_src('semanticshare')
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def test_load(self):
        p = DbPicks.load(write(self.dir / 'doctree-picks.json', doctree_picks))
        self.assertIsInstance(p, DbPicks)
        self.assertEqual(2, len(p.picks))

        b = p.pick('T', 'B')
        self.assertIsInstance(b, DbPick)
        self.assertEqual('v', b.dflt)
        self.assertIsInstance(b.when, DbLogic)
        self.assertEqual(('isNotnull', 'builtAt', None), (b.when.op, b.when.l, b.when.r))
        self.assertIsNone(p.pick('t', 'c'))

        r = p.inits[0]
        self.assertIsInstance(r, DbInitRow)
        self.assertEqual(['sid'], r.key)
        self.assertEqual(['seq', '0'], r.nvs[1])

        for it in p.picks + p.inits:
            self.assertTrue(it.mark.startswith(dbpicks.mark_prefix), it.mark)

    def test_marks(self):
        src = self.dir / 'repo' / 'Meta.java'
        src.parent.mkdir()
        src.write_text('a // DB-PICKING-0.8.2 t.a\n'
                       'b // DB-PICKING-0.8.2 t.b\n'
                       'b // DB-PICKING-0.8.2 t.b\n'
                       'f // JSON-PICKING-0.8.2 x.json#f\n'
                       'g // JSON-PICKING-0.8.2 x.json#ff\n', encoding='utf-8')

        picks = DbPicks(picks=[
            DbPick('DB-PICKING-0.8.2 t.a', 't', 'a'),
            DbPick('DB-PICKING-0.8.2 t.b', 't', 'b'),
            DbPick(None, 't', 'c'),                               # no mark
            DbPick('DB-PICKING-0.8.2 t.d', 't', 'd')])            # not in the marks file
        config_picks = {'x.json': {'f': ['f'], 'g': ['g']}}
        mk = write(self.dir / 'marks.json', {'marks': {
            'DB-PICKING-0.8.2 t.a': 'repo/Meta.java',
            'DB-PICKING-0.8.2 t.b': 'repo/Meta.java',             # marked twice in the source
            'DB-PICKING-0.8.2 t.e': 'repo/Meta.java',             # no item, not in the source
            'JSON-PICKING-0.8.2 x.json#f': 'repo/Meta.java',      # f, not its prefix of ff
            'JSON-PICKING-0.8.2 x.json#h': 'other/Gone.java'}})   # no such field, no such file

        checked = picks.check_marks(str(self.dir), mk, config_picks, strict=True)
        errors = '\n'.join(checked.errors)
        self.assertIn('an item without a mark', errors)
        self.assertIn('no source of DB-PICKING-0.8.2 t.d', errors)
        self.assertIn('DB-PICKING-0.8.2 t.e is no item', errors)
        self.assertIn('no mark of config_picks x.json#g', errors)
        self.assertIn('JSON-PICKING-0.8.2 x.json#h is no field', errors)
        self.assertIn('DB-PICKING-0.8.2 t.b is marked 2 times', errors)
        self.assertIn('DB-PICKING-0.8.2 t.e is marked 0 times', errors)
        self.assertIn('other/Gone.java', errors)
        self.assertNotIn('JSON-PICKING-0.8.2 x.json#f is marked', errors)
        self.assertNotIn('DB-PICKING-0.8.2 t.a ', errors)

        # not strict: a missing source file is skipped
        checked = picks.check_marks(str(self.dir), mk, config_picks, strict=False)
        self.assertTrue(any('other/Gone.java' in s for s in checked.skipped))
        self.assertNotIn('other/Gone.java', '\n'.join(checked.errors))

    def test_main(self):
        src = self.dir / 'repo' / 'Meta.java'
        src.parent.mkdir()
        src.write_text('a // DB-PICKING-0.8.2 t.a\n'
                       'b // DB-PICKING-0.8.2 t.b\n'
                       'i // DB-PICKING-0.8.2 oz_autoseq:t.id\n'
                       'f // JSON-PICKING-0.8.2 x.json#f\n', encoding='utf-8')
        picks = write(self.dir / 'doctree-picks.json', doctree_picks)
        res = write(self.dir / 'resource.json', {'type': 'io.oz.syn.pkgresource.PkgResources',
                                                 'config_picks': {'x.json': {'f': ['f']}}})
        mk = {'DB-PICKING-0.8.2 t.a': 'repo/Meta.java',
              'DB-PICKING-0.8.2 t.b': 'repo/Meta.java',
              'DB-PICKING-0.8.2 oz_autoseq:t.id': 'repo/Meta.java',
              'JSON-PICKING-0.8.2 x.json#f': 'repo/Meta.java'}
        ok = write(self.dir / 'marks.json', {'marks': mk})
        args = ['--github', str(self.dir), '--picks', picks, '--resource', res, '--marks']
        self.assertEqual(0, dbpicks.main(args + [ok]))

        mk['JSON-PICKING-0.8.2 x.json#g'] = 'repo/Meta.java'
        bad = write(self.dir / 'bad-marks.json', {'marks': mk})
        self.assertEqual(1, dbpicks.main(args + [bad]))

        with self.assertRaises(SystemExit):   # the picking list and the marks are required
            dbpicks.main(['--github', str(self.dir)])


if __name__ == '__main__':
    unittest.main()
