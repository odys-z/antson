import enum

from ansons.anson import Anson


class Port(enum.Enum):
    dataset = 0
    login = 1


class AnsonMsg(Anson):
    version: str


class AnsonBody(Anson):
    port: enum


class UserReq(AnsonBody):
    userId: str
