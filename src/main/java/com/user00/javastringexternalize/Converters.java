package com.user00.javastringexternalize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class Converters {
   public static class Translation
   {
      public Translation(String key, String text, String note) {
         this.key = key;
         this.note = note;
         this.text = text;
      }
      String key = "";
      String text = "";
      String note = "";
      @Override
      public int hashCode()
      {
         return Objects.hash(key, note, text);
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         Translation other = (Translation) obj;
         return Objects.equals(key, other.key) && Objects.equals(note, other.note) && Objects.equals(text, other.text);
      }      
   }

   public static List<Translation> readPropertiesFile(String propsFile)
   {
      // Add an extra blank line at the end of the properties file
      // to make it easier to parse the last line
      propsFile += "\n";
      PropertiesLexer lexer = new PropertiesLexer(CharStreams.fromString(propsFile));
      String key = "";
      String val = "";
      String notes = "";
      List<Translation> translations = new ArrayList<>();
      for (Token tok: lexer.getAllTokens())
      {
         switch (tok.getType())
         {
         case PropertiesLexer.KEY:
            key = tok.getText();
            break;
         case PropertiesLexer.VALUE:
            val = tok.getText();
            break;
         case PropertiesLexer.COMMENT:
            if (!notes.isEmpty()) notes += "\n";
            notes += tok.getText().substring(1);
            break;
         case PropertiesLexer.CRLF:
         {
            if (key.isEmpty()) break;
            Translation trans = new Translation(key, val, notes);
            translations.add(trans);
            key = "";
            val = "";
            notes = "";
            break;
         }
         default:
            break;
         }
      }
      return translations;
   }

   public static String translationsToStringsXml(List<Translation> translations)
   {
      String toReturn = "";
      toReturn += "<resources>\n";
      for (Translation trans: translations)
      {
         if (!trans.note.isEmpty())
         {
            toReturn += "<!--" + trans.note + "-->\n";
         }
         toReturn += String.format("  <string name=\"%s\">%s</string>\n", trans.key, trans.text);
      }
      toReturn += "</resources>\n";
      return toReturn;
   }
}
