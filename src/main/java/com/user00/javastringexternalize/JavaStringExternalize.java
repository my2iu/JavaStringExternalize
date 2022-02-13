package com.user00.javastringexternalize;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JavaStringExternalize
{

   public static void main(String[] args) throws IOException
   {
      // Command-line options
      Options options = new Options();
      options.addOption("src", true, "Source file to scan for strings");
      options.addOption("help", false, "Show command-line information");
      CommandLineParser argParser = new DefaultParser();
      try
      {
         CommandLine line = argParser.parse(options, args);
         if (line.hasOption("help"))
            printCommandLineHelp(options);
         if (!line.hasOption("src"))
         {
            System.err.println("No Java source file specified");
            printCommandLineHelp(options);
         }
         
         showStringSubstituter(line.getOptionValue("src"));
      }
      catch (ParseException e)
      {
         System.err.println(e.getMessage());
         printCommandLineHelp(options);
      }
   }
   
   static void showStringSubstituter(String file) throws IOException
   {
      String fileContents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
      JavaFileStringTracker tracker = new JavaFileStringTracker(fileContents);
      
      JFrame frame = new JFrame();
      frame.setTitle("String Externalization");
      frame.setLayout(new BorderLayout());
      
      StringTrackerPanel trackerPanel = new StringTrackerPanel(tracker);
      BiFunction<String, Integer, String> keyGenerator = (str, strIdx) -> {
         return str.substring(1, str.length() - 1).codePoints().map(new IntUnaryOperator() {
            boolean isStart = true;
            @Override public int applyAsInt(int ch)
            {
               if (isStart)
               {
                  isStart = false;
                  return Character.isJavaIdentifierStart(ch) ? ch : '_';
               }
               return Character.isJavaIdentifierPart(ch) ? ch : '_';
            }
         }).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
      }; 
      trackerPanel.setKeyGenerator(keyGenerator);
      frame.add(trackerPanel, BorderLayout.CENTER);
      
      JPanel buttonPanel = new JPanel();
      JButton applyButton = new JButton("Apply");
      applyButton.addActionListener(e -> {
         tracker.fillInKeySubstitutions(keyGenerator);
         String result = tracker.getTransformedFile();
      });
      buttonPanel.add(applyButton);
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> {
         frame.dispose();
      });
      buttonPanel.add(cancelButton);
      frame.add(buttonPanel, BorderLayout.PAGE_END);
      
      frame.pack();
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
   }
   
   static void printCommandLineHelp(Options options)
   {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("JavaStringExternalize", options);
      
   }
}
