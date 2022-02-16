package com.user00.javastringexternalize;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.user00.javastringexternalize.StringSubstitution.SubstitutionType;

public class JavaFileStringTrackerTest
{
   @Test
   public void testFindStrings()
   {
      JavaFileStringTracker tracker = new JavaFileStringTracker("package \"string\" goes \"sdf sdf\"{");
      Assertions.assertIterableEquals(
            Arrays.asList(2, 6), 
            tracker.getSubstitutions().stream().map(tok -> tok.getIndex()).collect(Collectors.toList()));
   }
   
   
   @Test
   void testGenerateContextAroundTokenIndex()
   {
      JavaFileStringTracker tracker = new JavaFileStringTracker("if (\n\"hello\" while\nint n = \"go\"\nprint(\"hi\")\n\"test\"");
      Assertions.assertEquals(4, tracker.getSubstitutions().size());
      Assertions.assertEquals("if (\n\"hello\" while\nint n = \"go\"", tracker.getSubstitutions().get(0).getSurroundingContext());
      Assertions.assertEquals("\"hello\" while\nint n = \"go\"\nprint(\"hi\")", tracker.getSubstitutions().get(1).getSurroundingContext());
      Assertions.assertEquals("int n = \"go\"\nprint(\"hi\")\n\"test\"", tracker.getSubstitutions().get(2).getSurroundingContext());
      Assertions.assertEquals("print(\"hi\")\n\"test\"", tracker.getSubstitutions().get(3).getSurroundingContext());
   }
   
   @Test
   void testGetTransformedFile()
   {
      JavaFileStringTracker tracker = new JavaFileStringTracker("if (\n\"hello\" while\nint n = \"go\"\nprint(\"hi\")\n\"test\"");
      tracker.getSubstitutions().get(1).setReplacementKey("TEST1");
      tracker.getSubstitutions().get(1).substitution = SubstitutionType.SUBSTITUTE;
      tracker.getSubstitutions().get(3).setReplacementKey("TEST2");
      tracker.getSubstitutions().get(3).substitution = SubstitutionType.SUBSTITUTE;
      Assertions.assertEquals("if (\n\"hello\" while\nint n = Messages.TEST1\nprint(\"hi\")\nMessages.TEST2", tracker.getTransformedFile());
   }

   @Test
   void testGetTransformedFileWithNewImport()
   {
      JavaFileStringTracker tracker = new JavaFileStringTracker("\nimport com.example.Messages;\nif (\n\"hello\" while\nint n = \"go\"\nprint(\"hi\")\n\"test\"");
      tracker.keyToSubstitute = (key) -> "NewMessages." + key;
      tracker.getSubstitutions().get(1).setReplacementKey("TEST1");
      tracker.getSubstitutions().get(1).substitution = SubstitutionType.SUBSTITUTE;
      tracker.addedImport = "com.example.NewMessages";
      Assertions.assertEquals("\nimport com.example.NewMessages;\nimport com.example.Messages;\nif (\n\"hello\" while\nint n = NewMessages.TEST1\nprint(\"hi\")\n\"test\"", tracker.getTransformedFile());
   }

   @Test
   void testGetTransformedFileWithExistingImport()
   {
      JavaFileStringTracker tracker = new JavaFileStringTracker("\nimport com.example.Messages;\nif (\n\"hello\" while\nint n = \"go\"\nprint(\"hi\")\n\"test\"");
      tracker.getSubstitutions().get(1).setReplacementKey("TEST1");
      tracker.getSubstitutions().get(1).substitution = SubstitutionType.SUBSTITUTE;
      tracker.addedImport = "com.example.Messages";
      Assertions.assertEquals("\nimport com.example.Messages;\nif (\n\"hello\" while\nint n = Messages.TEST1\nprint(\"hi\")\n\"test\"", tracker.getTransformedFile());
   }

   @Test
   void testParseJavaFile()
   {
      JavaFileStringTracker tracker = new JavaFileStringTracker("package com.example.test;\nimport java.util.*;\nimport java.io.IOException;\nclass Hello {}");
      Assertions.assertEquals(26, tracker.importChecker.importDeclStart);
      Assertions.assertEquals(74, tracker.importChecker.typeDeclStart);
      Assertions.assertIterableEquals(Arrays.asList("java.util.*", "java.io.IOException"), tracker.importChecker.imports);
      
   }
}
