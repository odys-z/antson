from semanticshare.io.odysz.semantic.jprotocol import MsgCode


class SsException(Exception):
    """
    Semantic session exception, Java io.odysz.semantic.jserv.crud.SsException
    (since Java 1.5.17, replacing io.odysz.semantic.jserv.x.SsException).

    @since semantics.py3 0.6.18
    """
    code: MsgCode = MsgCode.exSession

    def __init__(self, template: str, *args: object):
        """
        :param template: Java format string, e.g. "login failed: %s"
        """
        super().__init__(SsException.try_format(template, *args))

    @staticmethod
    def try_format(template: str, *args: object) -> str:
        try:
            return template % args if len(args) > 0 else template
        except Exception:
            return template
