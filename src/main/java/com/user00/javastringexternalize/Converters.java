package com.user00.javastringexternalize;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
   public static List<Translation> readXliff12File(String xliffFile, boolean includeTranslated, boolean includeUntranslated)
   {
      List<Translation> translations = new ArrayList<>();
      
      // Read the XML
      Document doc = parseXmlDomFromString(xliffFile);
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
            boolean isStoryboard = false;
            if (fileEl.hasAttribute("original") && fileEl.getAttribute("original").endsWith(".strings"))
               isStrings = true;
            if (fileEl.hasAttribute("original") && fileEl.getAttribute("original").endsWith(".storyboard"))
               isStoryboard = true;
            
            // Traverse the translations
            NodeList transUnits = (NodeList)xpath.evaluate("body/trans-unit", fileEl, XPathConstants.NODESET);
            for (int transIdx = 0; transIdx < transUnits.getLength(); transIdx++)
            {
               Element transEl = (Element)transUnits.item(transIdx);
               String key = transEl.getAttribute("id");
               String val = xpath.evaluate("source", transEl);
               String note = xpath.evaluate("note", transEl);
               String target = xpath.evaluate("target", transEl);
               boolean hasTranslation = target != null && !target.isEmpty() && !target.equals(val);
               if (hasTranslation && !includeTranslated) continue;
               if (!hasTranslation && !includeUntranslated) continue;
               // Comments from the UI are useless, so we will discard them
               if (isStoryboard)
               {
                  // It is possible for the programmers to insert translation notes in the storyboard UI, but it 
                  // requires extra parsing
                  Matcher m = Pattern.compile("Note = [\"](.*)[\"];").matcher(note);
                  if (m != null && m.find())
                     note = m.group(1);
                  else
                     note = "";
               }
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
   
   public static List<Translation> readStringsXmlFile(String xmlFile)
   {
      List<Translation> translations = new ArrayList<>();
      
      // Read the XML
      Document doc = parseXmlDomFromString(xmlFile);
      if (doc == null) return translations;
      
      // Convert each <string> to a translation
      String comments = "";
      if ("resouces".equals(doc.getDocumentElement().getNodeName()))
      {
         System.err.println("Strings.xml file does not have a <resources> element as the document element");
         return translations;
      }
      NodeList nodes = doc.getDocumentElement().getChildNodes();
      for (int n = 0; n < nodes.getLength(); n++)
      {
         Node node = nodes.item(n);
         if (node.getNodeType() == Node.COMMENT_NODE)
         {
            if (!comments.isEmpty())
            comments += "\n";
            comments += node.getNodeValue();
         }
         else if (node.getNodeType() == Node.ELEMENT_NODE)
         {
            Element el = (Element)node;
            if ("string".equals(node.getNodeName()))
            {
               translations.add(new Translation(el.getAttribute("name"), 
                     el.getTextContent(), comments));
            }
            comments = "";
         }
      }

      return translations;
   }
   
   private static Document parseXmlDomFromString(String xml)
   {
      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         return db.parse(new InputSource(new StringReader(xml)));
         
      } catch (ParserConfigurationException | SAXException | IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   /**
    * From a list of translations, create a map of translation keys to translations
    */
   private static Map<String, Translation> createTranslationMap(Collection<Translation> translations)
   {
      Map<String, Translation> translationMap = new HashMap<>();
      translations.forEach(trans -> translationMap.put(trans.key, trans));
      return translationMap;
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
         toReturn += String.format("  <string name=\"%s\">%s</string>\n", trans.key, StringEscapeUtils.escapeXml10(trans.text));
      }
      toReturn += "</resources>\n";
      return toReturn;
   }

   public static String translationsToProperties(List<Translation> translations)
   {
      String toReturn = "";
      for (Translation trans: translations)
      {
         if (!trans.note.isEmpty())
         {
            String [] noteLines = trans.note.split("\\R");
            for (String line: noteLines)
               toReturn += "#" + line + "\n";
         }
         toReturn += String.format("%s = %s\n", trans.key, trans.text);
      }
      return toReturn;
   }

   public static String mergeTranslationsIntoProperties(Collection<Translation> translations, String propsFile, boolean outputEvenIfUntranslated)
   {
      Map<String, Translation> transMap = createTranslationMap(translations);
      String toReturn = "";
      PropertiesLexer lexer = new PropertiesLexer(CharStreams.fromString(propsFile));
      String key = "";
      boolean skipToCrlf = false;
      for (Token tok: lexer.getAllTokens())
      {
         if (skipToCrlf && tok.getType() != PropertiesLexer.CRLF)
            continue;
         switch (tok.getType())
         {
         case PropertiesLexer.KEY:
         {
            key = tok.getText();
            Translation trans = transMap.get(key);
            if (trans != null)
            {
               toReturn += key + " = " + trans.text;
               skipToCrlf = true;
            }
            else if (outputEvenIfUntranslated)
            {
               toReturn += key;
            }
            else 
            {
               skipToCrlf = true;
            }
            break;
         }
         case PropertiesLexer.CRLF:
            skipToCrlf = false;
            // Fall through
         default:
            toReturn += tok.getText();
            break;
         }
      }
      return toReturn;
   }
   
   public static String mergeTranslationsIntoXliff(List<Translation> translations, String xliffFile)
   {
      Map<String, Translation> transMap = createTranslationMap(translations);
      
      // Read the XML
      Document doc = parseXmlDomFromString(xliffFile);
      if (doc == null) return null;
      
      // Walk through every translation unit in the xliff file
      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      try
      {
         NodeList transUnits = (NodeList)xpath.evaluate("/xliff/file/body/trans-unit", doc, XPathConstants.NODESET);
         for (int transIdx = 0; transIdx < transUnits.getLength(); transIdx++)
         {
            // Check if we have a translation available for the string
            Element transEl = (Element)transUnits.item(transIdx);
            String id = transEl.getAttribute("id");
            Translation trans = transMap.get(id);
            if (trans != null)
            {
               // Substitute in a new <target> tag with the translation
               // (assume that there is only a single <target> even though multiple are allowed)
               NodeList targetTags = transEl.getElementsByTagName("target");
               Element targetEl;
               if (targetTags.getLength() < 1)
               {
                  targetEl = doc.createElement("target");
                  transEl.appendChild(targetEl);
               }
               else if (targetTags.getLength() > 1)
               {
                  System.err.println("More than one translation <target> found in xliff file");
                  targetEl = (Element)targetTags.item(0);
               }
               else
                  targetEl = (Element)targetTags.item(0);
               targetEl.setTextContent(trans.text);
            }
         }
      } 
      catch (XPathExpressionException e)
      {
         e.printStackTrace();
      }
      
      // Write out the changed xliff file
      try {
         Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
         StringWriter writer = new StringWriter();
         transformer.transform(new DOMSource(doc), new StreamResult(writer));
         return writer.toString();
      } 
      catch (TransformerConfigurationException e)
      {
         e.printStackTrace();
      } catch (TransformerException e)
      {
         e.printStackTrace();
      }
      return null;
   }
}
