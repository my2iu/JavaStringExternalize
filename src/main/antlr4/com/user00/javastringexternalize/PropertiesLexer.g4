lexer grammar PropertiesLexer;

CRLF: ENDL -> channel(HIDDEN);
WS: Whitespace+ -> channel(HIDDEN);
COMMENT:  ('#' | '!') ~[\r\n]* -> channel(HIDDEN);
KEY: ('\\=' | '\\:' | '\\ ' | ~[ #!\t\f\r\n]) ( '\\=' | '\\:' | '\\ ' | ~[= \t\f\r\n])*  -> mode(EQUALS_MODE);

mode EQUALS_MODE;

WS_EQUALS: WS -> type(WS),channel(HIDDEN);
CRLF_EQUALS: CRLF -> type(CRLF),channel(HIDDEN),mode(DEFAULT_MODE);
EQUALS: ('=' | ':') -> mode(VALUE_MODE);

mode VALUE_MODE;

WS_VALUE: WS -> type(WS),channel(HIDDEN);
CRLF_VALUE: CRLF -> type(CRLF),channel(HIDDEN),mode(DEFAULT_MODE);
VALUE: ('\\' ENDL Whitespace* | ~[ \t\f\r\n]) ('\\' ENDL | ~[\r\n])* -> mode(DEFAULT_MODE);


fragment Whitespace
	: [ \t\f];

fragment ENDL
	: '\r\n'
	| '\r'
	| '\n';
	
