package com.user00.javastringexternalize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.user00.javastringexternalize.JavaParser.ImportDeclarationContext;
import com.user00.javastringexternalize.JavaParser.TypeDeclarationContext;
import com.user00.javastringexternalize.StringSubstitution.SubstitutionType;

public class JavaFileStringTracker
{
   String fileContents;
   List<? extends Token> tokens;
   JavaParser.CompilationUnitContext parseTree;
   List<StringSubstitution> substitutions;
   ImportChecker importChecker;
   
   String addedImport;
   Function<String, String> keyToSubstitute = (key) -> "Messages." + key;
   
   public JavaFileStringTracker(String fileContents)
   {
      this.fileContents = fileContents;
      
      // Do a scan to get all the string literals
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
      
      // Also do a fuller parse
      lexer = new JavaLexer(CharStreams.fromString(fileContents));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      JavaParser parser = new JavaParser(tokens);
      parseTree = parser.compilationUnit();
      
      // With a full parse, we can get the imports, so that we can change them
      importChecker = new ImportChecker();
      ParseTreeWalker.DEFAULT.walk(importChecker, parseTree);
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
      // Figure out where to add new import statements
      int addedImportPosition = -1;
      if (importChecker.importDeclStart >= 0)
         addedImportPosition = importChecker.importDeclStart;
      else if (importChecker.typeDeclStart >= 0)
         addedImportPosition = importChecker.typeDeclStart;
      // Check if we have an import that needs to be added (i.e. we actually perform
      // a substitution and the import isn't already in the import list)
      String importToAdd = null;
      if (!importChecker.imports.contains(addedImport) && hasSubstitutions())
         importToAdd = addedImport;
      
      // Figure out all the substitutions that we need
      Map<Integer, StringSubstitution> subsLookup = new HashMap<>();
      for (StringSubstitution sub: substitutions)
            subsLookup.put(sub.getIndex(), sub);
      
      // Apply the substitutions
      String toReturn = "";
      for (int n = 0; n < tokens.size(); n++)
      {
         Token tok = tokens.get(n);
         // See if this is the position to add new imports (assume that
         // new imports will be added right at the start of a token)
         if (tok.getStartIndex() == addedImportPosition)
         {
            if (importToAdd != null)
               toReturn += "import " + importToAdd + ";\n";
         }
         
         // Substitute any text
         StringSubstitution sub = subsLookup.get(n);
         if (sub == null || sub.substitution != SubstitutionType.SUBSTITUTE)
         {
            toReturn += tok.getText();
         }
         else
         {
            toReturn += keyToSubstitute.apply(sub.replacementKey);
         }
      }
      return toReturn;
   }

   /**
    * Are there any strings being externalized in the file
    */
   boolean hasSubstitutions()
   {
      return getSubstitutions().stream().anyMatch(sub -> sub.substitution == SubstitutionType.SUBSTITUTE);
   }
   
   /**
    * Adds new externalized strings to the end of a properties file
    */
   public String transformPropertiesFile(String contents)
   {
      // File doesn't need to change
      if (!hasSubstitutions()) return contents;

      // Just tack the new externalized strings to the end of the file
      contents += "\n";

      // Go through each substitution and store any unique ones
      Set<String> keysSubstituted = new HashSet<String>();
      for (StringSubstitution sub: substitutions)
      {
         if (sub.substitution != SubstitutionType.SUBSTITUTE) continue;
         if (keysSubstituted.contains(sub.replacementKey)) continue;
         keysSubstituted.add(sub.replacementKey);
         String originalString = sub.token.getText();
         originalString = originalString.substring(1, originalString.length() - 1);
         contents += sub.replacementKey + " = " + originalString + "\n";
      }
      
      return contents;
   }
   
   /**
    * Adds new externalized strings to a special Java Message file that is
    * used to access the contents of the Properties file in a static way
    */
   public String transformJavaMessageFile(String contents)
   {
      // Find the last }, which is presumably the ending of the class, so we can
      // insert new members there
      JavaLexer lexer = new JavaLexer(CharStreams.fromString(contents));
      List<Token> tokens = new ArrayList<>(lexer.getAllTokens());
      Collections.reverse(tokens);
      Optional<Token> lastBrace = tokens.stream().filter(tok -> tok.getType() == JavaLexer.RBRACE).findFirst();
      int insertionPosition = contents.length();
      if (lastBrace.isPresent())
         insertionPosition = lastBrace.get().getStartIndex();

      // Insert the new accessors to the externalized strings
      String before = contents.substring(0, insertionPosition);
      String after = contents.substring(insertionPosition);
      String newKeys = "";
      final String INDENT = "  ";
      Set<String> keysSubstituted = new HashSet<String>();
      for (StringSubstitution sub: substitutions)
      {
         if (sub.substitution != SubstitutionType.SUBSTITUTE) continue;
         if (keysSubstituted.contains(sub.replacementKey)) continue;
         keysSubstituted.add(sub.replacementKey);
         newKeys += INDENT + "public String " + sub.replacementKey + ";\n";
      }

      return before + newKeys + after;
   }
   
   /**
    * Reads in all classes imported into Java source file and notes where
    * new imports should be added.
    */
   static class ImportChecker extends JavaParserBaseListener
   {
      List<String> imports = new ArrayList<>();
      int typeDeclStart = -1;
      int importDeclStart = -1;
      @Override public void enterImportDeclaration(ImportDeclarationContext ctx)
      {
         if (importDeclStart < 0)
            importDeclStart = ctx.getStart().getStartIndex();
         if (ctx.STATIC() != null) return;
         int nameIdx = ctx.children.indexOf(ctx.qualifiedName());
         String imported = "";
         for (ParseTree subTree: ctx.children.subList(nameIdx, ctx.children.size() - 1))
            imported += subTree.getText();
         imports.add(imported);
         super.enterImportDeclaration(ctx);
      }
      
      @Override public void enterTypeDeclaration(TypeDeclarationContext ctx)
      {
         if (typeDeclStart < 0)
            typeDeclStart = ctx.getStart().getStartIndex();
         super.enterTypeDeclaration(ctx);
      }
   }
}
