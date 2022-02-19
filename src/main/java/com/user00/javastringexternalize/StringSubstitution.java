package com.user00.javastringexternalize;

import org.antlr.v4.runtime.Token;

public class StringSubstitution
{
   Token token;
   enum SubstitutionType
   {
      IGNORE, SKIP, SUBSTITUTE
   }
   int index;
   SubstitutionType substitution;
   String surroundingContext;  // Not used in the end
   String replacementKey = "";
   
   public StringSubstitution(Token token, int index, String surroundingContext)
   {
      this.token = token;
      this.index = index;
      substitution = SubstitutionType.SKIP;
      this.surroundingContext = surroundingContext;
   }

   int getIndex()
   {
      return index;
   }
   
   int getPositionInFile()
   {
      return token.getStartIndex();
   }
   
   int getPositionEndInFile()
   {
      return token.getStopIndex();
   }
   
   String getSurroundingContext()
   {
      return surroundingContext;
   }
   
   String getReplacementKey()
   {
      return replacementKey;
   }
   
   void setReplacementKey(String key)
   {
      this.replacementKey = key;
   }
}
