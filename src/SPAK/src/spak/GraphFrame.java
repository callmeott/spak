package spak;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

/**
 * <p>Title: GraphFrame</p>
 * <p>Description: Showing PNG image</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: NII</p>
 * @author Vuthichai Ampornaramveth
 * @version 1.0
 */

public class GraphFrame
    extends JFrame {
  JPanel jPanelButtons = new JPanel();
  JLabel jLabelImage = new JLabel();
  JScrollPane jScrollPaneMap = new JScrollPane(jLabelImage);
  BorderLayout borderLayout1 = new BorderLayout();
  JButton jButtonExport = new JButton();
  BufferedImage bi = null;

  private JFileChooser jFileChooser1 = new
      JFileChooser(System.getProperty("user.dir"));

  public void setImage(String imgfile) {
    updateImage(imgfile);
  }

  public GraphFrame() {
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    jPanelButtons.setMaximumSize(new Dimension(32767, 30));
    jPanelButtons.setMinimumSize(new Dimension(10, 30));
    jPanelButtons.setPreferredSize(new Dimension(10, 30));
    jPanelButtons.setLayout(null);
    this.getContentPane().setLayout(borderLayout1);
    jButtonExport.setBounds(new Rectangle(5, 3, 113, 26));
    jButtonExport.setText("Save as PNG");
    jButtonExport.addActionListener(new GraphFrame_jButtonExport_actionAdapter(this));
    this.setTitle("Knowledge Hierarchy");
    this.getContentPane().add(jPanelButtons, BorderLayout.NORTH);
    jPanelButtons.add(jButtonExport, null);
    this.getContentPane().add(jScrollPaneMap, BorderLayout.CENTER);
    this.setSize(new Dimension(600, 420));
  }

  void jButtonExport_actionPerformed(ActionEvent e) {
    if (bi != null) {
      try {
        if (JFileChooser.APPROVE_OPTION == jFileChooser1.showSaveDialog(this)) {
          String filename = jFileChooser1.getSelectedFile().getPath();
          javax.imageio.ImageIO.write(bi, "PNG",
                                      new FileOutputStream(filename));
        }
      }
      catch (IOException ex) {
        JOptionPane.showConfirmDialog(this, "File Write Error: " + ex);
      }
    }
  }

  private void updateImage(String filename) {
    try {
      bi = javax.imageio.ImageIO.read(new File(filename));
      jLabelImage.setIcon(new ImageIcon(bi));
    }
    catch (IOException ex) {
    }
  }

  class GraphFrame_jButtonExport_actionAdapter
      implements java.awt.event.ActionListener {
    GraphFrame adaptee;

    GraphFrame_jButtonExport_actionAdapter(GraphFrame adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
      adaptee.jButtonExport_actionPerformed(e);
    }
  }
}