package com.user00.javastringexternalize;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class JavaFileStringTracker
{
   String fileContents;
   List<? extends Token> tokens;
   List<StringSubstitution> substitutions;
   
   public JavaFileStringTracker(String fileContents)
   {
      this.fileContents = fileContents;
      JavaLexer lexer = new JavaLexer(CharStreams.fromString(fileContents));
      tokens = lexer.getAllTokens();
      substitutions = new ArrayList<>();
      for (int n = 0; n < tokens.size(); n++)
      {
         if (tokens.get(n).getType() == JavaLexer.STRING_LITERAL)
         {
            substitutions.add(new StringSubstitution(tokens.get(n), n,
                  generateContextAroundTokenIndex(n)));
         }
      }
   }
   
   String generateContextAroundTokenIndex(int tokidx)
   {
      // Step backwards 2 CRLF tokens
      int beginIdx, endIdx, crlfCount;
      for (beginIdx = tokidx, crlfCount = 0; 
            beginIdx >= 0 && crlfCount < 2; 
            beginIdx--)
      {
         if (tokens.get(beginIdx).getType() == JavaLexer.CRLF)
            crlfCount++;
      }
      if (beginIdx < 0)
         beginIdx++;
      else 
         beginIdx += 2;
      
      // Step forwards 2 CRLF tokens
      for (endIdx = tokidx, crlfCount = 0; 
            endIdx < tokens.size() && crlfCount < 2; 
            endIdx++)
      {
         if (tokens.get(endIdx).getType() == JavaLexer.CRLF)
            crlfCount++;
      }
      if (endIdx >= tokens.size())
         endIdx--;
      else 
         endIdx -= 2;

      // Pull out text of the context
      return fileContents.substring(tokens.get(beginIdx).getStartIndex(),
            tokens.get(endIdx).getStopIndex() + 1);
   }
   
   public List<StringSubstitution> getSubstitutions()
   {
      return substitutions;
   }
}
