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
		T__9=10, T__10=11, T__11=12, STRING=13, NUMBER=14, TYPE=15, IDENTIFIER=16, 
		WS=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "STRING", "ESC", "UNICODE", "HEX", "SAFECODEPOINT", 
		"NUMBER", "INT", "EXP", "TYPE", "IDENTIFIER", "LetterOrDigit", "Letter", 
		"WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "','", "'{'", "'}'", "':'", "'['", "'L'", "'.'", "'$'", "']'", "'true'", 
		"'false'", "'null'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, "STRING", "NUMBER", "TYPE", "IDENTIFIER", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\23\u00ad\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3"+
		"\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3"+
		"\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\7\16[\n\16\f\16\16\16^\13\16\3\16\3"+
		"\16\3\17\3\17\3\17\5\17e\n\17\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21"+
		"\3\22\3\22\3\23\5\23r\n\23\3\23\3\23\3\23\6\23w\n\23\r\23\16\23x\5\23"+
		"{\n\23\3\23\5\23~\n\23\3\24\3\24\3\24\7\24\u0083\n\24\f\24\16\24\u0086"+
		"\13\24\5\24\u0088\n\24\3\25\3\25\5\25\u008c\n\25\3\25\3\25\3\26\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\5\26\u0098\n\26\3\27\3\27\7\27\u009c\n\27"+
		"\f\27\16\27\u009f\13\27\3\30\3\30\5\30\u00a3\n\30\3\31\3\31\3\32\6\32"+
		"\u00a8\n\32\r\32\16\32\u00a9\3\32\3\32\2\2\33\3\3\5\4\7\5\t\6\13\7\r\b"+
		"\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\2\37\2!\2#\2%\20\'\2)\2+\21"+
		"-\22/\2\61\2\63\23\3\2\13\n\2$$\61\61^^ddhhppttvv\5\2\62;CHch\5\2\2!$"+
		"$^^\3\2\62;\3\2\63;\4\2GGgg\4\2--//\4\2C\\c|\5\2\13\f\17\17\"\"\2\u00b2"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2%\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2\63\3\2\2"+
		"\2\3\65\3\2\2\2\5\67\3\2\2\2\79\3\2\2\2\t;\3\2\2\2\13=\3\2\2\2\r?\3\2"+
		"\2\2\17A\3\2\2\2\21C\3\2\2\2\23E\3\2\2\2\25G\3\2\2\2\27L\3\2\2\2\31R\3"+
		"\2\2\2\33W\3\2\2\2\35a\3\2\2\2\37f\3\2\2\2!l\3\2\2\2#n\3\2\2\2%q\3\2\2"+
		"\2\'\u0087\3\2\2\2)\u0089\3\2\2\2+\u0097\3\2\2\2-\u0099\3\2\2\2/\u00a2"+
		"\3\2\2\2\61\u00a4\3\2\2\2\63\u00a7\3\2\2\2\65\66\7.\2\2\66\4\3\2\2\2\67"+
		"8\7}\2\28\6\3\2\2\29:\7\177\2\2:\b\3\2\2\2;<\7<\2\2<\n\3\2\2\2=>\7]\2"+
		"\2>\f\3\2\2\2?@\7N\2\2@\16\3\2\2\2AB\7\60\2\2B\20\3\2\2\2CD\7&\2\2D\22"+
		"\3\2\2\2EF\7_\2\2F\24\3\2\2\2GH\7v\2\2HI\7t\2\2IJ\7w\2\2JK\7g\2\2K\26"+
		"\3\2\2\2LM\7h\2\2MN\7c\2\2NO\7n\2\2OP\7u\2\2PQ\7g\2\2Q\30\3\2\2\2RS\7"+
		"p\2\2ST\7w\2\2TU\7n\2\2UV\7n\2\2V\32\3\2\2\2W\\\7$\2\2X[\5\35\17\2Y[\5"+
		"#\22\2ZX\3\2\2\2ZY\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]_\3\2\2\2^"+
		"\\\3\2\2\2_`\7$\2\2`\34\3\2\2\2ad\7^\2\2be\t\2\2\2ce\5\37\20\2db\3\2\2"+
		"\2dc\3\2\2\2e\36\3\2\2\2fg\7w\2\2gh\5!\21\2hi\5!\21\2ij\5!\21\2jk\5!\21"+
		"\2k \3\2\2\2lm\t\3\2\2m\"\3\2\2\2no\n\4\2\2o$\3\2\2\2pr\7/\2\2qp\3\2\2"+
		"\2qr\3\2\2\2rs\3\2\2\2sz\5\'\24\2tv\7\60\2\2uw\t\5\2\2vu\3\2\2\2wx\3\2"+
		"\2\2xv\3\2\2\2xy\3\2\2\2y{\3\2\2\2zt\3\2\2\2z{\3\2\2\2{}\3\2\2\2|~\5)"+
		"\25\2}|\3\2\2\2}~\3\2\2\2~&\3\2\2\2\177\u0088\7\62\2\2\u0080\u0084\t\6"+
		"\2\2\u0081\u0083\t\5\2\2\u0082\u0081\3\2\2\2\u0083\u0086\3\2\2\2\u0084"+
		"\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0088\3\2\2\2\u0086\u0084\3\2"+
		"\2\2\u0087\177\3\2\2\2\u0087\u0080\3\2\2\2\u0088(\3\2\2\2\u0089\u008b"+
		"\t\7\2\2\u008a\u008c\t\b\2\2\u008b\u008a\3\2\2\2\u008b\u008c\3\2\2\2\u008c"+
		"\u008d\3\2\2\2\u008d\u008e\5\'\24\2\u008e*\3\2\2\2\u008f\u0090\7V\2\2"+
		"\u0090\u0091\7[\2\2\u0091\u0092\7R\2\2\u0092\u0098\7G\2\2\u0093\u0094"+
		"\7v\2\2\u0094\u0095\7{\2\2\u0095\u0096\7r\2\2\u0096\u0098\7g\2\2\u0097"+
		"\u008f\3\2\2\2\u0097\u0093\3\2\2\2\u0098,\3\2\2\2\u0099\u009d\5\61\31"+
		"\2\u009a\u009c\5/\30\2\u009b\u009a\3\2\2\2\u009c\u009f\3\2\2\2\u009d\u009b"+
		"\3\2\2\2\u009d\u009e\3\2\2\2\u009e.\3\2\2\2\u009f\u009d\3\2\2\2\u00a0"+
		"\u00a3\5\61\31\2\u00a1\u00a3\t\5\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a1\3"+
		"\2\2\2\u00a3\60\3\2\2\2\u00a4\u00a5\t\t\2\2\u00a5\62\3\2\2\2\u00a6\u00a8"+
		"\t\n\2\2\u00a7\u00a6\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9"+
		"\u00aa\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00ac\b\32\2\2\u00ac\64\3\2\2"+
		"\2\21\2Z\\dqxz}\u0084\u0087\u008b\u0097\u009d\u00a2\u00a9\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}