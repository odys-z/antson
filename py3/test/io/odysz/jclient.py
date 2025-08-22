from collections.abc import Callable
from sys import stderr
from typing import Optional, Protocol, Any

import requests

from test.io.odysz import SessionInf
from test.io.odysz.semantic.jprotocol import MsgCode, Port, AnsonMsg, AnsonBody, AnsonResp, AnsonHeader
from test.io.odysz.semantic.jserv.echo import EchoReq, A


class OnError(Protocol):
    err : Callable = None
    def __call__(self, code: MsgCode, msg: str, *args: str) -> None:
        return self.err(code, msg, args)

    def __init__(self, on_err: Callable[[MsgCode, str, ...], None]):
        self.err = on_err


class Clients:
    """
    Java stub
    """

    servRt = None

    @staticmethod
    def pingLess(funcUri: str, errCtx: OnError=None):
        req = EchoReq()
        req.a = A.echo

        client = InsecureClient(Clients.servRt)
        jmsg = client.userReq(funcUri, Port.echo, req)

        resp = client.commit(jmsg, errCtx)

        return resp


class SessionClient:
    myservRt: str
    ssInf: SessionInf
    header: AnsonHeader

    def __init__(self, jserv: str, ssInf: SessionInf):
        self.proxies = {
            "http": None,
            "https": None,
        }
        '''
        https://stackoverflow.com/a/40470853/7362888
        '''

        self.myservRt = jserv
        self.ssInf = ssInf
        self.header = None

    def Header(self):
        if self.header is None:
            self.header = AnsonHeader(ssid=self.ssInf.ssid, uid=self.ssInf.uid, token=self.ssInf.ssToken)
        return self.header

    def commit(self, req: AnsonMsg, err: OnError) -> Optional[AnsonResp]:
        try:
            print(f'{self.myservRt}/{req.port.value}')
            print(req.toBlock(False))
            resp = requests.post(f'{self.myservRt}/{req.port.value}',
                                 proxies=self.proxies,
                                 data=req.toBlock(False))
            if resp.status_code == 200:
                data = resp.json()  # If the response is JSON
                return AnsonResp.from_envelope(data)
            else:
                print(f"Error: {resp.status_code}", file=stderr)
                res = f'{resp.status_code}\n{self.myservRt}\n{"" if req is None else req.toBlock()}'
                err.err(MsgCode.exIo, res, resp.text)
                return None
        except Exception as e:
            if err is not None:
                err(MsgCode.exIo, e.message if hasattr(e, 'message') else str(e), None)
            else:
                raise e

    def userReq(self, uri: str, port: Port, bodyItem: AnsonBody, *act: Any) -> AnsonMsg:
        bodyItem.uri = uri
        if act is not None and len(act) > 0:
            self.Header().act = act

        return AnsonMsg(port).Header(self.Header()).Body(bodyItem)


class InsecureClient(SessionClient):

    def __init__(self, servRt: str):
        super().__init__(servRt, SessionInf('uid', 'session less'))

