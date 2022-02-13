package com.user00.javastringexternalize;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavaStringExternalizeTest
{
   @Test
   void basicLexerTest()
   {
      JavaLexer lexer = new JavaLexer(CharStreams.fromString("if (hello \n\t"));
      Token tok;
      tok = lexer.nextToken();
      assertEquals(1, tok.getLine());
      assertEquals("if", tok.getText());
      assertEquals(JavaLexer.IF, tok.getType());
      tok = lexer.nextToken();
      assertEquals(JavaLexer.WS, tok.getType());
      assertEquals(" ", tok.getText());
      tok = lexer.nextToken();
      assertEquals(JavaLexer.LPAREN, tok.getType());
      assertEquals("(", tok.getText());
      tok = lexer.nextToken();
      assertEquals(JavaLexer.IDENTIFIER, tok.getType());
      assertEquals("hello", tok.getText());
      tok = lexer.nextToken();
      assertEquals(JavaLexer.WS, tok.getType());
      assertEquals(" \n\t", tok.getText());
      tok = lexer.nextToken();
      assertEquals(JavaLexer.EOF, tok.getType());

   }
}
