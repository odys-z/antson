// Generated from JSON.g4 by ANTLR 4.7.1
package gen.antlr.json;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSONLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, STRING=11, NUMBER=12, TYPE=13, IDENTIFIER=14, WS=15;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "STRING", "ESC", "UNICODE", "HEX", "SAFECODEPOINT", "NUMBER", 
		"INT", "EXP", "TYPE", "IDENTIFIER", "LetterOrDigit", "Letter", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'{'", "','", "'}'", "':'", "'['", "']'", "'.'", "'true'", "'false'", 
		"'null'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, "STRING", 
		"NUMBER", "TYPE", "IDENTIFIER", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JSONLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JSON.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\21\u00a5\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\7\fS\n"+
		"\f\f\f\16\fV\13\f\3\f\3\f\3\r\3\r\3\r\5\r]\n\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\17\3\17\3\20\3\20\3\21\5\21j\n\21\3\21\3\21\3\21\6\21o\n\21\r"+
		"\21\16\21p\5\21s\n\21\3\21\5\21v\n\21\3\22\3\22\3\22\7\22{\n\22\f\22\16"+
		"\22~\13\22\5\22\u0080\n\22\3\23\3\23\5\23\u0084\n\23\3\23\3\23\3\24\3"+
		"\24\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u0090\n\24\3\25\3\25\7\25\u0094"+
		"\n\25\f\25\16\25\u0097\13\25\3\26\3\26\5\26\u009b\n\26\3\27\3\27\3\30"+
		"\6\30\u00a0\n\30\r\30\16\30\u00a1\3\30\3\30\2\2\31\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\n\23\13\25\f\27\r\31\2\33\2\35\2\37\2!\16#\2%\2\'\17)\20"+
		"+\2-\2/\21\3\2\13\n\2$$\61\61^^ddhhppttvv\5\2\62;CHch\5\2\2!$$^^\3\2\62"+
		";\3\2\63;\4\2GGgg\4\2--//\4\2C\\c|\5\2\13\f\17\17\"\"\2\u00aa\2\3\3\2"+
		"\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17"+
		"\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2!\3\2\2"+
		"\2\2\'\3\2\2\2\2)\3\2\2\2\2/\3\2\2\2\3\61\3\2\2\2\5\63\3\2\2\2\7\65\3"+
		"\2\2\2\t\67\3\2\2\2\139\3\2\2\2\r;\3\2\2\2\17=\3\2\2\2\21?\3\2\2\2\23"+
		"D\3\2\2\2\25J\3\2\2\2\27O\3\2\2\2\31Y\3\2\2\2\33^\3\2\2\2\35d\3\2\2\2"+
		"\37f\3\2\2\2!i\3\2\2\2#\177\3\2\2\2%\u0081\3\2\2\2\'\u008f\3\2\2\2)\u0091"+
		"\3\2\2\2+\u009a\3\2\2\2-\u009c\3\2\2\2/\u009f\3\2\2\2\61\62\7}\2\2\62"+
		"\4\3\2\2\2\63\64\7.\2\2\64\6\3\2\2\2\65\66\7\177\2\2\66\b\3\2\2\2\678"+
		"\7<\2\28\n\3\2\2\29:\7]\2\2:\f\3\2\2\2;<\7_\2\2<\16\3\2\2\2=>\7\60\2\2"+
		">\20\3\2\2\2?@\7v\2\2@A\7t\2\2AB\7w\2\2BC\7g\2\2C\22\3\2\2\2DE\7h\2\2"+
		"EF\7c\2\2FG\7n\2\2GH\7u\2\2HI\7g\2\2I\24\3\2\2\2JK\7p\2\2KL\7w\2\2LM\7"+
		"n\2\2MN\7n\2\2N\26\3\2\2\2OT\7$\2\2PS\5\31\r\2QS\5\37\20\2RP\3\2\2\2R"+
		"Q\3\2\2\2SV\3\2\2\2TR\3\2\2\2TU\3\2\2\2UW\3\2\2\2VT\3\2\2\2WX\7$\2\2X"+
		"\30\3\2\2\2Y\\\7^\2\2Z]\t\2\2\2[]\5\33\16\2\\Z\3\2\2\2\\[\3\2\2\2]\32"+
		"\3\2\2\2^_\7w\2\2_`\5\35\17\2`a\5\35\17\2ab\5\35\17\2bc\5\35\17\2c\34"+
		"\3\2\2\2de\t\3\2\2e\36\3\2\2\2fg\n\4\2\2g \3\2\2\2hj\7/\2\2ih\3\2\2\2"+
		"ij\3\2\2\2jk\3\2\2\2kr\5#\22\2ln\7\60\2\2mo\t\5\2\2nm\3\2\2\2op\3\2\2"+
		"\2pn\3\2\2\2pq\3\2\2\2qs\3\2\2\2rl\3\2\2\2rs\3\2\2\2su\3\2\2\2tv\5%\23"+
		"\2ut\3\2\2\2uv\3\2\2\2v\"\3\2\2\2w\u0080\7\62\2\2x|\t\6\2\2y{\t\5\2\2"+
		"zy\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\u0080\3\2\2\2~|\3\2\2\2\177"+
		"w\3\2\2\2\177x\3\2\2\2\u0080$\3\2\2\2\u0081\u0083\t\7\2\2\u0082\u0084"+
		"\t\b\2\2\u0083\u0082\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0085\3\2\2\2\u0085"+
		"\u0086\5#\22\2\u0086&\3\2\2\2\u0087\u0088\7V\2\2\u0088\u0089\7[\2\2\u0089"+
		"\u008a\7R\2\2\u008a\u0090\7G\2\2\u008b\u008c\7v\2\2\u008c\u008d\7{\2\2"+
		"\u008d\u008e\7r\2\2\u008e\u0090\7g\2\2\u008f\u0087\3\2\2\2\u008f\u008b"+
		"\3\2\2\2\u0090(\3\2\2\2\u0091\u0095\5-\27\2\u0092\u0094\5+\26\2\u0093"+
		"\u0092\3\2\2\2\u0094\u0097\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2"+
		"\2\2\u0096*\3\2\2\2\u0097\u0095\3\2\2\2\u0098\u009b\5-\27\2\u0099\u009b"+
		"\t\5\2\2\u009a\u0098\3\2\2\2\u009a\u0099\3\2\2\2\u009b,\3\2\2\2\u009c"+
		"\u009d\t\t\2\2\u009d.\3\2\2\2\u009e\u00a0\t\n\2\2\u009f\u009e\3\2\2\2"+
		"\u00a0\u00a1\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a3"+
		"\3\2\2\2\u00a3\u00a4\b\30\2\2\u00a4\60\3\2\2\2\21\2RT\\ipru|\177\u0083"+
		"\u008f\u0095\u009a\u00a1\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}