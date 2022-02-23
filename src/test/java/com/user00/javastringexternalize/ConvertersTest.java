package com.user00.javastringexternalize;

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
}
