import os
import unittest
from datetime import datetime

from src.anson.io.odysz.common import Utils


class UtilsTest(unittest.TestCase):
    def testFilePatterns(self):
        lines = [
            '@set jar_ver=0.7.1 # some comments',
            '@REM set version=0.7.0',
            '@set html_ver=0.1.5',
            'test_05_Utils.py',
            'UtilsTest().replaceFilePatterns()',
            str(datetime.now())]

        jar_ver = '0.7.2'
        html_ver= '0.1.6'
        version = '0.7.1'

        cwd = os.getcwd()
        version_file = os.path.join(cwd, 'test' if os.path.basename(cwd) != 'test' else '', 'res', '__version__.bat')
        Utils.writeline_nl(version_file, lines)

        Utils.update_patterns(version_file,
    {'@set jar_ver=[0-9\\.]+': f'@set jar_ver={jar_ver}',
             '@REM set version=[0-9\\.]+': f'@set version={version}',
             '@set html_ver=[0-9\\.]+': f'@set html_ver={html_ver}'})

        with open(version_file, 'r') as f:
            xlines = f.readlines()

            self.assertEqual(f'@set jar_ver={jar_ver} # some comments\n', xlines[0])
            self.assertEqual(f'@set version={version}\n', xlines[1])
            self.assertEqual(f'@set html_ver={html_ver}\n', xlines[2])

            for x in range(3, len(lines)):
                l, f = lines[x], xlines[x]
                self.assertEqual(l + '\n', f)

    def testArcPatterns(self):
        '''
        gzip2() excludes by the path in the package, for patterns with a '/'.
        '''
        import tarfile, tempfile, zipfile
        from src.anson.io.odysz.utils import gzip2, matches_arc_patterns

        self.assertTrue(matches_arc_patterns('web-dist/fonts/a.ttf', ['web-dist/fonts/a.ttf']))
        self.assertTrue(matches_arc_patterns('web-dist\\fonts\\a.ttf', ['web-dist/fonts/a.ttf']))
        self.assertFalse(matches_arc_patterns('template/fonts/a.ttf', ['web-dist/fonts/a.ttf']))
        self.assertTrue(matches_arc_patterns('template/fonts/a.log', ['*.log']))
        self.assertTrue(matches_arc_patterns('web-dist/a/b.map', ['web-dist/*.map']))
        self.assertFalse(matches_arc_patterns('web-dist/a.ttf', ['fonts/a.ttf']))

        with tempfile.TemporaryDirectory() as tmp:
            for f in ['web/a.js', 'web/a.js.gz', 'web/fonts/a.ttf', 'web/fonts/a.ttf.gz', 'web/x.log',
                      'tmpl/a.js', 'tmpl/fonts/a.ttf', 'tmpl/x.log']:
                os.makedirs(os.path.dirname(os.path.join(tmp, f)), exist_ok=True)
                with open(os.path.join(tmp, f), 'w') as fo: fo.write(f)

            pkg = os.path.join(tmp, 'p.zip' if os.name == 'nt' else 'p.tar.gz')
            gzip2(pkg, {'web-dist': f'{tmp}/web/*', 'template': f'{tmp}/tmpl/*'},
                  ['*.log', 'web-dist/a.js', 'web-dist/fonts/a.ttf'])

            if os.name == 'nt':
                with zipfile.ZipFile(pkg) as z: names = z.namelist()
            else:
                with tarfile.open(pkg) as z: names = z.getnames()

            self.assertEqual(['template/a.js', 'template/fonts/a.ttf',
                              'web-dist/a.js.gz', 'web-dist/fonts/a.ttf.gz'],
                             sorted(n.replace('\\', '/') for n in names))

    def testListInList(self):
        '''
        A list in a list is written as json, e.g. html-service.json's startHandlers.
        '''
        import json
        from src.anson.io.odysz.anson import Anson
        self.assertEqual('[["a"], ["b", "c"]]', Anson.toList_([['a'], ['b', 'c']], 0, True))
        self.assertEqual([['a'], ['b', 'c'], []], json.loads(Anson.toList_([['a'], ['b', 'c'], []], 0, False)))


if __name__ == '__main__':
    unittest.main()
    t = UtilsTest()
    t.testFilePatterns()
    print('05: 0K')

