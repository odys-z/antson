"""
AESHelper2, the py3 peer of

- Java:  semantic-transact, io.odysz.common.AESHelper2 (the reference)
- TS:    @anclient/semantier, aes2.ts
- C++20: anson.cmake, io/odysz/common.h, class AESHelper2

Session token de/encryption only: AES-256-CBC, key = SHA-256(utf-8 key), PKCS#7 padding,
cipher and iv in base 64. To be overhauled when handshakes are built on CA / self-signed certs.

Requires the package 'cryptography' (pip install anson.py3[crypto]).
"""
from __future__ import annotations

import base64
import hashlib
import os
from typing import List

from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes

_block_bits = 128
_iv_size = 16


class AESHelper2:

    @staticmethod
    def getRandom() -> bytes:
        """
        :return: 16 random bytes
        """
        return os.urandom(_iv_size)

    @staticmethod
    def encode64(data: bytes) -> str:
        return base64.b64encode(data).decode('ascii')

    @staticmethod
    def decode64(b64: str) -> bytes:
        return base64.b64decode(b64)

    @staticmethod
    def _key(key: str) -> bytes:
        return hashlib.sha256(key.encode('utf-8')).digest()

    @staticmethod
    def encrypt(plain: str, key: str, iv: bytes) -> str:
        """
        Encrypt plain text to cipher of base 64.
        Java: public static String encrypt(String plain, String key, byte[] iv)
        :raises ValueError: plain text begins or ends with space, the same as Java
        """
        if plain.strip() != plain:
            raise ValueError("Plain text to be encrypted can not begin or end with space.")

        padder = padding.PKCS7(_block_bits).padder()
        padded = padder.update(plain.encode('utf-8')) + padder.finalize()

        encryptor = Cipher(algorithms.AES(AESHelper2._key(key)), modes.CBC(iv)).encryptor()
        return AESHelper2.encode64(encryptor.update(padded) + encryptor.finalize())

    @staticmethod
    def decrypt(cipher64: str, key: str, iv: bytes) -> str:
        """
        Java: public static String decrypt(String cypher, String key, byte[] iv)
        :raises ValueError: wrong key / iv (bad padding)
        """
        decryptor = Cipher(algorithms.AES(AESHelper2._key(key)), modes.CBC(iv)).decryptor()
        padded = decryptor.update(AESHelper2.decode64(cipher64)) + decryptor.finalize()

        unpadder = padding.PKCS7(_block_bits).unpadder()
        return (unpadder.update(padded) + unpadder.finalize()).decode('utf-8')

    @staticmethod
    def repackSessionToken(ssToken: str, key: str, uid: str, iv: bytes = None) -> str:
        """
        <pre>
        ssToken = cipher : iv
        plain = decrypt(cipher, key, iv)
        token = encrypt(uid : plain, key, iv2)
        return token : iv2
        </pre>
        :param iv: iv2, random if None (only for tests, like TS repackSessionToken_())
        """
        ss = ssToken.split(':')
        plain = AESHelper2.decrypt(ss[0], key, AESHelper2.decode64(ss[1]))

        if iv is None:
            iv = AESHelper2.getRandom()
        cipher = AESHelper2.encrypt(f'{uid}:{plain}', key, iv)
        return f'{cipher}:{AESHelper2.encode64(iv)}'

    @staticmethod
    def verifyToken(requestoken: str, myKnowledge: str, uid: str, key: str) -> bool:
        """
        Is encrypt(uid:knowledge, key, iv) == token? where requestoken = token : iv
        """
        sstoken = requestoken.split(':')
        enciphered = AESHelper2.encrypt(f'{uid}:{myKnowledge}', key, AESHelper2.decode64(sstoken[1]))
        return enciphered == sstoken[0]

    @staticmethod
    def packSessionKey(key: str) -> List[str]:
        """
        <pre>
        iv, knows = random(16)
        token = encrypt(knows, key, iv)
        return [token : iv, knows.64]
        </pre>
        :return: 0: token : iv, 1: knowledge in base 64
        """
        iv = AESHelper2.getRandom()
        knows = AESHelper2.encode64(AESHelper2.getRandom())
        return [f'{AESHelper2.encrypt(knows, key, iv)}:{AESHelper2.encode64(iv)}', knows]
