import unittest

from src.anson.io.odysz.anson import class4Name, instanceof


class Instance4Test(unittest.TestCase):

    def testForwardRef(self):
        module_name = 'test.io.odysz.semantic.jprotocol'
        class_name = 'AnsonBody'
        cls = class4Name(module_name, class_name)
        self.assertEqual(f'{module_name}.{class_name}', cls.__type__)

        uri = 'test/instance-forward'
        obj = instanceof(cls, {"uri": uri})
        self.assertEqual(f'{module_name}.{class_name}', obj.__type__)
        self.assertEqual(uri, obj.uri)

        # Already warned about it:
        self.assertFalse(hasattr(obj, 'a'))
        # self.assertIsNone(obj.a)


if __name__ == '__main__':
    unittest.main()
    t = Instance4Test()
    t.testForwardRef()

