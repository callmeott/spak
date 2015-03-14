package spak;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class ZeroConsole extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();
  JScrollPane jScrollPane1 = new JScrollPane();
  JTextArea jTextArea1 = new JTextArea();
  JPopupMenu commandMenu;

  public ZeroConsole() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    // jTextArea1.setWrapStyleWord(true);  // Wrap only at whitespaces.
    jTextArea1.setLineWrap(true);
    this.setupCommandMenu();
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    jTextArea1.setEditable(false);
    jTextArea1.setText("");
    jTextArea1.addMouseListener(new ZeroConsole_this_mouseAdapter(this));
    add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTextArea1, null);
    this.setSize(new Dimension(320, 240));
  }

  Rectangle prect = new Rectangle(0,0,10,10);

  public void println(String s) {
    jTextArea1.append(s+"\n");
    prect.setLocation(0, jTextArea1.getHeight());
    jTextArea1.scrollRectToVisible(prect);
    jTextArea1.repaint();
    if(!isVisible())
      setVisible(true);
  }

  void this_mouseClicked(MouseEvent e) {
    // System.err.println("Mouse "+e);
    if (e.getButton() == 3) { // Right button (as tested on Windows)
      commandMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  private void setupCommandMenu() {
    commandMenu = new JPopupMenu("Console Commands");
    ActionListener al = new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        this_commandPerformed(e);
      }
    };
    commandMenu.add(makeMenuItem("Clear Console", al));
  }

  private JMenuItem makeMenuItem(String label, ActionListener al) {
    JMenuItem item = new JMenuItem(label);
    item.addActionListener(al);
    return item;
  }

  void this_commandPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("Clear Console")) {
      jTextArea1.setText("SPAK Console\n");
    }
  }
}

class ZeroConsole_this_mouseAdapter extends java.awt.event.MouseAdapter {
  ZeroConsole adaptee;

  ZeroConsole_this_mouseAdapter(ZeroConsole adaptee) {
    this.adaptee = adaptee;
  }

  public void mouseClicked(MouseEvent e) {
    adaptee.this_mouseClicked(e);
  }
}