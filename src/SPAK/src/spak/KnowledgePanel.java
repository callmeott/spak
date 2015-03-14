package spak;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class KnowledgePanel extends JPanel {
  // For adding multiple-inheritance using left-mouse dragging
  public static KFrame dragSource = null;
  public static KFrame dropTarget = null;

  // For ClipBoard Manipulation
  public static KFrame frameClip = null;  // Clipboard for Copy/Paste

  // Class member vars
  KFrame rootnode = new KFrame("Root", null);
  Vector linkLines = null;

  // Default working memory
  WorkingMemory wm = new WorkingMemory(rootnode);

  public KnowledgePanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    redrawFrame();
  }

  public void paint(Graphics g) {
    super.paint(g);
    if(linkLines != null) {
      g.setColor(Color.red);
      for(int i=0;i<linkLines.size();i+=2) {
        Point p1 = (Point) linkLines.elementAt(i);
        Point p2 = (Point) linkLines.elementAt(i+1);
        g.drawLine(p1.x,p1.y,p2.x,p2.y);
//        System.out.println("Line "+p1.x+" "+p1.y+" "+p2.x+" "+p2.y);
      }
//      System.out.println("Line Count"+linkLines.size());
    }
  }

  public void setRootNode(KFrame rn) {
    rootnode=rn;
    if (wm != null) wm.stopEval();
    wm = new WorkingMemory(rootnode);
    String cmd = rn.getSlotValue("onLoad");
    if(cmd!=null) {
      System.out.println("Do onLoad: "+cmd);
      wm.putJSCommand(cmd);
    }
    redrawFrame();
  }

  /**
   * Re-layout all the frames in this Panel
   */

  public void redrawFrame() {
    layoutFrame(rootnode,10,10);
    repaint();
  }

  public KFrame getRootNode() {
    return rootnode;
  }

  /**
   * Return the current active WorkingMemory object for the current rootnode
   */

  public WorkingMemory getWorkingMemory() {
    return wm;
  }

  void jbInit() throws Exception {
    this.setLayout(null);
    this.setBackground(Color.white);
  }

  private void layoutFrame(KFrame frame, int x, int y) {
    removeAll();
    frame.resetLocation();
    frame.determineLocation(x,y);
    linkLines = frame.getLinkLines();
    frame.addSelfTo(this);
    // Reset size for proper scrolling
    setPreferredSize(new Dimension(frame.getFrameWidth()+20, frame.getFrameHeight()+20));
    revalidate();
  }

  public Vector getAllSlotNames() {
    SlotList allSlots = rootnode.collectAllSlotList();
    Vector slots = allSlots.getSlotVector();
    Vector nameList = new Vector();
    for(int i=0;i<slots.size();i++) {
      String name = ((Slot)slots.elementAt(i)).getName();
      // System.out.println(name);
      if(!nameList.contains(name))
        nameList.add(name);
    }
    return nameList;
  }

}