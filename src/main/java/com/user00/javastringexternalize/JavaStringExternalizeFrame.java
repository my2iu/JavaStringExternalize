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

import static com.user00.javastringexternalize.JavaStringExternalize.GUI_GAP;

public class JavaStringExternalizeFrame extends JFrame
{
   private static JavaFileStringTracker trackerForPath(String file) throws IOException
   {
      String fileContents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
      JavaFileStringTracker tracker = new JavaFileStringTracker(fileContents);
      tracker.keyToSubstitute = (key) -> "Messages.m." + key;
      return tracker;
   }

   JavaFileStringTracker tracker;

   JavaStringExternalizeFrame(String javaFile, String addedImport,
         String propertiesFile, String javaMessageFile)
   {
      String sourceFile = javaFile;
      try {
         if (sourceFile == null) sourceFile = "";
         tracker = trackerForPath(sourceFile);
      }
      catch (IOException e1)
      {
         tracker = new JavaFileStringTracker("");
      }
      
      JFrame frame = this;
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
      
      ConfigurationFilesChooserPanel topPanel = new ConfigurationFilesChooserPanel(sourceFile, addedImport, propertiesFile, javaMessageFile);
      topPanel.onSourceChange = () -> {
         String f = topPanel.sourceFile;
         try {
            tracker = trackerForPath(f);
            trackerPanel.setTracker(tracker);
         } 
         catch (IOException e)
         {
            trackerPanel.setTracker(new JavaFileStringTracker(""));
         }
      };
      frame.add(topPanel, BorderLayout.PAGE_START);
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, GUI_GAP, 0));
      JButton applyButton = new JButton("Apply");
      applyButton.addActionListener(e -> {
         tracker.fillInKeySubstitutions(keyGenerator);
         String result = tracker.getTransformedFile(topPanel.importedClass);
         try {
            Files.writeString(Paths.get(topPanel.sourceFile), result, StandardCharsets.UTF_8);
            
            if (topPanel.propertiesFile != null && !topPanel.propertiesFile.isEmpty())
            {
               String props = Files.readString(Paths.get(topPanel.propertiesFile), StandardCharsets.UTF_8);
               Files.writeString(Paths.get(topPanel.propertiesFile), tracker.transformPropertiesFile(props), StandardCharsets.UTF_8);
            }
            
            if (topPanel.javaMessageFile != null && !topPanel.javaMessageFile.isEmpty())
            {
               String msgClass = Files.readString(Paths.get(topPanel.javaMessageFile), StandardCharsets.UTF_8);
               Files.writeString(Paths.get(topPanel.javaMessageFile), tracker.transformJavaMessageFile(msgClass), StandardCharsets.UTF_8);
            }

            trackerPanel.setTracker(new JavaFileStringTracker(""));
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
   }


}
