package spak;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import javax.swing.event.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class FrameSystemSlot extends JFrame implements ListSelectionListener{
  BorderLayout borderLayout1 = new BorderLayout();
  JList jList1 = null;
  KFrame root = null;

  public FrameSystemSlot(Vector names, KnowledgePanel kp) {
    root = kp.getRootNode();

    jList1 = new JList(names);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    jList1.addListSelectionListener(this);
    this.getContentPane().setLayout(borderLayout1);
    this.setTitle("All Slots");

    JScrollPane scrollPane = new JScrollPane(jList1);
    //    jList1.setPreferredScrollableViewportSize(new Dimension(100, 500));

    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    this.pack();
  }

  public void valueChanged(ListSelectionEvent e) {
    Object names [] = jList1.getSelectedValues();
    Vector v = new Vector();
    for(int i=0;i<names.length;i++) {
      // System.out.println((String)names[i]);
      v.add((String)names[i]);
    }
    root.activateSlots(v);
  }
}