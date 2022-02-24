package com.user00.javastringexternalize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ConvertersTest
{
   @Test
   public void testReadPropertiesFile()
   {
      List<Converters.Translation> translations = Converters.readPropertiesFile(
            "#Comment\n"
            + "Hello = This is hello\n"
            + "\n"
            + "go = Go\n"
            + "#Another comment\n"
            + "# two comments\n"
            + " OK = okay\n"
            + "keyOnly");
      assertIterableEquals(
            Arrays.asList(
                  new Converters.Translation("Hello", "This is hello", "Comment"),
                  new Converters.Translation("go", "Go", ""),
                  new Converters.Translation("OK", "okay", "Another comment\n two comments"),
                  new Converters.Translation("keyOnly", "", "")
                  ),
            translations);
   }
   
   @Test
   public void testTranslationsToStringsXml()
   {
      String xml = Converters.translationsToStringsXml(Arrays.asList(
                  new Converters.Translation("Hello", "This is hello", "Comment"),
                  new Converters.Translation("go", "Go", ""),
                  new Converters.Translation("OK", "okay", "Another comment\n two comments"),
                  new Converters.Translation("keyOnly", "", "")
                  ));
      assertEquals("<resources>\n"
            + "<!--Comment-->\n"
            + "  <string name=\"Hello\">This is hello</string>\n"
            + "  <string name=\"go\">Go</string>\n"
            + "<!--Another comment\n"
            + " two comments-->\n"
            + "  <string name=\"OK\">okay</string>\n"
            + "  <string name=\"keyOnly\"></string>\n"
            + "</resources>\n", xml);
   }
   
   @Test
   public void testReadXliff12File()
   {
      List<Converters.Translation> translations = Converters.readXliff12File(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.2\" xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 http://docs.oasis-open.org/xliff/v1.2/os/xliff-core-1.2-strict.xsd\">\n"
            + "  <file original=\"Omber/Base.lproj/AskForRating.storyboard\" source-language=\"en\" target-language=\"fr\" datatype=\"plaintext\">\n"
            + "    <header>\n"
            + "      <tool tool-id=\"com.apple.dt.xcode\" tool-name=\"Xcode\" tool-version=\"13.2.1\" build-num=\"13C100\"/>\n"
            + "    </header>\n"
            + "    <body>\n"
            + "      <trans-unit id=\"Djc-sA-1YT.title\" xml:space=\"preserve\">\n"
            + "        <source>Jello</source>\n"
            + "        <target>Jello2</target>\n"
            + "        <note>Class = \"NSButtonCell\"; title = \"Jello\"; ObjectID = \"Djc-sA-1YT\";</note>\n"
            + "      </trans-unit>"
            + "</body></file>"
            + " <file original=\"Omber/en.lproj/Localizable.strings\" datatype=\"plaintext\" source-language=\"en\" target-language=\"fr\">\n"
            + "    <header>\n"
            + "      <tool tool-id=\"com.apple.dt.xcode\" tool-name=\"Xcode\" tool-version=\"13.2.1\" build-num=\"13C100\"/>\n"
            + "    </header>\n"
            + "    <body>\n"
            + "      <trans-unit id=\"hi\" xml:space=\"preserve\">\n"
            + "        <source>hello</source>\n"
            + "        <note>No comment provided by engineer.</note>\n"
            + "      </trans-unit>\n"
            + "      <trans-unit id=\"hi2\" xml:space=\"preserve\">\n"
            + "        <source>hello2</source>\n"
            + "        <note>a comment.</note>\n"
            + "      </trans-unit>\n"
            + "</body></file></xliff>");
      assertIterableEquals(
            Arrays.asList(
                  new Converters.Translation("Djc-sA-1YT.title", "Jello", ""),
                  new Converters.Translation("hi", "hello", ""),
                  new Converters.Translation("hi2", "hello2", "a comment.")
                  ),
            translations);
   }
   
   @Test
   public void testReadStringsXml()
   {
      List<Converters.Translation> translations = Converters.readStringsXmlFile(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><resources>\n"
            + "  <string name=\"Cancel\">Annuler</string>\n"
            + "  \n"
            + "  <!--Menu option-->\n"
            + "<!-- Additional Comment  -->\n"
            + "  <string name=\"Exit\">Sortir</string>\n"
            + "  <string name=\"dMs-cI-mzQ.title\">Fichier</string>\n"
            + "</resources>"
      );
      assertIterableEquals(
            Arrays.asList(
                  new Converters.Translation("Cancel", "Annuler", ""),
                  new Converters.Translation("Exit", "Sortir", "Menu option\n Additional Comment  "),
                  new Converters.Translation("dMs-cI-mzQ.title", "Fichier", "")
                  ),
            translations);
   }
}
