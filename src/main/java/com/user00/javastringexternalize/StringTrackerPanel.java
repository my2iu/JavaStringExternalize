package com.user00.javastringexternalize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.function.BiFunction;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import com.user00.javastringexternalize.StringSubstitution.SubstitutionType;

public class StringTrackerPanel extends JPanel
{
   final int STRING_COLUMN = 0;
   final int EXTERNALIZE_COLUMN = 1;
   final int KEY_COLUMN = 2;

   BiFunction<String, Integer, String> defaultKeyGenerator = (str, idx) -> "value" + idx;

   JTable table;
   AbstractTableModel model;
   JavaFileStringTracker tracker;
   JTextArea contextTextArea;
   JScrollPane contextScroller;
   
   public StringTrackerPanel(JavaFileStringTracker stringTracker)
   {
      tracker = stringTracker; 
      model = modelForTracker();
      table = new JTable(model);
      configureTableColumnModel();
      setLayout(new BorderLayout(JavaStringExternalize.GUI_GAP, JavaStringExternalize.GUI_GAP));
      JScrollPane scrollPane = new JScrollPane(table);
      add(scrollPane, BorderLayout.CENTER);
      
      contextTextArea = new JTextArea("\n\n\n\n\n", 5, 20);
      contextTextArea.setEditable(false);
      contextTextArea.setText(tracker.fileContents);
      table.getSelectionModel().addListSelectionListener(e -> {
         try {
            int row = table.getSelectedRow();
            int pos = 0;
            if (row >= 0)
            {
               StringSubstitution sub = tracker.getSubstitutions().get(row); 
               pos = sub.getPositionInFile();
               contextTextArea.getHighlighter().removeAllHighlights();
               contextTextArea.getHighlighter().addHighlight(sub.getPositionInFile() + 1, sub.getPositionEndInFile(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
//               contextTextArea.setCaretPosition(sub.getPositionInFile() + 1);
//               contextTextArea.moveCaretPosition(sub.getPositionEndInFile());
            }
            Rectangle2D r = contextTextArea.modelToView2D(pos);
            int scrollPosX = (int)r.getCenterX() - contextScroller.getViewport().getExtentSize().width / 2; 
            int scrollPosY = (int)r.getCenterY() - contextScroller.getViewport().getExtentSize().height / 2;
            if (scrollPosX < 0) scrollPosX = 0;
            if (scrollPosY < 0) scrollPosY = 0;
            contextScroller.getViewport().setViewPosition(new Point(scrollPosX, scrollPosY));
         }
         catch (BadLocationException e1)
         {
            e1.printStackTrace();
         }
      });
      contextScroller = new JScrollPane(contextTextArea);
      add(contextScroller, BorderLayout.PAGE_END);
   }

   private void configureTableColumnModel()
   {
      table.getColumnModel().getColumn(EXTERNALIZE_COLUMN).setPreferredWidth(20);
      table.getColumnModel().getColumn(KEY_COLUMN).setCellRenderer(new DefaultTableCellRenderer() {
         Font italicFont = getFont().deriveFont(Font.ITALIC);
         @Override
         public Component getTableCellRendererComponent(JTable table,
               Object value, boolean isSelected, boolean hasFocus, int row,
               int column)
         {
            Component toReturn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                  row, column);
            StringSubstitution sub = tracker.getSubstitutions().get(row); 
            if (sub.substitution == SubstitutionType.SUBSTITUTE
                  && "".equals(sub.getReplacementKey()))
            {
               setText(defaultKeyGenerator.apply(sub.token.getText(), row));
               setForeground(Color.GRAY);
               setFont(italicFont);
            }
            else
            {
               setForeground(Color.BLACK);
               setFont(getFont());
            }
            return toReturn;
         }
         @Override protected void setValue(Object value)
         {
            super.setValue(value);
         }
      });
   }
   
   private AbstractTableModel modelForTracker()
   {
      return new AbstractTableModel() {
         @Override public int getRowCount() { return tracker.getSubstitutions().size(); }

         @Override public int getColumnCount() { return 3; }

         @Override
         public Class<?> getColumnClass(int columnIndex)
         {
            switch (columnIndex)
            {
            case EXTERNALIZE_COLUMN: return Boolean.class;
            case KEY_COLUMN: return String.class;
            default: return String.class;
            }
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
            // Redraw the whole table if anything changes, not just a single cel
            fireTableRowsUpdated(0, getRowCount());
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
   }
   
   void setTracker(JavaFileStringTracker newTracker)
   {
      tracker = newTracker;
      contextTextArea.setText(tracker.fileContents);
      table.setModel(modelForTracker());
      configureTableColumnModel();
   }
   
   void setKeyGenerator(BiFunction<String, Integer, String> keyGenerator)
   {
      defaultKeyGenerator = keyGenerator;
   }
}
