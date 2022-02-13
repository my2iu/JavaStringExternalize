package com.user00.javastringexternalize;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.user00.javastringexternalize.StringSubstitution.SubstitutionType;

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
      final int STRING_COLUMN = 0;
      final int EXTERNALIZE_COLUMN = 1;
      final int KEY_COLUMN = 2;
      String fileContents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
      JavaFileStringTracker tracker = new JavaFileStringTracker(fileContents);
      
      JFrame frame = new JFrame();
      frame.setTitle("String Externalization");
      
      AbstractTableModel model = new AbstractTableModel() {
         @Override public int getRowCount() { return tracker.getSubstitutions().size(); }

         @Override public int getColumnCount() { return 3; }

         @Override
         public Class<?> getColumnClass(int columnIndex)
         {
            if (columnIndex == EXTERNALIZE_COLUMN)
               return Boolean.class;
            return String.class;
         }
         
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex)
         {
            if (columnIndex == EXTERNALIZE_COLUMN
                  || columnIndex == KEY_COLUMN)
               return true;
            return super.isCellEditable(rowIndex, columnIndex);
         }
         
         @Override
         public Object getValueAt(int rowIndex, int columnIndex)
         {
            if (columnIndex == STRING_COLUMN)
               return tracker.getSubstitutions().get(rowIndex).token.getText();
            else if (columnIndex == EXTERNALIZE_COLUMN)
               return tracker.getSubstitutions().get(rowIndex).substitution == SubstitutionType.SUBSTITUTE;
            else if (columnIndex == KEY_COLUMN)
               return tracker.getSubstitutions().get(rowIndex).getReplacementKey();
            return "";
         }
         
         @Override
         public void setValueAt(Object aValue, int rowIndex, int columnIndex)
         {
            if (columnIndex == EXTERNALIZE_COLUMN)
            {
               if (((Boolean)aValue).booleanValue())
                  tracker.getSubstitutions().get(rowIndex).substitution = SubstitutionType.SUBSTITUTE;
               else
                  tracker.getSubstitutions().get(rowIndex).substitution = SubstitutionType.SKIP;
            }
            else if (columnIndex == KEY_COLUMN)
            {
               tracker.getSubstitutions().get(rowIndex).setReplacementKey((String)aValue);
            }
            super.setValueAt(aValue, rowIndex, columnIndex);
         }
         
         @Override public String getColumnName(int column)
         {
            switch (column)
            {
            case 0: return "String";
            case EXTERNALIZE_COLUMN: return "Externalize";
            case KEY_COLUMN: return "Key";
            default: return "";
            }
         }
      };
      JTable table = new JTable(model);
      table.getColumnModel().getColumn(EXTERNALIZE_COLUMN).setPreferredWidth(20);
      frame.setLayout(new BorderLayout());
      JScrollPane scrollPane = new JScrollPane(table);
      frame.add(scrollPane, BorderLayout.CENTER);
      
      JTextArea contextText = new JTextArea("\n\n\n\n\n");
      contextText.setEditable(false);
      table.getSelectionModel().addListSelectionListener(e -> {
         int row = table.getSelectedRow();
         String context;
         if (row < 0)
            context = "";
         else
            context = tracker.getSubstitutions().get(row).getSurroundingContext();
         contextText.setText(context);
      });
      JScrollPane contextScroller = new JScrollPane(contextText);
      frame.add(contextScroller, BorderLayout.PAGE_END);

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
