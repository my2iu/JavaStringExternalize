package com.user00.javastringexternalize;

import static com.user00.javastringexternalize.JavaStringExternalize.GUI_GAP;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Simple dialog for getting the parameters for different
 * conversions between different translation formats
 */
public class ConvertersDialog extends JDialog
{
   public String sourceName;
   public String destName;
   boolean applyClicked = false;
   public ConvertersDialog(Frame owner, String title, boolean modal, String sourceExtension, String targetExtension)
   {
      super(owner, title, modal);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      JPanel contentPane = new JPanel();
      setContentPane(contentPane);
      contentPane.setBorder(BorderFactory.createEmptyBorder(GUI_GAP, GUI_GAP, GUI_GAP, GUI_GAP));
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
      
      add(ConfigurationFilesChooserPanel.createFileLine(
            "Source", "", true, sourceExtension, (newName) -> {
         sourceName = newName;
      }));
      add(ConfigurationFilesChooserPanel.createFileLine(
            "Destination", "", false, targetExtension, (newName) -> {
         destName = newName;
      }));
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, GUI_GAP, 0));
      JButton applyButton = new JButton("Apply");
      applyButton.addActionListener(e -> {
         applyClicked = true;
         ConvertersDialog.this.dispose();
      });
      buttonPanel.add(applyButton);
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> {
         ConvertersDialog.this.dispose();
      });
      buttonPanel.add(cancelButton);
      add(buttonPanel, BorderLayout.PAGE_END);
      
      pack();
   }
}
