package spak;

import java.awt.*;
import javax.swing.*;

/**
 * AboutFrame
 */

public class AboutFrame extends JFrame {
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel4 = new JLabel();

  /**
   * Class constructor specifying the version string.
   *
   * @param ver the program version number string
   */

  public AboutFrame(String ver) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    jLabel2.setText("Version "+ver);
  }

  private void jbInit() throws Exception {
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel1.setText("SPAK: Software Platform for Agents and Knowledge Management");
    jLabel1.setBounds(new Rectangle(22, 0, 393, 34));
    this.setSize(new Dimension(427, 126));
    this.getContentPane().setLayout(null);
    this.setTitle("About SPAK");
    jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel2.setBounds(new Rectangle(118, 67, 185, 27));
    jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel3.setText("Vuthichai Ampornaramveth, Pattara Kiatisevi, Haruki Ueno");
    jLabel3.setBounds(new Rectangle(22, 24, 383, 27));
    jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel4.setText("Copyright (c) 2002-2005 National Institute of Informatics (NII)");
    jLabel4.setBounds(new Rectangle(39, 44, 351, 28));
    this.getContentPane().add(jLabel3, null);
    this.getContentPane().add(jLabel4, null);
    this.getContentPane().add(jLabel1, null);
    this.getContentPane().add(jLabel2, null);
  }
}