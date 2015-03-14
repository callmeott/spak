package spak;

import java.awt.*;
import javax.swing.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class AskSlotValue extends JDialog {
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  private JLabel questionLabel = new JLabel();
  private JPanel answerPanel = new JPanel();
  private JPanel buttonPanel = new JPanel();
  private JButton buttonCancel = new JButton();
  private JButton buttonOk = new JButton();
  private GridLayout gridLayout1 = new GridLayout();
  private JTextField answerField = new JTextField();

  public AskSlotValue(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public AskSlotValue() {
    this(null, "", false);
  }
  private void jbInit() throws Exception {
    panel1.setLayout(borderLayout1);
    questionLabel.setToolTipText("");
    questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
    questionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    questionLabel.setText("Question is here...");
    buttonPanel.setMinimumSize(new Dimension(10, 27));
    buttonPanel.setPreferredSize(new Dimension(10, 27));
    buttonPanel.setLayout(gridLayout1);
    buttonCancel.setText("Cancel");
    buttonOk.setText("OK");
    answerField.setBounds(new Rectangle(86, 14, 238, 21));
    answerPanel.setLayout(null);
    this.setTitle("Slot Value");
    getContentPane().add(panel1);
    panel1.add(questionLabel, BorderLayout.NORTH);
    panel1.add(answerPanel, BorderLayout.CENTER);
    panel1.add(buttonPanel,  BorderLayout.SOUTH);
    buttonPanel.add(buttonOk, null);
    buttonPanel.add(buttonCancel, null);
    answerPanel.add(answerField, null);
  }
}