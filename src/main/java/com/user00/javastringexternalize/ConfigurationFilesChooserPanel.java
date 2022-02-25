package com.user00.javastringexternalize;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ConfigurationFilesChooserPanel extends JPanel
{
   String sourceFile;
   String propertiesFile;
   String javaMessageFile;
   String importedClass;
   String substitutionFormat;
   Runnable onSourceChange;
   
   ConfigurationFilesChooserPanel(String sourceFile, String substitutionFormat, String importedClass, String propertiesFile, String javaMessageFile)
   {
      this.sourceFile = sourceFile;
      this.propertiesFile = propertiesFile;
      this.javaMessageFile = javaMessageFile;
      this.importedClass = importedClass;
      this.substitutionFormat = substitutionFormat;
      
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      
      
      add(createFileLine("Properties File", propertiesFile, newFile -> this.propertiesFile = newFile));
      add(Box.createRigidArea(new Dimension(0, JavaStringExternalize.GUI_GAP)));
      add(createFileLine("Java Message File", javaMessageFile, newFile -> this.javaMessageFile = newFile));
      add(Box.createRigidArea(new Dimension(0, JavaStringExternalize.GUI_GAP)));
      add(createTextFieldOnlyLine("Imported Class", importedClass, newClass -> this.importedClass = newClass));
      add(Box.createRigidArea(new Dimension(0, JavaStringExternalize.GUI_GAP)));
      add(createTextFieldOnlyLine("Substitution Format", substitutionFormat, newFormat -> this.substitutionFormat = newFormat));
      add(Box.createRigidArea(new Dimension(0, JavaStringExternalize.GUI_GAP)));
      add(createFileLine("Source", sourceFile, newFile -> {
         this.sourceFile = newFile;
         if (onSourceChange != null)
            onSourceChange.run();
      }));
   }

   static JPanel createTextFieldOnlyLine(String label, String value, Consumer<String> onChange)
   {
      JPanel line = new JPanel();
      line.setLayout(new BorderLayout(JavaStringExternalize.GUI_GAP, 0));
      line.add(new JLabel(label + ": "), BorderLayout.LINE_START);
      JTextField fileTextField = new JTextField(value);
      fileTextField.getDocument().addDocumentListener(new DocumentListener() {
         @Override public void removeUpdate(DocumentEvent e) { onChange.accept(fileTextField.getText()); }
         @Override public void insertUpdate(DocumentEvent e) { onChange.accept(fileTextField.getText()); }
         @Override public void changedUpdate(DocumentEvent e) { onChange.accept(fileTextField.getText()); }
      });
      line.add(fileTextField, BorderLayout.CENTER);
      return line;
   }

   static JPanel createFileLine(String label, String value, Consumer<String> onChange)
   {
      return createFileLine(label, value, true, null, onChange);
   }

   static JPanel createFileLine(String label, String value, boolean isOpenFile, String extension, Consumer<String> onChange)
   {
      JPanel line = new JPanel();
      line.setLayout(new BorderLayout(JavaStringExternalize.GUI_GAP, 0));
      line.add(new JLabel(label + ": "), BorderLayout.LINE_START);
      JTextField fileTextField = new JTextField(value, 20);
      fileTextField.getDocument().addDocumentListener(new DocumentListener() {
         @Override public void removeUpdate(DocumentEvent e) { onChange.accept(fileTextField.getText()); }
         @Override public void insertUpdate(DocumentEvent e) { onChange.accept(fileTextField.getText()); }
         @Override public void changedUpdate(DocumentEvent e) { onChange.accept(fileTextField.getText()); }
      });
//      fileTextField.addActionListener(e -> onChange.accept(fileTextField.getText()));
      line.add(fileTextField, BorderLayout.CENTER);
      JButton fileButton = new JButton("\uD83D\uDCC1"); 
      line.add(fileButton, BorderLayout.LINE_END);
      fileButton.addActionListener((evt) -> {
         JFileChooser fc = new JFileChooser(fileTextField.getText());
         if (extension != null)
         {
            FileFilter filter = new FileNameExtensionFilter("*." + extension, extension);
            fc.addChoosableFileFilter(filter);
            fc.setFileFilter(filter);
         }
         int result;
         if (isOpenFile)
            result = fc.showOpenDialog(fileButton);
         else
            result = fc.showSaveDialog(fileButton);
         if (result == JFileChooser.APPROVE_OPTION)
         {
            try {
               String path = fc.getSelectedFile().getCanonicalPath();
               fileTextField.setText(path);
               onChange.accept(path);
            }
            catch (IOException e1)
            {
               e1.printStackTrace();
            }
         }
      });
      return line;
   }
   
   
}
