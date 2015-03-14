package spak;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.event.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class FrameProperty extends JFrame {
  BorderLayout borderLayout1 = new BorderLayout();
  JTable jTable1 = null;
  JMenuBar myMenuBar = new JMenuBar();
  JMenu jMenu1 = new JMenu();
  JMenuItem jMenuItem1 = new JMenuItem();
  JMenu jMenu2 = new JMenu();
  JMenuItem jMenuItem2 = new JMenuItem();
  KFrame myFrame;
  SlotTableModel slModel;
  SlotList sl;
  JMenuItem jMenuItemDelete = new JMenuItem();

  public FrameProperty(String title, KFrame kf) {
    // sl = kf.getAllSlotList();
    sl = kf.getSlotList();
    myFrame = kf;
    /*
    Vector rowdata = new Vector();
    Vector column = new Vector();
    column.addElement("Slot");
    column.addElement("Value");
    for(int i=0;i<sl.size();i++)
      rowdata.addElement(sl.vectorAt(i));
    jTable1 = new JTable(rowdata, column);
    */
    slModel = new SlotTableModel(sl, myFrame);
    jTable1 = new JTable(slModel);

    TableColumn typeColumn = jTable1.getColumnModel().getColumn(1);
    JComboBox typeCombo = new JComboBox();
    for(int i=0;i<Slot.TYPE_COUNT;i++)
      typeCombo.addItem(Slot.type_names[i]);
    typeColumn.setCellEditor(new DefaultCellEditor(typeCombo));
    typeColumn.setMaxWidth(60);

    TableColumn condColumn = jTable1.getColumnModel().getColumn(3);
    JComboBox condCombo = new JComboBox();
    for(int i=0;i<Slot.COND_COUNT;i++)
      condCombo.addItem(Slot.cond_names[i]);
    condColumn.setCellEditor(new DefaultCellEditor(condCombo));
    condColumn.setPreferredWidth(40);

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    setTitle(title);

    // Fix columns widths
    TableColumn reqCol = jTable1.getColumnModel().getColumn(5);
    reqCol.setMaxWidth(25);
    TableColumn reqBCol = jTable1.getColumnModel().getColumn(6);
    reqBCol.setMaxWidth(25);
    TableColumn dfCol = jTable1.getColumnModel().getColumn(7);
    dfCol.setMaxWidth(25);
    TableColumn sharedCol = jTable1.getColumnModel().getColumn(8);
    sharedCol.setMaxWidth(25);
    TableColumn uniqCol = jTable1.getColumnModel().getColumn(9);
    uniqCol.setMaxWidth(25);
    TableColumn nameCol = jTable1.getColumnModel().getColumn(0);
    nameCol.setPreferredWidth(50); // Resizeable
    TableColumn argCol = jTable1.getColumnModel().getColumn(4);
    argCol.setPreferredWidth(50); // Resizeable
    // Value column
    jTable1.getColumnModel().getColumn(2).setPreferredWidth(200);
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(600, 400));
    this.getContentPane().setLayout(borderLayout1);
    JScrollPane scrollPane = new JScrollPane(jTable1);
    jTable1.setPreferredScrollableViewportSize(new Dimension(500, 70));
    jMenu1.setText("File");
    jMenu1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenu1_actionPerformed(e);
      }
    });
    jMenuItem1.setText("Close");
    jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem1_actionPerformed(e);
      }
    });
    jMenu2.setText("Edit");
    jMenuItem2.setText("Add");
    jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenuItemDelete.setText("Delete");
    jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemDelete_actionPerformed(e);
      }
    });
    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    myMenuBar.add(jMenu1);
    myMenuBar.add(jMenu2);
    jMenu1.add(jMenuItem1);
    jMenu2.add(jMenuItem2);
    jMenu2.add(jMenuItemDelete);
    this.setJMenuBar(myMenuBar);
  }

  void jMenu1_actionPerformed(ActionEvent e) {
  }

  /**
   * File/Close
   */
  void jMenuItem1_actionPerformed(ActionEvent e) {
    setVisible(false);
  }

  /**
   * Edit/Add
   */
  void jMenuItem2_actionPerformed(ActionEvent e) {
    sl.add(new Slot("NewSlot"+sl.size(),"",myFrame));
    slModel.fireTableDataChanged();
  }

  /**
   * Edit/Delete: Delete a Slot
   */
  void jMenuItemDelete_actionPerformed(ActionEvent e) {
    int slIndex = jTable1.getSelectedRow();
//  System.out.println("Delete: "+slIndex);
    if(slIndex>=0) {
      // We can only remove slots we own.
      if(sl.slotAt(slIndex).getOwner().equals(myFrame)) {
        sl.removeSlotAt(slIndex);
        slModel.fireTableDataChanged();
      }
    }
  }
}