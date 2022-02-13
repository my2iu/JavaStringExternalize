package com.user00.javastringexternalize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import com.user00.javastringexternalize.StringSubstitution.SubstitutionType;

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
   
   public void fillInKeySubstitutions(BiFunction<String, Integer, String> keyGenerator)
   {
      for (int n = 0; n < substitutions.size(); n++) 
      {
         StringSubstitution sub = substitutions.get(n); 
         if (sub.substitution != SubstitutionType.SUBSTITUTE) continue;
         if (!"".equals(sub.replacementKey)) continue;
         sub.replacementKey = keyGenerator.apply(sub.token.getText(), n);
      }
   }
   
   public List<StringSubstitution> getSubstitutions()
   {
      return substitutions;
   }

   public String getTransformedFile()
   {
      // Figure out all the substitutions that we need
      Map<Integer, StringSubstitution> subsLookup = new HashMap<>();
      for (StringSubstitution sub: substitutions)
            subsLookup.put(sub.getIndex(), sub);
      // Apply the substitutions
      String toReturn = "";
      for (int n = 0; n < tokens.size(); n++)
      {
         Token tok = tokens.get(n);
         StringSubstitution sub = subsLookup.get(n);
         if (sub == null || sub.substitution != SubstitutionType.SUBSTITUTE)
         {
            toReturn += tok.getText();
         }
         else
         {
            toReturn += sub.replacementKey;
         }
      }
      return toReturn;
   }
}
