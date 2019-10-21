# Generated from JSON.g4 by ANTLR 4.7.2
# encoding: utf-8
from antlr4 import *
from io import StringIO
from typing.io import TextIO
import sys


def serializedATN():
    with StringIO() as buf:
        buf.write("\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\23")
        buf.write("i\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b")
        buf.write("\t\b\4\t\t\t\4\n\t\n\3\2\3\2\3\2\7\2\30\n\2\f\2\16\2\33")
        buf.write("\13\2\3\3\3\3\3\3\3\3\7\3!\n\3\f\3\16\3$\13\3\3\3\3\3")
        buf.write("\3\4\3\4\3\4\3\4\7\4,\n\4\f\4\16\4/\13\4\3\4\3\4\3\4\3")
        buf.write("\4\5\4\65\n\4\3\5\3\5\3\5\3\5\3\6\3\6\5\6=\n\6\3\6\3\6")
        buf.write("\3\6\7\6B\n\6\f\6\16\6E\13\6\3\6\5\6H\n\6\3\7\3\7\3\7")
        buf.write("\3\7\3\b\3\b\3\t\3\t\3\t\3\t\7\tT\n\t\f\t\16\tW\13\t\3")
        buf.write("\t\3\t\3\t\3\t\5\t]\n\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3")
        buf.write("\n\5\ng\n\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2\4\3\2\t")
        buf.write("\n\3\2\20\21\2o\2\24\3\2\2\2\4\34\3\2\2\2\6\64\3\2\2\2")
        buf.write("\b\66\3\2\2\2\nG\3\2\2\2\fI\3\2\2\2\16M\3\2\2\2\20\\\3")
        buf.write("\2\2\2\22f\3\2\2\2\24\31\5\4\3\2\25\26\7\3\2\2\26\30\5")
        buf.write("\4\3\2\27\25\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32")
        buf.write("\3\2\2\2\32\3\3\2\2\2\33\31\3\2\2\2\34\35\7\4\2\2\35\"")
        buf.write("\5\b\5\2\36\37\7\3\2\2\37!\5\f\7\2 \36\3\2\2\2!$\3\2\2")
        buf.write("\2\" \3\2\2\2\"#\3\2\2\2#%\3\2\2\2$\"\3\2\2\2%&\7\5\2")
        buf.write("\2&\5\3\2\2\2\'(\7\4\2\2(-\5\f\7\2)*\7\3\2\2*,\5\f\7\2")
        buf.write("+)\3\2\2\2,/\3\2\2\2-+\3\2\2\2-.\3\2\2\2.\60\3\2\2\2/")
        buf.write("-\3\2\2\2\60\61\7\5\2\2\61\65\3\2\2\2\62\63\7\4\2\2\63")
        buf.write("\65\7\5\2\2\64\'\3\2\2\2\64\62\3\2\2\2\65\7\3\2\2\2\66")
        buf.write("\67\7\17\2\2\678\7\6\2\289\5\n\6\29\t\3\2\2\2:;\7\7\2")
        buf.write("\2;=\7\b\2\2<:\3\2\2\2<=\3\2\2\2=>\3\2\2\2>C\7\20\2\2")
        buf.write("?@\t\2\2\2@B\7\20\2\2A?\3\2\2\2BE\3\2\2\2CA\3\2\2\2CD")
        buf.write("\3\2\2\2DH\3\2\2\2EC\3\2\2\2FH\7\21\2\2G<\3\2\2\2GF\3")
        buf.write("\2\2\2H\13\3\2\2\2IJ\5\16\b\2JK\7\6\2\2KL\5\22\n\2L\r")
        buf.write("\3\2\2\2MN\t\3\2\2N\17\3\2\2\2OP\7\7\2\2PU\5\22\n\2QR")
        buf.write("\7\3\2\2RT\5\22\n\2SQ\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3")
        buf.write("\2\2\2VX\3\2\2\2WU\3\2\2\2XY\7\13\2\2Y]\3\2\2\2Z[\7\7")
        buf.write("\2\2[]\7\13\2\2\\O\3\2\2\2\\Z\3\2\2\2]\21\3\2\2\2^g\7")
        buf.write("\21\2\2_g\7\22\2\2`g\5\6\4\2ag\5\4\3\2bg\5\20\t\2cg\7")
        buf.write("\f\2\2dg\7\r\2\2eg\7\16\2\2f^\3\2\2\2f_\3\2\2\2f`\3\2")
        buf.write("\2\2fa\3\2\2\2fb\3\2\2\2fc\3\2\2\2fd\3\2\2\2fe\3\2\2\2")
        buf.write("g\23\3\2\2\2\f\31\"-\64<CGU\\f")
        return buf.getvalue()


class JSONParser ( Parser ):

    grammarFileName = "JSON.g4"

    atn = ATNDeserializer().deserialize(serializedATN())

    decisionsToDFA = [ DFA(ds, i) for i, ds in enumerate(atn.decisionToState) ]

    sharedContextCache = PredictionContextCache()

    literalNames = [ "<INVALID>", "','", "'{'", "'}'", "':'", "'['", "'L'", 
                     "'.'", "'$'", "']'", "'true'", "'false'", "'null'" ]

    symbolicNames = [ "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "TYPE", "IDENTIFIER", "STRING", "NUMBER", 
                      "WS" ]

    RULE_json = 0
    RULE_envelope = 1
    RULE_obj = 2
    RULE_type_pair = 3
    RULE_qualifiedName = 4
    RULE_pair = 5
    RULE_propname = 6
    RULE_array = 7
    RULE_value = 8

    ruleNames =  [ "json", "envelope", "obj", "type_pair", "qualifiedName", 
                   "pair", "propname", "array", "value" ]

    EOF = Token.EOF
    T__0=1
    T__1=2
    T__2=3
    T__3=4
    T__4=5
    T__5=6
    T__6=7
    T__7=8
    T__8=9
    T__9=10
    T__10=11
    T__11=12
    TYPE=13
    IDENTIFIER=14
    STRING=15
    NUMBER=16
    WS=17

    def __init__(self, input:TokenStream, output:TextIO = sys.stdout):
        super().__init__(input, output)
        self.checkVersion("4.7.2")
        self._interp = ParserATNSimulator(self, self.atn, self.decisionsToDFA, self.sharedContextCache)
        self._predicates = None




    class JsonContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def envelope(self, i:int=None):
            if i is None:
                return self.getTypedRuleContexts(JSONParser.EnvelopeContext)
            else:
                return self.getTypedRuleContext(JSONParser.EnvelopeContext,i)


        def getRuleIndex(self):
            return JSONParser.RULE_json

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterJson" ):
                listener.enterJson(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitJson" ):
                listener.exitJson(self)




    def json(self):

        localctx = JSONParser.JsonContext(self, self._ctx, self.state)
        self.enterRule(localctx, 0, self.RULE_json)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 18
            self.envelope()
            self.state = 23
            self._errHandler.sync(self)
            _la = self._input.LA(1)
            while _la==JSONParser.T__0:
                self.state = 19
                self.match(JSONParser.T__0)
                self.state = 20
                self.envelope()
                self.state = 25
                self._errHandler.sync(self)
                _la = self._input.LA(1)

        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class EnvelopeContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def type_pair(self):
            return self.getTypedRuleContext(JSONParser.Type_pairContext,0)


        def pair(self, i:int=None):
            if i is None:
                return self.getTypedRuleContexts(JSONParser.PairContext)
            else:
                return self.getTypedRuleContext(JSONParser.PairContext,i)


        def getRuleIndex(self):
            return JSONParser.RULE_envelope

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterEnvelope" ):
                listener.enterEnvelope(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitEnvelope" ):
                listener.exitEnvelope(self)




    def envelope(self):

        localctx = JSONParser.EnvelopeContext(self, self._ctx, self.state)
        self.enterRule(localctx, 2, self.RULE_envelope)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 26
            self.match(JSONParser.T__1)
            self.state = 27
            self.type_pair()
            self.state = 32
            self._errHandler.sync(self)
            _la = self._input.LA(1)
            while _la==JSONParser.T__0:
                self.state = 28
                self.match(JSONParser.T__0)
                self.state = 29
                self.pair()
                self.state = 34
                self._errHandler.sync(self)
                _la = self._input.LA(1)

            self.state = 35
            self.match(JSONParser.T__2)
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class ObjContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def pair(self, i:int=None):
            if i is None:
                return self.getTypedRuleContexts(JSONParser.PairContext)
            else:
                return self.getTypedRuleContext(JSONParser.PairContext,i)


        def getRuleIndex(self):
            return JSONParser.RULE_obj

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterObj" ):
                listener.enterObj(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitObj" ):
                listener.exitObj(self)




    def obj(self):

        localctx = JSONParser.ObjContext(self, self._ctx, self.state)
        self.enterRule(localctx, 4, self.RULE_obj)
        self._la = 0 # Token type
        try:
            self.state = 50
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,3,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 37
                self.match(JSONParser.T__1)
                self.state = 38
                self.pair()
                self.state = 43
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while _la==JSONParser.T__0:
                    self.state = 39
                    self.match(JSONParser.T__0)
                    self.state = 40
                    self.pair()
                    self.state = 45
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)

                self.state = 46
                self.match(JSONParser.T__2)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 48
                self.match(JSONParser.T__1)
                self.state = 49
                self.match(JSONParser.T__2)
                pass


        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class Type_pairContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def TYPE(self):
            return self.getToken(JSONParser.TYPE, 0)

        def qualifiedName(self):
            return self.getTypedRuleContext(JSONParser.QualifiedNameContext,0)


        def getRuleIndex(self):
            return JSONParser.RULE_type_pair

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterType_pair" ):
                listener.enterType_pair(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitType_pair" ):
                listener.exitType_pair(self)




    def type_pair(self):

        localctx = JSONParser.Type_pairContext(self, self._ctx, self.state)
        self.enterRule(localctx, 6, self.RULE_type_pair)
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 52
            self.match(JSONParser.TYPE)
            self.state = 53
            self.match(JSONParser.T__3)
            self.state = 54
            self.qualifiedName()
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class QualifiedNameContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def IDENTIFIER(self, i:int=None):
            if i is None:
                return self.getTokens(JSONParser.IDENTIFIER)
            else:
                return self.getToken(JSONParser.IDENTIFIER, i)

        def STRING(self):
            return self.getToken(JSONParser.STRING, 0)

        def getRuleIndex(self):
            return JSONParser.RULE_qualifiedName

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterQualifiedName" ):
                listener.enterQualifiedName(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitQualifiedName" ):
                listener.exitQualifiedName(self)




    def qualifiedName(self):

        localctx = JSONParser.QualifiedNameContext(self, self._ctx, self.state)
        self.enterRule(localctx, 8, self.RULE_qualifiedName)
        self._la = 0 # Token type
        try:
            self.state = 69
            self._errHandler.sync(self)
            token = self._input.LA(1)
            if token in [JSONParser.T__4, JSONParser.IDENTIFIER]:
                self.enterOuterAlt(localctx, 1)
                self.state = 58
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                if _la==JSONParser.T__4:
                    self.state = 56
                    self.match(JSONParser.T__4)
                    self.state = 57
                    self.match(JSONParser.T__5)


                self.state = 60
                self.match(JSONParser.IDENTIFIER)
                self.state = 65
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while _la==JSONParser.T__6 or _la==JSONParser.T__7:
                    self.state = 61
                    _la = self._input.LA(1)
                    if not(_la==JSONParser.T__6 or _la==JSONParser.T__7):
                        self._errHandler.recoverInline(self)
                    else:
                        self._errHandler.reportMatch(self)
                        self.consume()
                    self.state = 62
                    self.match(JSONParser.IDENTIFIER)
                    self.state = 67
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)

                pass
            elif token in [JSONParser.STRING]:
                self.enterOuterAlt(localctx, 2)
                self.state = 68
                self.match(JSONParser.STRING)
                pass
            else:
                raise NoViableAltException(self)

        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class PairContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def propname(self):
            return self.getTypedRuleContext(JSONParser.PropnameContext,0)


        def value(self):
            return self.getTypedRuleContext(JSONParser.ValueContext,0)


        def getRuleIndex(self):
            return JSONParser.RULE_pair

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterPair" ):
                listener.enterPair(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitPair" ):
                listener.exitPair(self)




    def pair(self):

        localctx = JSONParser.PairContext(self, self._ctx, self.state)
        self.enterRule(localctx, 10, self.RULE_pair)
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 71
            self.propname()
            self.state = 72
            self.match(JSONParser.T__3)
            self.state = 73
            self.value()
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class PropnameContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def STRING(self):
            return self.getToken(JSONParser.STRING, 0)

        def IDENTIFIER(self):
            return self.getToken(JSONParser.IDENTIFIER, 0)

        def getRuleIndex(self):
            return JSONParser.RULE_propname

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterPropname" ):
                listener.enterPropname(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitPropname" ):
                listener.exitPropname(self)




    def propname(self):

        localctx = JSONParser.PropnameContext(self, self._ctx, self.state)
        self.enterRule(localctx, 12, self.RULE_propname)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 75
            _la = self._input.LA(1)
            if not(_la==JSONParser.IDENTIFIER or _la==JSONParser.STRING):
                self._errHandler.recoverInline(self)
            else:
                self._errHandler.reportMatch(self)
                self.consume()
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class ArrayContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def value(self, i:int=None):
            if i is None:
                return self.getTypedRuleContexts(JSONParser.ValueContext)
            else:
                return self.getTypedRuleContext(JSONParser.ValueContext,i)


        def getRuleIndex(self):
            return JSONParser.RULE_array

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterArray" ):
                listener.enterArray(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitArray" ):
                listener.exitArray(self)




    def array(self):

        localctx = JSONParser.ArrayContext(self, self._ctx, self.state)
        self.enterRule(localctx, 14, self.RULE_array)
        self._la = 0 # Token type
        try:
            self.state = 90
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,8,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 77
                self.match(JSONParser.T__4)
                self.state = 78
                self.value()
                self.state = 83
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while _la==JSONParser.T__0:
                    self.state = 79
                    self.match(JSONParser.T__0)
                    self.state = 80
                    self.value()
                    self.state = 85
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)

                self.state = 86
                self.match(JSONParser.T__8)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 88
                self.match(JSONParser.T__4)
                self.state = 89
                self.match(JSONParser.T__8)
                pass


        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class ValueContext(ParserRuleContext):

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def STRING(self):
            return self.getToken(JSONParser.STRING, 0)

        def NUMBER(self):
            return self.getToken(JSONParser.NUMBER, 0)

        def obj(self):
            return self.getTypedRuleContext(JSONParser.ObjContext,0)


        def envelope(self):
            return self.getTypedRuleContext(JSONParser.EnvelopeContext,0)


        def array(self):
            return self.getTypedRuleContext(JSONParser.ArrayContext,0)


        def getRuleIndex(self):
            return JSONParser.RULE_value

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterValue" ):
                listener.enterValue(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitValue" ):
                listener.exitValue(self)




    def value(self):

        localctx = JSONParser.ValueContext(self, self._ctx, self.state)
        self.enterRule(localctx, 16, self.RULE_value)
        try:
            self.state = 100
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,9,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 92
                self.match(JSONParser.STRING)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 93
                self.match(JSONParser.NUMBER)
                pass

            elif la_ == 3:
                self.enterOuterAlt(localctx, 3)
                self.state = 94
                self.obj()
                pass

            elif la_ == 4:
                self.enterOuterAlt(localctx, 4)
                self.state = 95
                self.envelope()
                pass

            elif la_ == 5:
                self.enterOuterAlt(localctx, 5)
                self.state = 96
                self.array()
                pass

            elif la_ == 6:
                self.enterOuterAlt(localctx, 6)
                self.state = 97
                self.match(JSONParser.T__9)
                pass

            elif la_ == 7:
                self.enterOuterAlt(localctx, 7)
                self.state = 98
                self.match(JSONParser.T__10)
                pass

            elif la_ == 8:
                self.enterOuterAlt(localctx, 8)
                self.state = 99
                self.match(JSONParser.T__11)
                pass


        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx





