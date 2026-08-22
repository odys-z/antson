
import unittest
from datetime import datetime

from src.anson.io.odysz.anson import AnsonException
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
  "type": "io.odysz.semantic.syn.SyncUser",
  "userId": "1",
  "userName": "ody",
  "pswd": "8964"
}''', LangExt.str(usr))

        usr = {'a': 1, 'b': usr}
        self.assertEqual('''{"a": 1,
"b": "{
  "type": "io.odysz.semantic.syn.SyncUser",
  "userId": "1",
  "userName": "ody",
  "pswd": "8964"
}"}''', LangExt.str(usr))

        self.assertEqual('''[2, {"a": 1,
"b": "{
  "type": "io.odysz.semantic.syn.SyncUser",
  "userId": "1",
  "userName": "ody",
  "pswd": "8964"
}"}]''', LangExt.str([2, usr]))

    def test_isblank(self):
        self.assertTrue(LangExt.isblank(None))
        self.assertTrue(LangExt.isblank(''))
        self.assertTrue(LangExt.isblank(' '))
        self.assertTrue(LangExt.isblank('00', r'0+'))
        self.assertTrue(LangExt.isblank('00', r'0'))
        self.assertFalse(LangExt.isblank('00', r'^0$'))
        self.assertFalse(LangExt.isblank(' ', r'0'))
        self.assertTrue(LangExt.isblank('0.0.0.0', r'^\s*(0)|(0\\.(0\\.)*\\.0)\s*$'))
        self.assertTrue(LangExt.isblank(' 0.0.0.0  ', r'^\s*(0)|(0\\.(0\\.)+\\.0)\s*$'))
        self.assertTrue(LangExt.isblank('0.0.0.0.0', r'^\s*(0)|(0\\.(0\\.)+\\.0)\s*$'))

    def test_passwd_valid(self):
        pswds = ['io.github.odys-z', '12345678', '!#%^*(){}:;']
        for p in pswds:
            self.assertTrue(LangExt.only_passwdlen(p, 8, 32))

        np = ['1234567', '1234567\\', '1234567890ABCDEF1234567890ABCDEF-bi5']
        for p in np:
            try:
                LangExt.only_passwdlen(p, 8, 32)
                self.fail(p)
            except AnsonException as e:
                pass


class TestTruncRightASCII(unittest.TestCase):
    def test_shorter_than_limit(self):
        assert LangExt.trunc_right("hello", 12) == "hello"

    def test_exact_limit(self):
        s = "hello world!"  # exactly 12 chars/bytes
        assert LangExt.trunc_right(s, 12) == "hello world!"

    def test_longer_than_limit(self):
        s = "hello world, how are you?"
        result = LangExt.trunc_right(s, 12)
        assert result == s[-12:]
        assert len(result.encode('utf-8')) == 12

    def test_empty_string(self):
        assert LangExt.trunc_right("", 12) == ""

    def test_n_zero(self):
        assert LangExt.trunc_right("hello", 0) == ""

    def test_n_negative(self):
        assert LangExt.trunc_right("hello", -5) == ""


class TestTruncRightUnicode(unittest.TestCase):
    def test_multibyte_chars_no_split(self):
        # each 世/界 char is 3 bytes in utf-8; choose n that lands cleanly
        s = "世界世界"  # 12 bytes total
        result = LangExt.trunc_right(s, 12)
        assert result == "世界世界"
        assert len(result.encode('utf-8')) == 12

    def test_multibyte_chars_forces_trim(self):
        ss = [(" 世界", "héllo wörld 世界"), ("їні!", "Слава Україні!")]
        for exp, t_str in ss:
            result = LangExt.trunc_right(t_str, 7)
            encoded = result.encode('utf-8')
            assert len(encoded) <= 7
            assert t_str.endswith(result)  # result is a genuine suffix of s
            self.assertEqual(exp, result)  # result is a genuine suffix of s

    def test_result_is_always_valid_utf8(self):
        s = "abc世界xyz"
        for n in range(0, 15):
            result = LangExt.trunc_right(s, n)
            # should not raise
            result.encode('utf-8').decode('utf-8')

    def test_single_multibyte_char_too_small_n(self):
        # if n is smaller than a single multi-byte char, may return ''
        s = "世"  # 3 bytes
        assert LangExt.trunc_right(s, 1) == ""
        assert LangExt.trunc_right(s, 2) == ""
        assert LangExt.trunc_right(s, 3) == "世"


class TestTruncRightEdgeCases(unittest.TestCase):
    def test_n_larger_than_string(self):
        s = "hi"
        assert LangExt.trunc_right(s, 1000) == "hi"

    def test_emoji(self):
        s = "hello 👋🌍"  # emojis are 4 bytes each in utf-8
        result = LangExt.trunc_right(s, 4)
        assert result == "🌍" or result == ""  # depends on exact byte alignment
        assert len(result.encode('utf-8')) <= 4

    def test_result_never_exceeds_n_bytes(self):
        s = "The quick brown fox jumps over the lazy dog 🦊🐕"
        for n in range(0, 20):
            result = LangExt.trunc_right(s, n)
            assert len(result.encode('utf-8')) <= n


if __name__ == '__main__':
    unittest.main()
    t = LangExtTest()
    t.testStr()
    t.test_isblank()
    t.test_passwd_valid()

    t = TestTruncRightASCII()
    t.test_shorter_than_limit()
    t.test_exact_limit()
    t.test_longer_than_limit()
    t.test_empty_string()
    t.test_n_zero()
    t.test_n_negative()

    t = TestTruncRightUnicode()
    t.test_single_multibyte_char_too_small_n()
    t.test_multibyte_chars_forces_trim()
    t.test_result_is_always_valid_utf8()
    t.test_multibyte_chars_no_split()
    t.test_multibyte_chars_forces_trim()

    t = TestTruncRightEdgeCases()
    t.test_emoji()
    t.test_n_larger_than_string()
    t.test_result_never_exceeds_n_bytes()
