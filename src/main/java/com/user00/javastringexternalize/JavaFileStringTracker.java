package com.user00.javastringexternalize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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
      if (!importChecker.imports.contains(addedImport)
            && getSubstitutions().stream().anyMatch(sub -> sub.substitution == SubstitutionType.SUBSTITUTE))
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
            toReturn += sub.replacementKey;
         }
      }
      return toReturn;
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
