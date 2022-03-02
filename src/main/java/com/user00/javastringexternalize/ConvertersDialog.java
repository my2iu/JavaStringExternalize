package com.user00.javastringexternalize;

import static com.user00.javastringexternalize.JavaStringExternalize.GUI_GAP;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

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
   String [] fileNames;
   boolean applyClicked = false;
   String lastFile = "";
   private ConvertersDialog(Frame owner, String title, boolean modal, String [] fileLabels, String [] fileNames, String[] extensions)
   {
      super(owner, title, modal);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      JPanel contentPane = new JPanel();
      setContentPane(contentPane);
      contentPane.setBorder(BorderFactory.createEmptyBorder(GUI_GAP, GUI_GAP, GUI_GAP, GUI_GAP));
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
      
      for (int n = 0; n < fileNames.length; n++)
      {
         final int idx = n;
         add(ConfigurationFilesChooserPanel.createFileLine(
               fileLabels[idx], "", true, extensions[idx], (newName) -> {
            fileNames[idx] = newName;
            lastFile = newName;
         }, () -> lastFile));
      }
      
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
   
   public static ConvertersDialog forSourceDest(Frame owner, String title, boolean modal, String sourceExtension, String targetExtension)
   {
      return new ConvertersDialog(owner, title, modal,
            new String[] {"Source", "Destination"},
            new String[2],
            new String[] {sourceExtension, targetExtension});
   }

   public static ConvertersDialog forMultipleFiles(Frame owner, String title, boolean modal, String [] fileLabels, String [] fileNames, String[] extensions)
   {
      return new ConvertersDialog(owner, title, modal, fileLabels, fileNames, extensions);
   }

   
   public String getSourceName() { return fileNames[0]; }

   public String getDestName() { return fileNames[1]; }
   
   public String[] getFileNames() { return fileNames; }
}
