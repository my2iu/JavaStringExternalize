package com.user00.javastringexternalize;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

public class PropertiesLexerTest
{
   @Test
   public void basicLexerTest()
   {
      PropertiesLexer lexer = new PropertiesLexer(CharStreams.fromString(
            "  hello =   Hello, how are you?  \n"
            + "no\n"
            + "\n"
            + "#comment sdf \\\n"
            + "go = yes\\\n    newline"));
      List<Integer> tokenTypes = lexer.getAllTokens().stream().map(tok -> tok.getType()).collect(Collectors.toList()); 
      assertIterableEquals(Arrays.asList(
            PropertiesLexer.WS, PropertiesLexer.KEY, PropertiesLexer.WS, PropertiesLexer.EQUALS, PropertiesLexer.WS, PropertiesLexer.VALUE, PropertiesLexer.CRLF,
            PropertiesLexer.KEY, PropertiesLexer.CRLF,
            PropertiesLexer.CRLF,
            PropertiesLexer.COMMENT, PropertiesLexer.CRLF,
            PropertiesLexer.KEY, PropertiesLexer.WS, PropertiesLexer.EQUALS, PropertiesLexer.WS, PropertiesLexer.VALUE),
            tokenTypes);
   }
}
