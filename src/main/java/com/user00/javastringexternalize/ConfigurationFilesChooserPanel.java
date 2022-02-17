package com.user00.javastringexternalize;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConfigurationFilesChooserPanel extends JPanel
{
   String sourceFile;
   String propertiesFile;
   String javaMessageFile;
   
   ConfigurationFilesChooserPanel(String sourceFile, String propertiesFile, String javaMessageFile)
   {
      this.sourceFile = sourceFile;
      this.propertiesFile = propertiesFile;
      this.javaMessageFile = javaMessageFile;
      
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      
      
      add(createFileLine("Properties File", propertiesFile, newFile -> this.propertiesFile = newFile));
      add(Box.createRigidArea(new Dimension(0, JavaStringExternalize.GUI_GAP)));
      add(createFileLine("Java Message File", javaMessageFile, newFile -> this.javaMessageFile = newFile));
      add(Box.createRigidArea(new Dimension(0, JavaStringExternalize.GUI_GAP)));
      add(createFileLine("Source", sourceFile, newFile -> this.sourceFile = newFile));
   }
   
   JPanel createFileLine(String label, String value, Consumer<String> onChange)
   {
      JPanel line = new JPanel();
      line.setLayout(new BorderLayout(JavaStringExternalize.GUI_GAP, 0));
      line.add(new JLabel(label + ": "), BorderLayout.LINE_START);
      JTextField fileTextField = new JTextField(value);
      fileTextField.addActionListener(e -> onChange.accept(fileTextField.getText()));
      line.add(fileTextField, BorderLayout.CENTER);
      JButton fileButton = new JButton("\uD83D\uDCC1"); 
      line.add(fileButton, BorderLayout.LINE_END);
      fileButton.addActionListener((evt) -> {
         JFileChooser fc = new JFileChooser(value);
         int result = fc.showOpenDialog(this);
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
