# Generated from JSON.parser by ANTLR 4.13.1
# encoding: utf-8
from antlr4 import *
from io import StringIO
import sys
if sys.version_info[1] > 5:
	from typing import TextIO
else:
	from typing.io import TextIO

def serializedATN():
    return [
        4,1,20,119,2,0,7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,7,4,2,5,7,5,2,6,7,
        6,2,7,7,7,2,8,7,8,2,9,7,9,1,0,1,0,1,0,5,0,24,8,0,10,0,12,0,27,9,
        0,1,1,1,1,1,1,5,1,32,8,1,10,1,12,1,35,9,1,1,1,1,1,1,2,1,2,1,2,1,
        2,5,2,43,8,2,10,2,12,2,46,9,2,1,2,1,2,1,2,1,2,3,2,52,8,2,1,3,1,3,
        1,3,1,3,1,4,1,4,3,4,60,8,4,1,4,1,4,1,4,5,4,65,8,4,10,4,12,4,68,9,
        4,1,4,3,4,71,8,4,1,5,1,5,1,5,1,5,1,6,1,6,1,7,1,7,1,7,1,7,5,7,83,
        8,7,10,7,12,7,86,9,7,1,7,1,7,1,7,1,7,3,7,92,8,7,1,8,1,8,1,8,1,8,
        1,8,1,8,1,8,1,8,1,8,4,8,103,8,8,11,8,12,8,104,3,8,107,8,8,1,9,1,
        9,1,9,1,9,1,9,1,9,1,9,1,9,3,9,117,8,9,1,9,0,0,10,0,2,4,6,8,10,12,
        14,16,18,0,3,1,0,7,8,1,0,17,18,1,0,13,16,129,0,20,1,0,0,0,2,28,1,
        0,0,0,4,51,1,0,0,0,6,53,1,0,0,0,8,70,1,0,0,0,10,72,1,0,0,0,12,76,
        1,0,0,0,14,91,1,0,0,0,16,106,1,0,0,0,18,116,1,0,0,0,20,25,3,2,1,
        0,21,22,5,1,0,0,22,24,3,2,1,0,23,21,1,0,0,0,24,27,1,0,0,0,25,23,
        1,0,0,0,25,26,1,0,0,0,26,1,1,0,0,0,27,25,1,0,0,0,28,33,3,6,3,0,29,
        30,5,1,0,0,30,32,3,10,5,0,31,29,1,0,0,0,32,35,1,0,0,0,33,31,1,0,
        0,0,33,34,1,0,0,0,34,36,1,0,0,0,35,33,1,0,0,0,36,37,5,2,0,0,37,3,
        1,0,0,0,38,39,5,3,0,0,39,44,3,10,5,0,40,41,5,1,0,0,41,43,3,10,5,
        0,42,40,1,0,0,0,43,46,1,0,0,0,44,42,1,0,0,0,44,45,1,0,0,0,45,47,
        1,0,0,0,46,44,1,0,0,0,47,48,5,2,0,0,48,52,1,0,0,0,49,50,5,3,0,0,
        50,52,5,2,0,0,51,38,1,0,0,0,51,49,1,0,0,0,52,5,1,0,0,0,53,54,3,18,
        9,0,54,55,5,4,0,0,55,56,3,8,4,0,56,7,1,0,0,0,57,58,5,5,0,0,58,60,
        5,6,0,0,59,57,1,0,0,0,59,60,1,0,0,0,60,61,1,0,0,0,61,66,5,17,0,0,
        62,63,7,0,0,0,63,65,5,17,0,0,64,62,1,0,0,0,65,68,1,0,0,0,66,64,1,
        0,0,0,66,67,1,0,0,0,67,71,1,0,0,0,68,66,1,0,0,0,69,71,5,18,0,0,70,
        59,1,0,0,0,70,69,1,0,0,0,71,9,1,0,0,0,72,73,3,12,6,0,73,74,5,4,0,
        0,74,75,3,16,8,0,75,11,1,0,0,0,76,77,7,1,0,0,77,13,1,0,0,0,78,79,
        5,5,0,0,79,84,3,16,8,0,80,81,5,1,0,0,81,83,3,16,8,0,82,80,1,0,0,
        0,83,86,1,0,0,0,84,82,1,0,0,0,84,85,1,0,0,0,85,87,1,0,0,0,86,84,
        1,0,0,0,87,88,5,9,0,0,88,92,1,0,0,0,89,90,5,5,0,0,90,92,5,9,0,0,
        91,78,1,0,0,0,91,89,1,0,0,0,92,15,1,0,0,0,93,107,5,18,0,0,94,107,
        5,19,0,0,95,107,3,4,2,0,96,107,3,2,1,0,97,107,3,14,7,0,98,107,5,
        10,0,0,99,107,5,11,0,0,100,107,5,12,0,0,101,103,7,2,0,0,102,101,
        1,0,0,0,103,104,1,0,0,0,104,102,1,0,0,0,104,105,1,0,0,0,105,107,
        1,0,0,0,106,93,1,0,0,0,106,94,1,0,0,0,106,95,1,0,0,0,106,96,1,0,
        0,0,106,97,1,0,0,0,106,98,1,0,0,0,106,99,1,0,0,0,106,100,1,0,0,0,
        106,102,1,0,0,0,107,17,1,0,0,0,108,109,5,3,0,0,109,117,5,14,0,0,
        110,111,5,3,0,0,111,117,5,16,0,0,112,113,5,3,0,0,113,117,5,13,0,
        0,114,115,5,3,0,0,115,117,5,15,0,0,116,108,1,0,0,0,116,110,1,0,0,
        0,116,112,1,0,0,0,116,114,1,0,0,0,117,19,1,0,0,0,12,25,33,44,51,
        59,66,70,84,91,104,106,116
    ]

class JSONParser ( Parser ):

    grammarFileName = "JSON.parser"

    atn = ATNDeserializer().deserialize(serializedATN())

    decisionsToDFA = [ DFA(ds, i) for i, ds in enumerate(atn.decisionToState) ]

    sharedContextCache = PredictionContextCache()

    literalNames = [ "<INVALID>", "','", "'}'", "'{'", "':'", "'['", "'L'", 
                     "'.'", "'$'", "']'", "'true'", "'false'", "'null'", 
                     "'type'", "'TYPE'", "'\"type\"'", "'\"TYPE\"'" ]

    symbolicNames = [ "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "<INVALID>", "<INVALID>", "<INVALID>", 
                      "<INVALID>", "IDENTIFIER", "STRING", "NUMBER", "WS" ]

    RULE_json = 0
    RULE_envelope = 1
    RULE_obj = 2
    RULE_type_pair = 3
    RULE_qualifiedName = 4
    RULE_pair = 5
    RULE_propname = 6
    RULE_array = 7
    RULE_value = 8
    RULE_type = 9

    ruleNames =  [ "json", "envelope", "obj", "type_pair", "qualifiedName", 
                   "pair", "propname", "array", "value", "type" ]

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
    T__12=13
    T__13=14
    T__14=15
    T__15=16
    IDENTIFIER=17
    STRING=18
    NUMBER=19
    WS=20

    def __init__(self, input:TokenStream, output:TextIO = sys.stdout):
        super().__init__(input, output)
        self.checkVersion("4.13.1")
        self._interp = ParserATNSimulator(self, self.atn, self.decisionsToDFA, self.sharedContextCache)
        self._predicates = None




    class JsonContext(ParserRuleContext):
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitJson" ):
                return visitor.visitJson(self)
            else:
                return visitor.visitChildren(self)




    def json(self):

        localctx = JSONParser.JsonContext(self, self._ctx, self.state)
        self.enterRule(localctx, 0, self.RULE_json)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 20
            self.envelope()
            self.state = 25
            self._errHandler.sync(self)
            _la = self._input.LA(1)
            while _la==1:
                self.state = 21
                self.match(JSONParser.T__0)
                self.state = 22
                self.envelope()
                self.state = 27
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
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitEnvelope" ):
                return visitor.visitEnvelope(self)
            else:
                return visitor.visitChildren(self)




    def envelope(self):

        localctx = JSONParser.EnvelopeContext(self, self._ctx, self.state)
        self.enterRule(localctx, 2, self.RULE_envelope)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 28
            self.type_pair()
            self.state = 33
            self._errHandler.sync(self)
            _la = self._input.LA(1)
            while _la==1:
                self.state = 29
                self.match(JSONParser.T__0)
                self.state = 30
                self.pair()
                self.state = 35
                self._errHandler.sync(self)
                _la = self._input.LA(1)

            self.state = 36
            self.match(JSONParser.T__1)
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class ObjContext(ParserRuleContext):
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitObj" ):
                return visitor.visitObj(self)
            else:
                return visitor.visitChildren(self)




    def obj(self):

        localctx = JSONParser.ObjContext(self, self._ctx, self.state)
        self.enterRule(localctx, 4, self.RULE_obj)
        self._la = 0 # Token type
        try:
            self.state = 51
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,3,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 38
                self.match(JSONParser.T__2)
                self.state = 39
                self.pair()
                self.state = 44
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while _la==1:
                    self.state = 40
                    self.match(JSONParser.T__0)
                    self.state = 41
                    self.pair()
                    self.state = 46
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)

                self.state = 47
                self.match(JSONParser.T__1)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 49
                self.match(JSONParser.T__2)
                self.state = 50
                self.match(JSONParser.T__1)
                pass


        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class Type_pairContext(ParserRuleContext):
        __slots__ = 'parser'

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser

        def type_(self):
            return self.getTypedRuleContext(JSONParser.TypeContext,0)


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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitType_pair" ):
                return visitor.visitType_pair(self)
            else:
                return visitor.visitChildren(self)




    def type_pair(self):

        localctx = JSONParser.Type_pairContext(self, self._ctx, self.state)
        self.enterRule(localctx, 6, self.RULE_type_pair)
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 53
            self.type_()
            self.state = 54
            self.match(JSONParser.T__3)
            self.state = 55
            self.qualifiedName()
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class QualifiedNameContext(ParserRuleContext):
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitQualifiedName" ):
                return visitor.visitQualifiedName(self)
            else:
                return visitor.visitChildren(self)




    def qualifiedName(self):

        localctx = JSONParser.QualifiedNameContext(self, self._ctx, self.state)
        self.enterRule(localctx, 8, self.RULE_qualifiedName)
        self._la = 0 # Token type
        try:
            self.state = 70
            self._errHandler.sync(self)
            token = self._input.LA(1)
            if token in [5, 17]:
                self.enterOuterAlt(localctx, 1)
                self.state = 59
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                if _la==5:
                    self.state = 57
                    self.match(JSONParser.T__4)
                    self.state = 58
                    self.match(JSONParser.T__5)


                self.state = 61
                self.match(JSONParser.IDENTIFIER)
                self.state = 66
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while _la==7 or _la==8:
                    self.state = 62
                    _la = self._input.LA(1)
                    if not(_la==7 or _la==8):
                        self._errHandler.recoverInline(self)
                    else:
                        self._errHandler.reportMatch(self)
                        self.consume()
                    self.state = 63
                    self.match(JSONParser.IDENTIFIER)
                    self.state = 68
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)

                pass
            elif token in [18]:
                self.enterOuterAlt(localctx, 2)
                self.state = 69
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
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitPair" ):
                return visitor.visitPair(self)
            else:
                return visitor.visitChildren(self)




    def pair(self):

        localctx = JSONParser.PairContext(self, self._ctx, self.state)
        self.enterRule(localctx, 10, self.RULE_pair)
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 72
            self.propname()
            self.state = 73
            self.match(JSONParser.T__3)
            self.state = 74
            self.value()
        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class PropnameContext(ParserRuleContext):
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitPropname" ):
                return visitor.visitPropname(self)
            else:
                return visitor.visitChildren(self)




    def propname(self):

        localctx = JSONParser.PropnameContext(self, self._ctx, self.state)
        self.enterRule(localctx, 12, self.RULE_propname)
        self._la = 0 # Token type
        try:
            self.enterOuterAlt(localctx, 1)
            self.state = 76
            _la = self._input.LA(1)
            if not(_la==17 or _la==18):
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
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitArray" ):
                return visitor.visitArray(self)
            else:
                return visitor.visitChildren(self)




    def array(self):

        localctx = JSONParser.ArrayContext(self, self._ctx, self.state)
        self.enterRule(localctx, 14, self.RULE_array)
        self._la = 0 # Token type
        try:
            self.state = 91
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,8,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 78
                self.match(JSONParser.T__4)
                self.state = 79
                self.value()
                self.state = 84
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while _la==1:
                    self.state = 80
                    self.match(JSONParser.T__0)
                    self.state = 81
                    self.value()
                    self.state = 86
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)

                self.state = 87
                self.match(JSONParser.T__8)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 89
                self.match(JSONParser.T__4)
                self.state = 90
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
        __slots__ = 'parser'

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

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitValue" ):
                return visitor.visitValue(self)
            else:
                return visitor.visitChildren(self)




    def value(self):

        localctx = JSONParser.ValueContext(self, self._ctx, self.state)
        self.enterRule(localctx, 16, self.RULE_value)
        self._la = 0 # Token type
        try:
            self.state = 106
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,10,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 93
                self.match(JSONParser.STRING)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 94
                self.match(JSONParser.NUMBER)
                pass

            elif la_ == 3:
                self.enterOuterAlt(localctx, 3)
                self.state = 95
                self.obj()
                pass

            elif la_ == 4:
                self.enterOuterAlt(localctx, 4)
                self.state = 96
                self.envelope()
                pass

            elif la_ == 5:
                self.enterOuterAlt(localctx, 5)
                self.state = 97
                self.array()
                pass

            elif la_ == 6:
                self.enterOuterAlt(localctx, 6)
                self.state = 98
                self.match(JSONParser.T__9)
                pass

            elif la_ == 7:
                self.enterOuterAlt(localctx, 7)
                self.state = 99
                self.match(JSONParser.T__10)
                pass

            elif la_ == 8:
                self.enterOuterAlt(localctx, 8)
                self.state = 100
                self.match(JSONParser.T__11)
                pass

            elif la_ == 9:
                self.enterOuterAlt(localctx, 9)
                self.state = 102 
                self._errHandler.sync(self)
                _la = self._input.LA(1)
                while True:
                    self.state = 101
                    _la = self._input.LA(1)
                    if not((((_la) & ~0x3f) == 0 and ((1 << _la) & 122880) != 0)):
                        self._errHandler.recoverInline(self)
                    else:
                        self._errHandler.reportMatch(self)
                        self.consume()
                    self.state = 104 
                    self._errHandler.sync(self)
                    _la = self._input.LA(1)
                    if not ((((_la) & ~0x3f) == 0 and ((1 << _la) & 122880) != 0)):
                        break

                pass


        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx


    class TypeContext(ParserRuleContext):
        __slots__ = 'parser'

        def __init__(self, parser, parent:ParserRuleContext=None, invokingState:int=-1):
            super().__init__(parent, invokingState)
            self.parser = parser


        def getRuleIndex(self):
            return JSONParser.RULE_type

        def enterRule(self, listener:ParseTreeListener):
            if hasattr( listener, "enterType" ):
                listener.enterType(self)

        def exitRule(self, listener:ParseTreeListener):
            if hasattr( listener, "exitType" ):
                listener.exitType(self)

        def accept(self, visitor:ParseTreeVisitor):
            if hasattr( visitor, "visitType" ):
                return visitor.visitType(self)
            else:
                return visitor.visitChildren(self)




    def type_(self):

        localctx = JSONParser.TypeContext(self, self._ctx, self.state)
        self.enterRule(localctx, 18, self.RULE_type)
        try:
            self.state = 116
            self._errHandler.sync(self)
            la_ = self._interp.adaptivePredict(self._input,11,self._ctx)
            if la_ == 1:
                self.enterOuterAlt(localctx, 1)
                self.state = 108
                self.match(JSONParser.T__2)
                self.state = 109
                self.match(JSONParser.T__13)
                pass

            elif la_ == 2:
                self.enterOuterAlt(localctx, 2)
                self.state = 110
                self.match(JSONParser.T__2)
                self.state = 111
                self.match(JSONParser.T__15)
                pass

            elif la_ == 3:
                self.enterOuterAlt(localctx, 3)
                self.state = 112
                self.match(JSONParser.T__2)
                self.state = 113
                self.match(JSONParser.T__12)
                pass

            elif la_ == 4:
                self.enterOuterAlt(localctx, 4)
                self.state = 114
                self.match(JSONParser.T__2)
                self.state = 115
                self.match(JSONParser.T__14)
                pass


        except RecognitionException as re:
            localctx.exception = re
            self._errHandler.reportError(self, re)
            self._errHandler.recover(self, re)
        finally:
            self.exitRule()
        return localctx





