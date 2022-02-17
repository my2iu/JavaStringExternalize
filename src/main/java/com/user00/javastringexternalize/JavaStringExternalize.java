package com.user00.javastringexternalize;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;

import javax.swing.BorderFactory;
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
      options.addOption("import", true, "Import to be added to source file");
      options.addOption("propertiesFile", true, "Properties file where translations are added");
      options.addOption("messageFile", true, "Java file where ");
      options.addOption("help", false, "Show command-line information");
      CommandLineParser argParser = new DefaultParser();
      try
      {
         CommandLine line = argParser.parse(options, args);
         if (line.hasOption("help"))
            printCommandLineHelp(options);
         if (!line.hasOption("src"))
         {
            throw new ParseException("No Java source file specified");
         }
         
         showStringSubstituter(line.getOptionValue("src"), line.getOptionValue("import"),
               line.getOptionValue("propertiesFile"), line.getOptionValue("messageFile"));
      }
      catch (ParseException e)
      {
         System.err.println(e.getMessage());
         printCommandLineHelp(options);
      }
   }
   
   static final int GUI_GAP = 5;
   
   static void showStringSubstituter(String file, String addedImport,
         String propertiesFile, String javaMessageFile) throws IOException
   {
      String fileContents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
      JavaFileStringTracker tracker = new JavaFileStringTracker(fileContents);
      tracker.addedImport = addedImport;
      
      JFrame frame = new JFrame();
      frame.setTitle("String Externalization");
      frame.setLayout(new BorderLayout(GUI_GAP, GUI_GAP));
      
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
      
      ConfigurationFilesChooserPanel topPanel = new ConfigurationFilesChooserPanel(file, propertiesFile, javaMessageFile);
      frame.add(topPanel, BorderLayout.PAGE_START);
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, GUI_GAP, 0));
      JButton applyButton = new JButton("Apply");
      applyButton.addActionListener(e -> {
         tracker.fillInKeySubstitutions(keyGenerator);
         String result = tracker.getTransformedFile();
         try {
            Files.writeString(Paths.get(file), result, StandardCharsets.UTF_8);
         }
         catch (IOException ex)
         {
            ex.printStackTrace();
         }
      });
      buttonPanel.add(applyButton);
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> {
         frame.dispose();
      });
      buttonPanel.add(cancelButton);
      frame.add(buttonPanel, BorderLayout.PAGE_END);
      
      frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(GUI_GAP, GUI_GAP, GUI_GAP, GUI_GAP));
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
