
import unittest
from datetime import datetime

from src.anson.io.odysz.common import LangExt
from test.io.oz.syn import SyncUser


class LangExtTest(unittest.TestCase):
    def testStr(self):
        obj = {'a': 1, 'b': "2"}

        dt = datetime.now()
        lst = [1, 3, 'a', 5.5, dt, str(dt)]

        self.assertEqual('{"a": 1,\n"b": "2"}', LangExt.str(obj))
        self.assertEqual(f'[1, 3, "a", 5.5, {str(dt)}, "{str(dt)}"]', LangExt.str(lst))
        self.assertEqual('1', LangExt.str(1))

        usr = SyncUser(userId='1', userName='ody', pswd='8964')
        self.assertEqual('''{
  "type": "io.oz.syn.SyncUser",
  "userId": "1",
  "userName": "ody",
  "pswd": "8964"
}''', LangExt.str(usr))

        usr = {'a': 1, 'b': usr}
        self.assertEqual('''{"a": 1,
"b": "{
  "type": "io.oz.syn.SyncUser",
  "userId": "1",
  "userName": "ody",
  "pswd": "8964"
}"}''', LangExt.str(usr))

        self.assertEqual('''[2, {"a": 1,
"b": "{
  "type": "io.oz.syn.SyncUser",
  "userId": "1",
  "userName": "ody",
  "pswd": "8964"
}"}]''', LangExt.str([2, usr]))


if __name__ == '__main__':
    unittest.main()
    t = LangExtTest()
    t.testStr()

