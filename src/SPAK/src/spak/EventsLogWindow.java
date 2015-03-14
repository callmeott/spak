package spak;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * <p>Title: SPAK</p>
 * <p>Description: Software Platform for Agent and Knowledge Management</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: National Institute of Informatics</p>
 * @author not attributable
 * @version 1.0
 */

public class EventsLogWindow extends JFrame {
  JTextPane logpanel = new JTextPane();
  KnowledgePanel kp = null;
  public EventsLogWindow(KnowledgePanel _kp) throws HeadlessException {
    kp = _kp;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    update();
    logpanel.addMouseListener(new EventsLogWindow_jTextPane1_mouseAdapter(this));
    this.getContentPane().add(logpanel, BorderLayout.CENTER);
    logpanel.addFocusListener(new EventsLogWindow_logpanel_focusAdapter(this));
  }

  public void update() {
    logpanel.setText(kp.getWorkingMemory().getEventsLog());
  }
  void jTextPane1_mouseClicked(MouseEvent e) {
    update();
  }

  void logpanel_focusGained(FocusEvent e) {
    update();
  }

}

class EventsLogWindow_jTextPane1_mouseAdapter extends java.awt.event.MouseAdapter {
  EventsLogWindow adaptee;

  EventsLogWindow_jTextPane1_mouseAdapter(EventsLogWindow adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseClicked(MouseEvent e) {
    adaptee.jTextPane1_mouseClicked(e);
  }
}

class EventsLogWindow_logpanel_focusAdapter extends java.awt.event.FocusAdapter {
  EventsLogWindow adaptee;

  EventsLogWindow_logpanel_focusAdapter(EventsLogWindow adaptee) {
    this.adaptee = adaptee;
  }
  public void focusGained(FocusEvent e) {
    adaptee.logpanel_focusGained(e);
  }
}