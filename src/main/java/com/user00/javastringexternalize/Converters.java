package com.user00.javastringexternalize;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

   /**
    * Read xliff format used by Apple
    */
   public static List<Translation> readXliff12File(String xliffFile)
   {
      List<Translation> translations = new ArrayList<>();
      
      // Read the XML
      Document doc = null;
      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         doc = db.parse(new InputSource(new StringReader(xliffFile)));
         
      } catch (ParserConfigurationException | SAXException | IOException e)
      {
         e.printStackTrace();
      }
      if (doc == null) return translations;
      
      // Convert things to translations
      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      try
      {
         NodeList files = (NodeList)xpath.evaluate("/xliff/file", doc, XPathConstants.NODESET);
         for (int fileIdx = 0; fileIdx < files.getLength(); fileIdx++)
         {
            // Check if it's a strings file or a UI file
            Element fileEl = (Element)files.item(fileIdx);
            boolean isStrings = false;
            if (fileEl.hasAttribute("original") && fileEl.getAttribute("original").endsWith(".strings"))
               isStrings = true;
            
            // Traverse the translations
            NodeList transUnits = (NodeList)xpath.evaluate("body/trans-unit", fileEl, XPathConstants.NODESET);
            for (int transIdx = 0; transIdx < transUnits.getLength(); transIdx++)
            {
               Element transEl = (Element)transUnits.item(transIdx);
               String key = transEl.getAttribute("id");
               String val = xpath.evaluate("source", transEl);
               String note = xpath.evaluate("note", transEl);
               // Comments from the UI are useless, so we will discard them
               if (!isStrings) note = "";
               if ("No comment provided by engineer.".equals(note)) note = "";
               translations.add(new Translation(key, val, note));
            }
         }
      } catch (XPathExpressionException e)
      {
         e.printStackTrace();
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
