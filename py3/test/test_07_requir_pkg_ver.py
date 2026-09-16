import unittest

from src.anson.io.odysz.common import requir_pkg, requir_npm_package_lock


class RequirePkgVerTests(unittest.TestCase):
    def testRequires(self):
        requir_pkg('semantics.py3', '0.6.2')

        requir_npm_package_lock('test/res', '@anclient/anreact', '0.7.0')
        requir_npm_package_lock('test/res/package-lock.json', '@anclient/anreact', '0.7.0')
        requir_npm_package_lock('test/res', 'qrcode', ['1.5.0', '1.6.0'])

        requir_npm_package_lock('test/res', '@anclient/semantier', ['1.0.5', '2.0.0'])

        with self.assertRaises(SystemExit) as cm:
            requir_npm_package_lock('xxx', '@anclient/anreact')
        self.assertEqual(cm.exception.code, 1)

        with self.assertRaises(SystemExit) as cm:
            requir_npm_package_lock('@anclient/anreact', '0.7.0')
        self.assertEqual(cm.exception.code, 1)

        requir_npm_package_lock('test/res', '@anclient/anreact', '0.7.0')

        with self.assertRaises(SystemExit) as cm:
            requir_npm_package_lock('@anclient/semantier', '1.0.5')
        self.assertEqual(cm.exception.code, 1)

        requir_npm_package_lock('test/res', '@anclient/semantier', '1.0.5')

if __name__ == '__main__':
    unittest.main()
    t = RequirePkgVerTests()
    t.testRequires()

