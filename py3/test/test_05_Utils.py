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


if __name__ == '__main__':
    unittest.main()
    t = UtilsTest()
    t.testFilePatterns()
    print('05: 0K')

