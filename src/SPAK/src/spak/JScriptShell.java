package spak;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.mozilla.javascript.*;
import java.io.*;


/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class JScriptShell extends JFrame {
  BorderLayout borderLayout1 = new BorderLayout();
  JTextField jTextFieldCommand = new JTextField();
  static  JTextArea jTextAreaConsole = new JTextArea();
  static JScrollPane jScrollPane1 = new JScrollPane(jTextAreaConsole);
  ScriptEngine se;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenuItem jMenuItemRunScript = new JMenuItem();
  JFileChooser jFileChooser1 = new JFileChooser();

  public JScriptShell(ScriptEngine myengine) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    se = myengine;

  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jTextFieldCommand.setText("");
    jTextFieldCommand.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jTextFieldCommand_actionPerformed(e);
      }
    });
    this.setSize(new Dimension(400, 300));
    this.setTitle("JavaScript Shell");
    jTextAreaConsole.setEditable(false);
    jTextAreaConsole.setText("Welcome to JavaScript shell\n");
    jMenuFile.setText("File");
    jMenuItemRunScript.setText("Run Script File");
    jMenuItemRunScript.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemRunScript_actionPerformed(e);
      }
    });
    this.getContentPane().add(jTextFieldCommand, BorderLayout.SOUTH);
    this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jMenuBar1.add(jMenuFile);
    jMenuFile.add(jMenuItemRunScript);
    this.setJMenuBar(jMenuBar1);
  }

  void jTextFieldCommand_actionPerformed(ActionEvent e) {
    String cmd = jTextFieldCommand.getText();
    p(">"+cmd+"\n");
    jTextFieldCommand.setText("");
    se.putCommand(cmd);  // Let SE's own Thread handle it
//  se.processOneLine(cmd);
  }

  Rectangle prect = new Rectangle(0,0,10,10);
  public void p(String s) {
    jTextAreaConsole.append(s);
    prect.setLocation(0, jTextAreaConsole.getHeight());
    jTextAreaConsole.scrollRectToVisible(prect);
    jTextAreaConsole.repaint();
  }

  void jMenuItemRunScript_actionPerformed(ActionEvent e) {
    if (JFileChooser.APPROVE_OPTION == jFileChooser1.showOpenDialog(this)) {
      // Display the name of the opened directory+file in the statusBar.
      String filename = jFileChooser1.getSelectedFile().getPath();
      p("File>"+filename+"\n");
//    se.processSource(filename);
      se.putSource(filename);
    }
  }

  public void setSE(ScriptEngine newse) {
    se=newse;
  }
}