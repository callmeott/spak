package spak;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class MessageBox extends JFrame {
  JLabel jLabel = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  JButton jButton1 = new JButton();
  String msg;

  public MessageBox(String m) {
    msg = m;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 4);
    setVisible(true);
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(300, 100));
    jLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel.setText(msg);
    this.getContentPane().setLayout(borderLayout1);
    jButton1.setText("Close");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButton1_actionPerformed(e);
      }
    });
    this.getContentPane().add(jLabel, BorderLayout.CENTER);
    this.getContentPane().add(jButton1, BorderLayout.SOUTH);
  }

  void jButton1_actionPerformed(ActionEvent e) {
    setVisible(false);
  }
}