package spak;
import javax.swing.JButton;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import java.util.*;
import javax.swing.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.font.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class KFrame extends JButton implements MouseListener {
  public Vector children = new Vector();
  public Vector inactivechildren = new Vector();
  public Vector depender = new Vector();
  public Vector parents = new Vector();
  private String name = null;
  public Vector oldnames = new Vector();
  public Vector oldparents = new Vector();
  static int idcount = 0; // keep the ID of all frames

  int x=0, y=0, height=0, width=0;
  int framedepth=200;
  int frameheight=30;
  int buttonwidth=170;
  private KFrame rootFrame = null;
  private WorkingMemory wm = null;
  private Vector myLines = null;
  private SlotList mySlotList = new SlotList();
  private boolean isInstance = false;
  private boolean isActive = true;
  JPopupMenu commandMenu;

  private KnowledgePanel inKP = null;
  private KFrameScript myKFrameScript = null;
  String paramHash = null;
  boolean isDummy = false;
  int instanceCount = 0;  // Number of existing instances beloging to this Frame
  int inused = 0; // This is used only for an Instance. Indicating how many times
                  // it has been used by any other instance ?
  long createtime, lastupdate;
  private boolean showChildren = true; // Whether to display subframes

  /**
   * Frames and instances in SPAK are represented by this KFrame object
   * @param str name of the frame to be created
   * @param root root of the knowledge hierarchy
   */
  public KFrame(String str, KFrame root) {
    try {
      jbInit(str);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    name = str;
    rootFrame = root;
    if(root==null) {
      rootFrame = this;
    }
    idcount++;
    System.out.println("Creating frame "+name+" with ID "+idcount);
    mySlotList.add(new Slot("Name", str, this));
    mySlotList.add(new Slot("_ID", String.valueOf(idcount), this));
    mySlotList.add(new Slot("_ISA", null, this, Slot.TYPE_LIST, Slot.COND_ANY, ""));
    mySlotList.add(new Slot("_WASA", null, this, Slot.TYPE_LIST, Slot.COND_ANY, ""));

    Date d = new Date();
    createtime = d.getTime();
    lastupdate = createtime;

    setupCommandMenu();
  }

  public KFrameScript getKFrameScript() {
    if(myKFrameScript == null) {
      myKFrameScript = new KFrameScript(this);
    }
    return myKFrameScript;
  }

  private void setupCommandMenu() {
    commandMenu = new JPopupMenu("Frame Commands");
    ActionListener al = new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        this_commandPerformed(e);
      }
    };
    commandMenu.add(makeMenuItem("Add Child", al));
    commandMenu.add(makeMenuItem("Show/Hide Children", al));
    commandMenu.add(makeMenuItem("Create Instance", al));
    commandMenu.add(makeMenuItem("Try Instance", al));
    commandMenu.add(makeMenuItem("Copy", al));
    commandMenu.add(makeMenuItem("Cut", al));
    commandMenu.add(makeMenuItem("Paste", al));
    commandMenu.add(makeMenuItem("Delete", al));
    commandMenu.add(makeMenuItem("Show All Slots", al));
    commandMenu.add(makeMenuItem("Show All Parents", al));
    commandMenu.add(makeMenuItem("reInduce me", al));
    commandMenu.add(makeMenuItem("Export XML", al));
    commandMenu.add(makeMenuItem("Import XML", al));
    commandMenu.add(makeMenuItem("Write DOT", al));
    commandMenu.add(makeMenuItem("Write DOT BW", al));
    commandMenu.add(makeMenuItem("Write DOT (no slots)", al));
    commandMenu.add(makeMenuItem("Write DOT BW (no slots)", al));
    commandMenu.add(makeMenuItem("Draw Diagram", al));
    commandMenu.add(makeMenuItem("Draw Diagram (no slots)", al));
  }

  /**
   * Ask all of my parents to remove me from their children list
   */

  void parentsRemove() {
    Enumeration en = parents.elements();
    while(en.hasMoreElements())
      ((KFrame)en.nextElement()).remove(this);
  }

  /**
   * Ask all children to remove me from their parent list
   */

  void childrenRemove() {
    Enumeration en = children.elements();
    while(en.hasMoreElements())
      ((KFrame)en.nextElement()).doRemoveParentFrame(this);
  }

  public boolean hasParentFrame() {
    return (parents.size()>0);
  }

  /**
   * Define if this frame is just a DUMMY.
   * Dummy Frame is used as a placeholder for HashTable/Vector
   * which expects a Frame instance, but we want to put an empty
   * (or no) frame there.
   */

  public void setDummy(boolean d) {
    isDummy = d;
  }

  public boolean isDummy() {
    return isDummy;
  }

  /**
   * Callback routines for Frame Popup menus
   */

  void this_commandPerformed(ActionEvent e) {
    String cmd = e.getActionCommand(  );
    if (cmd.equals("Add Child")) {
      if(isInstance) {
        new MessageBox("An Instance cannot have a Child");
      }
      else {
        createFrame("NewFrame");
      }
    }
    else if (cmd.equals("Show/Hide Children")) {
      this.setChildVisible(!showChildren);
      this.redrawPanel();
    }
    else if (cmd.equals("Create Instance")) {
      if(isInstance) {
        new MessageBox("An Instance cannot have another Instance");
      }
      else {
        wm.putJSCommand("Root.findFrame(\""+name+"\").createInstance().updateHash()");
      }
    }
    else if (cmd.equals("Try Instance")) {
      BackwardChaining bc = new BackwardChaining(this.wm);
      bc.setZeroConsole(wm.getZeroConsole());
      bc.backward(this.getName());
      ForwardChaining ic = new ForwardChaining(this.wm);
      ic.setZeroConsole(wm.getZeroConsole());
      ic.induce();

      redrawPanel();
    }
    else if (cmd.equals("Copy")) {
      KnowledgePanel.frameClip = this;
    }
    else if (cmd.equals("Cut")) {
      if(parents.size()>0) { //Cannot cut the Root frame
        KnowledgePanel.frameClip = this;
        // parentFrame.remove(this);
        parentsRemove();
        for (int i=0;i<parents.size();i++)
          removeParent(((KFrame) parents.elementAt(i)));
        redrawPanel();
      }
    }
    else if (cmd.equals("Paste")) {
      if(KnowledgePanel.frameClip!=null) { // Sth is in Clipboard
        if(!KnowledgePanel.frameClip.hasParentFrame()) { // Move
          this.add(KnowledgePanel.frameClip);
//          KnowledgePanel.frameClip = null;
          redrawPanel();
        }
        else { // Copy
          this.addCopyOf(KnowledgePanel.frameClip);
        }
      }
    }
    else if (cmd.equals("Delete")) {
      selfDelete();
    }
    else if (cmd.equals("Show All Slots")) {
      StringBuffer allslot = new StringBuffer();
      Hashtable allslots = this.getAllSlotFullnames();
      Enumeration en = allslots.keys();
      while(en.hasMoreElements()) {
        allslot.append((String)allslots.get(en.nextElement()));
        allslot.append("\n");
      }
      JOptionPane.showMessageDialog(null,allslot.toString(),
                                    "All Slots for "+this.getName(),
                                    JOptionPane.PLAIN_MESSAGE);
    }
    else if (cmd.equals("Show All Parents")) {
      StringBuffer output = new StringBuffer();
      Vector allparentsnames = getAllParentsNamesWithDepth();
      if ( allparentsnames == null ) {
        output.append("null");
      } else {
        for (int i=0;i<allparentsnames.size();i++) {
          output.append(" - ");
          output.append( ((Vector) allparentsnames.elementAt(i)).elementAt(0));
          output.append(" (");
          output.append( ((Vector) allparentsnames.elementAt(i)).elementAt(1));
          output.append(")\n");
        }
      }
      JOptionPane.showMessageDialog(null,output.toString(),
                                    "All Parents for "+this.getName(),
                                    JOptionPane.PLAIN_MESSAGE);
    }
    else if (cmd.equals("reInduce me")) {
      System.out.println("Reinducing " + getName());
      ForwardChaining ic = new ForwardChaining(this.wm);
      ic.reInduceFrame(this);
      redrawPanel();
    }
    else if (cmd.equals("Export XML")) {
      try {
        SPAKFileFilter filter = new SPAKFileFilter("SPAK XML Knowledge File: *.xml");
        filter.addExtension("xml");
        ApplicationFrame.jFileChooser1.setFileFilter(filter);
        if (JFileChooser.APPROVE_OPTION == ApplicationFrame.jFileChooser1.showSaveDialog(this)) {
           String filename = ApplicationFrame.jFileChooser1.getSelectedFile().getPath();
           writeXMLFile(filename);
        }
      }
      catch(Exception ex) {}
    }
    else if (cmd.equals("Import XML")) {
      SPAKFileFilter filter = new SPAKFileFilter("SPAK XML Knowledge File: *.xml");
      filter.addExtension("xml");
      ApplicationFrame.jFileChooser1.setFileFilter(filter);
      if (JFileChooser.APPROVE_OPTION == ApplicationFrame.jFileChooser1.showOpenDialog(this)) {
        // Display the name of the opened directory+file in the statusBar.
        String filename = ApplicationFrame.jFileChooser1.getSelectedFile().getPath();
        XMLReader zr = new XMLReader();
        zr.parseFile(filename);
        KFrame loaded = zr.getRootNode();
        if(loaded!=null) {
          add(loaded);
          redrawPanel();
        }
      }
    }
    else if (cmd.equals("Write DOT")) {
      do_writeDot(true,true);
    }
    else if (cmd.equals("Write DOT BW")) {
      do_writeDot(false,true);
    }
    else if (cmd.equals("Write DOT (no slots)")) {
      do_writeDot(true,false);
    }
    else if (cmd.equals("Write DOT BW (no slots)")) {
      do_writeDot(false,false);
    }
    else if (cmd.equals("Draw Diagram")) {
      do_Diagram(true);
    }
    else if (cmd.equals("Draw Diagram (no slots)")) {
      do_Diagram(false);
    }
  }

  private void do_writeDot(boolean isColor, boolean drawSlot) {
    try {
      SPAKFileFilter filter = new SPAKFileFilter("Graphviz DOT File: *.dot");
      filter.addExtension("dot");
      ApplicationFrame.jFileChooser1.setFileFilter(filter);
      if (JFileChooser.APPROVE_OPTION == ApplicationFrame.jFileChooser1.showSaveDialog(this)) {
         String filename = ApplicationFrame.jFileChooser1.getSelectedFile().getPath();
         FileOutputStream fo = new FileOutputStream(filename);
         PrintStream ps = new PrintStream(fo);
         ps.println("digraph prof {");
         ps.println("size=\"10,8\";");
         ps.println("rotate=90;");
//           ps.println("node [fontsize=12];");
//           ps.println("ratio = fill;");
//           ps.println("node [style=filled];");
         writeDOT(ps,null,isColor,drawSlot);
         ps.println("}");
         ps.close();
         fo.close();
      }
    }
    catch(Exception ex) {}
  }

  private void do_Diagram(boolean drawSlot) {
    try {
      File tmpDotFile = File.createTempFile("spak", ".dot");
      File tmpPngFile = File.createTempFile("spak", ".png");
      FileOutputStream fo = new FileOutputStream(tmpDotFile);
      PrintStream ps = new PrintStream(fo);
      ps.println("digraph prof {");
      // Maximum size allowed. If this is too small, dot will shrink the
      // graph to fit this area, and characters may be unreadable.
      ps.println("size=\"100,100\";");

      // Comment out the followings if you wish.
//      ps.println("rotate=90;");
//      ps.println("node [fontsize=12];");
//      ps.println("ratio = fill;");
//      ps.println("node [style=filled];");
      writeDOT(ps, null, true, drawSlot);
      ps.println("}");
      ps.close();
      fo.close();
      String [] cmds = { "dot", "-Tpng", tmpDotFile.getAbsolutePath(), "-o",
          tmpPngFile.getAbsolutePath() };
      Process p = Runtime.getRuntime().exec(cmds);
      try {
        p.waitFor();
        // System.err.println("Out "+tmpPngFile.getAbsolutePath());

        if(tmpPngFile.exists()) {
          GraphFrame gf = new GraphFrame();
          gf.setImage(tmpPngFile.getAbsolutePath());

          //Center the window
          Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
          Dimension frameSize = gf.getSize();
          if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
          }
          if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
          }
          gf.setLocation( (screenSize.width - frameSize.width) / 2,
                         (screenSize.height - frameSize.height) / 2);
          gf.setVisible(true);
        }
        else {
          JOptionPane.showMessageDialog(null,
              "dot did not create a PNG file !");
        }
      }
      catch (InterruptedException ex1) {
        JOptionPane.showMessageDialog(null, "Error running dot: "+ex1);
      }
      tmpDotFile.delete();
      tmpPngFile.delete();
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(null, "Have you installed GraphViz ?\n\nError rendering graph:\n"+ex);
    }
  }

  public void redrawPanel() {
    if(inKP!=null)
      inKP.redrawFrame();
  }


  /**
   * Remove this frame/instance from working memory.
   */

  public void selfDelete() {
    runSpecialSlot("onDie");
    // me becomes inactive
    isActive = false;
    wm.removeInstance(this);
    redrawPanel();
  }
  private JMenuItem makeMenuItem(String label, ActionListener al) {
   JMenuItem item = new JMenuItem(label);
   item.addActionListener(al);
   return item;
 }
  public void addSlot(String name, String val) {
    mySlotList.add(new Slot(name,val,this));
  }

  public void addSlot(String name, String val, int type, int cond, String ar) {
    mySlotList.add(new Slot(name,val,this,type,cond,ar));
  }

  /**
   * This method is provided for backward compatibility with earlier .JS file.
   * Note that default value for UNIQUE is "true", the default value for BO is false,
   * and the default value for dontfill is false
   */
  public void addSlot(String name, String val, int type, int cond, String ar,
         boolean req, boolean shared) {
    mySlotList.add(new Slot(name,val,this,type,cond,ar,req,false,false,shared,true));
  }

  /**
   * for backward compatible, the unique (U) slot flag is always set to true.
   */
  public void addSlot(String name, String val, int type, int cond, String ar,
         boolean req, boolean reqb, boolean df, boolean shared) {
    mySlotList.add(new Slot(name,val,this,type,cond,ar,req,reqb,df,shared,true));
  }

  /**
   * Add a slot to the frame
   * @param name slot name
   * @param val  slot value
   * @param type slot type
   * @param cond slot condition
   * @param ar   condition argument
   * @param req  the required (R) flag
   * @param reqb the beginning only (BO) flag
   * @param df   the don't fill (DF) flag
   * @param shared  the shared (S) flag
   * @param uni  the unique (U) flag
   */
  public void addSlot(String name, String val, int type, int cond, String ar,
         boolean req, boolean reqb, boolean df, boolean shared, boolean uni) {
    mySlotList.add(new Slot(name,val,this,type,cond,ar,req,reqb,df,shared,uni));
  }

  /**
   * Set to "true" if this frame is an Instance.
   * "false" if it's a Class.
   */

  public void setInstance(boolean ins) {
    isInstance = ins;
    setForeground(isInstance?Color.red:Color.black);
  }

  public boolean isInstance() {
    return isInstance;
  }

  public boolean isActive() {
    return isActive;
  }

  private void jbInit(String str) throws Exception {
    this.setText(str);
    this.setBackground(Color.lightGray);
    this.addMouseListener(this);
    this.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        this_actionPerformed(e);
      }
    });
  }

  /**
   * Add a frame to be a child of this frame (will ask for a remove
   * if it is already a child)
   *
   * @param child the child frame
   */
  public void add(KFrame child) {
    if(inChildList(child)) {
      if(JOptionPane.showConfirmDialog(
          null,child.getName()+" is already a child of "+getName()+
                                    ". Do you want to remove it ?",
          "Remove Confirmation", JOptionPane.YES_NO_OPTION)==
          JOptionPane.YES_OPTION) {
         // remove this parent and let reinduce do the rest
        this.remove(child);
        child.removeParent(this);
        this.redrawPanel();
      }
    }
    else if(inParentList(child)) {
      JOptionPane.showMessageDialog(null,child.getName()+" is a parent of "+getName(),
                                    "Error Loop Detected...",
                                    JOptionPane.ERROR_MESSAGE);
    }
    else {
      children.addElement(child);
      child.setRootFrame(rootFrame);
      child.setWorkingMemory(wm);
      child.addParentFrame(this); // need wm, so let's do it after above
    }
  }

  /**
   * Remove a child from this frame.
   *
   * @param child the child to be removed
   */

  public void remove(KFrame child) {
    if(children.contains(child)) {
      children.removeElement(child);

      // add to inactivechildren list
      if ( ! inactivechildren.contains(child)) {
           inactivechildren.addElement(child);
      }
      // it still should have its parent
      //child.removeParentFrame(this);

      // The child may have multiple parents, this below stuff will be
      // handled by reInduce
      /*
      child.setRootFrame(child);    // This child becomes root of its own
      child.setWorkingMemory(null); // No WM attached to this child
      */
    }
  }

  public void addParentFrame(KFrame p) {
    if(!parents.contains(p)) {
      parents.addElement(p);
      try {
        addSlotValue("_ISA", p.getName());
        //System.out.println("Frame: "+getName()+ ": added "+p.getName()+" to _ISA slot!");
      } catch (Exception e) {
        System.err.println("Error in frame: "+getName()+ " error adding "+p.getName()+" to _ISA slot!");
      }
    }
  }

  /**
   * If X is in this frame's depender list, it means X depend
   * on this frame. Or this frame instance is a member of X slots.
   */
  public void addToDependerList(KFrame p) {
    if(!depender.contains(p)) {
      depender.addElement(p);
    }
  }

  /**
   *  Being in this frame's depender list means I depend on this frame
   */
  public Vector getDependerList() {
    return(depender);
  }

  /**
   *  Being in this frame's depender list means I depend on this frame
   */
  public Vector getDependerNameList() {
    Vector output = new Vector();
    for (int i=0;i< depender.size();i++) {
      output.add( ( (KFrame) depender.elementAt(i)).getName());
    }
    return output;
  }


  /**
   *  Update who I depend on
   */
  public void updateDependency() {
    for(int i=0;i<mySlotList.size();i++) {
      Slot sl = mySlotList.slotAt(i);
      String slotName = sl.getName();
      if(sl.getType()==Slot.TYPE_INSTANCE) {
        String slotvalue = sl.getValue();
        //System.err.println("Slot value: "+slotvalue);
        if ( ! slotvalue.equals("")) {
          KFrame fr = rootFrame.findFrame(sl.getValue());
          if (fr != null) {
            if (fr.isInstance) {
              fr.addToDependerList(this);
            }
            else {
              System.err.println("Error: instance " + slotvalue +
                                 " is not an instance!");
            }
          }
          else {
            //System.err.println("Slot value: "+slotvalue + " not set!");
          }
        }
      }
    }
  }


  public void removeParent(KFrame p) {
    if(parents.contains(p)) {
      if (! oldparents.contains(p)) {
        oldparents.add(p);
        try {
          addSlotValue("_WASA", p.getName());
        } catch (Exception e) {
          System.err.println("Exception when adding slot value: "+e);
        }
      }
      removeSlotValue("_ISA", p.getName());
      doRemoveParentFrame(p);
      p.remove(this);
    }
  }

  private void doRemoveParentFrame(KFrame p) {
    if(parents.contains(p)) {
      parents.removeElement(p);
    }
  }

  /**
   * Propagate the new RootFrame to all subframes
   */

  public void setRootFrame(KFrame rf) {
    rootFrame = rf;
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.setRootFrame(rf);
    }
  }

  /**
   * Cut me from being the children of the current parent, save my old name
   * and set my parent to be the newparent as well as inform the new parent
   * about me
   *
   * @param newparent
   */

  public void setToNewParent(KFrame newparent) {
    System.out.println("Entering setToNewParent("+newparent.getName()+")");
    if (! isInstance() ) {
      System.err.println("Error: setToNewParent can be called only if this is instance");
      return;
    }

    //System.err.println("My paramHash: "+getParamHash());
    //System.err.println("wm.getInstanceHash: "+ wm.getInstanceHash().toString());

    // remove me from WM hash
    Hashtable instanceHash=wm.getInstanceHash();
    if (instanceHash.containsValue(this))
        instanceHash.remove(getParamHash());
    else
      System.out.println("Strange, not found meself in the WM hash, maybe I was deleted already");

     // save my current name and parent
    if (! oldnames.contains(name) ) {
      oldnames.add(name);
    }

    // do add
    addParentFrame(newparent);
    newparent.add(this);
    updateHash();
  }

  /**
   * Propagate the new WorkingMemory to all subframes
   */
  public void setWorkingMemory(WorkingMemory newwm) {
    wm = newwm;
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.setWorkingMemory(newwm);
    }
  }

  /**
   * See if item is one of my children, sub-subsub children, ...
   */

  boolean inChildList(KFrame frame) {
    if(children.contains(frame))
      return true;
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      if(item.inChildList(frame))
        return true;
    }
    return false;
  }

  /**
   * See if item is one of my parents, grand parents, ...
   */

  public boolean inParentList(KFrame frame) {
    if(parents.contains(frame))
      return true;
    for(int i=0;i<parents.size();i++) {
      KFrame item = ((KFrame)parents.elementAt(i));
     if(item.inParentList(frame))
        return true;
    }
    return false;
  }

  /**
   * Open Frame Property Window (Slot List) when button is pressed
   */

  void this_actionPerformed(ActionEvent e) {
    FrameProperty fp = new FrameProperty("Property of "+name, this);

    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = fp.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    fp.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    fp.setVisible(true);
  }

  public int getFrameWidth() {
    return width;
  }

  public int getFrameHeight() {
    return height;
  }

  public int getFrameY() {
    return y;
  }

  public int getFrameX() {
    return x;
  }

  /**
   * Assign self co-ordinate x,y and dimension width,height
   */
  public void resetLocation() {
    x=0;
    y=0;
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.resetLocation();
    }
  }

  /**
   * Check if the location/size has been assigned
   */

  public boolean knownLocation() {
    return (x>0) || (y>0);
  }

  /**
   * Assign self co-ordinate x,y and dimension width,height
   */
  public void determineLocation(int bound_x, int bound_y) {
    if(knownLocation())
      return;
    myLines = new Vector();
    name = getSlotValue("Name");
    setText(name);
    // Calculate text (label) width
    TextLayout tl = new TextLayout(this.getText(), this.getFont(),
                                   new FontRenderContext(null, false, false));
    Rectangle2D r2d = tl.getBounds();
    int textwidth = (int)r2d.getWidth();
    buttonwidth = textwidth+40;
    framedepth = buttonwidth+30;
    /**
     * Check if we have any of our own Children....
     */
    if(showChildren) {
      int newchildcount = 0;
      for (int i = 0; i < children.size(); i++) {
        KFrame item = ( (KFrame) children.elementAt(i));
        if (!item.knownLocation())
          newchildcount++;
      }
      if (newchildcount == 0) {
        x = bound_x;
        y = bound_y;
        height = frameheight;
        width = framedepth;
      }
      else {
        int current_y = bound_y;
        int sum_y = 0;
        int maxchildwidth = 0;
        for (int i = 0; i < children.size(); i++) {
          KFrame item = ( (KFrame) children.elementAt(i));
          if (!item.knownLocation()) {
            item.determineLocation(bound_x + framedepth, current_y);
            current_y += item.getFrameHeight();
            int cwidth = item.getFrameWidth();
            if (cwidth > maxchildwidth)
              maxchildwidth = cwidth;
            int itemY = item.getFrameY();
            sum_y += itemY;
          }
        }
        x = bound_x;
        y = sum_y / newchildcount;
        height = current_y - bound_y;
        width = framedepth + maxchildwidth;
      }
      Point p = new Point(x + buttonwidth, y + 12);
      for (int i = 0; i < children.size(); i++) {
        KFrame item = ( (KFrame) children.elementAt(i));
        myLines.addElement(p);
        myLines.addElement(new Point(item.getFrameX(), item.getFrameY() + 12));
      }
    } // showChildren
    else { // hide Children
      x = bound_x;
      y = bound_y;
      height = frameheight;
      width = framedepth;
      if(children.size()>0) {
        Point p = new Point(x + buttonwidth, y + 12);
        Point q = new Point(x + buttonwidth +10, y + 12);
        myLines.addElement(p);
        myLines.addElement(q);
      }
    }
    setBounds(new Rectangle(x, y, buttonwidth, 25));
  }
  public void setChildVisible(boolean childvis) {
    this.showChildren = childvis;
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.setChildVisible(childvis);
      item.setVisible(childvis);
    }
  }
  public Vector getLinkLines() {
    if(showChildren) {
      for (int i = 0; i < children.size(); i++)
        myLines.addAll( ( (KFrame) children.elementAt(i)).getLinkLines());
    }
    return myLines;
  }

  /**
   * Add this frame JButton to the given JPanel
   */

  public void addSelfTo(KnowledgePanel jp) {
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.addSelfTo(jp);
    }
    jp.add(this,null);
    inKP = jp;
  }

  private SlotList doGetAllSlotList() {
    SlotList ret = new SlotList();
    ret.add(mySlotList);
    return ret;

  }

  /**
   * Get the list of all slots of mine and all my anchestors
   * Nearer parents have higher priority
   * @return SlotList
   */

  public SlotList getAllSlotList() {
    if(!hasParentFrame())
      return mySlotList;
    else {
      SlotList ret = new SlotList();

      // collect parents' slot first
      Vector allparents = getAllParentsFrames();
      //for (int i = 0; i <allparents.size(); i++) {
      // this is a bit awkward. As the later-added slot will override
      // the previusly-added slots so we need to add the farthest parents first
      for(int i=allparents.size()-1;i>=0;i--){
        ret.add( ( (KFrame) allparents.elementAt(i)).doGetAllSlotList());
      }

      // then include my slots
      ret.add(doGetAllSlotList());
      return ret;
    }
  }

  // Collect all self and children's slotlist DOWN
  public SlotList collectAllSlotList() {
    int count = children.size();
    if(count>0) {
      SlotList childList = new SlotList();
      for(int i=0;i<count;i++)
        childList.add(((KFrame)children.elementAt(i)).collectAllSlotList());
      childList.add(mySlotList);
      return childList;
    }
    return mySlotList;
  }

  public SlotList getSlotList() {
    return mySlotList;
  }

  public Vector getParentFrames() {
    return parents;
  }

  public KFrame getParentFrame() {
    if ( parents.size() > 0 ) {
      return (KFrame) parents.elementAt(0);
    } else {
      return null;
    }
  }

  public String getParentName() {
    if ( parents.size() > 0 ) {
      return ((KFrame) parents.elementAt(0)).getSlotValue("Name");
    } else {
      return new String("");
    }
  }

  /**
   * Get all upper parents'name until Root, sorted according to the depth
   *
   * return Vector of String containing the name of each parent
   */

  public Vector getAllParentsNames() {
    Vector tmp = getAllParentsNamesWithDepth();
    Vector retval = new Vector();
    for(int i=0;i<tmp.size();i++){
      retval.add( ((Vector) tmp.elementAt(i)).elementAt(0));
    }
    return retval;
  }

  /**
   * Get all upper parent frames until Root, sorted according to the depth
   *
   * return Vector of parent frames
   */

  public Vector getAllParentsFrames() {
    Vector tmp = getAllParentsFramesWithDepth();
    Vector retval = new Vector();
    if (tmp == null)
      return null;
    for(int i=0;i<tmp.size();i++){
      retval.add(((Vector) tmp.elementAt(i)).elementAt(0));
    }
    return retval;
  }

  /**
   * Get all upper parents until Root
   *
   * return Vector of Vector(String containing the name of each parent,
   * depth from the current frame)
   */

  public Vector getAllParentsNamesWithDepth() {
    Vector retval = new Vector();
    Vector allparents = getAllParentsFramesWithDepth();
    if (allparents == null ) {
      return null;
    } else {
      for (int i=0;i<allparents.size();i++) {
        Vector tmp = new Vector();
        tmp.add( ((KFrame) ((Vector) allparents.elementAt(i)).elementAt(0)).getSlotValue("Name"));
        tmp.add( ((Vector) allparents.elementAt(i)).elementAt(1));
        retval.add(tmp);
      }
    }
    return retval;
  }

  public Vector getAllParentsFramesWithDepth() {
    Vector retval = getAllParentsFramesWithDepth(1);
    if ( retval != null ) {
      // sort according to the depth
      DepthComp m = new DepthComp();
      Collections.sort(retval, m);
      return retval;
    }
    return null;
  }

  private Vector getAllParentsFramesWithDepth(int depth) {
    if(!hasParentFrame()) {  // I'm root Frame
      return null;
    }
    else {
      Vector upper = new Vector();
      for(int i=0;i<parents.size();i++) {
        // first add meself
        Vector tmp = new Vector();
        tmp.add(((KFrame) parents.elementAt(i)));
        tmp.add(new Integer(depth));
        upper.add(tmp);

        Vector subupper = ((KFrame)parents.elementAt(i)).getAllParentsFramesWithDepth(depth+1);

        if (subupper != null ) {
          for (int j=0;j<subupper.size();j++) {
            boolean found = false;
            for (int k=0; k<upper.size();k++) {
              if (  ((Vector) upper.elementAt(k)).elementAt(0) ==
                  ((Vector) subupper.elementAt(j)).elementAt(0) ) {
                found = true;
                break;
              }
            }
            if (! found) upper.add(subupper.elementAt(j));
          }
        }
      }
      return upper;
    }
  }

  public Vector getAllChildren() {
    Vector mychildren = new Vector();
    mychildren.addAll(children);
    for (int i=0;i<children.size();i++) {
      mychildren.addAll( ((KFrame) mychildren.elementAt(i)).getAllChildren());
    }
    return mychildren;
  }

  public Vector getChildren() {
    return children;
  }

  public String getName() {
    return name;
  }

  /**
   * Highlight this Frame if it contains slots whose name show up
   * in the argument "slnames".
   */

  public void activateSlots(Vector slnames) {
    int matchCount = mySlotList.countSlotMatch(slnames);
    if(matchCount > 0) {
      if(matchCount >= mySlotList.size()-1)
        this.setBackground(Color.red);
      else
        this.setBackground(Color.yellow);
    }
    else
      this.setBackground(Color.lightGray);
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.activateSlots(slnames);
    }
  }

  public void mouseClicked(MouseEvent e) {
    if(e.getButton()==3) { // Right button (as tested on Windows)
      commandMenu.show(e.getComponent(), e.getX(), e.getY(  ));
    }
  }

  public void mousePressed(MouseEvent e) {
    if(e.getButton()==1) { // Left Button
//      System.out.println("Mouse Pressed at "+this.getName());
      KnowledgePanel.dragSource=this;
      KnowledgePanel.dropTarget=null;
    }
  }

  public void mouseReleased(MouseEvent e) {
    if(KnowledgePanel.dragSource != null &&
       KnowledgePanel.dropTarget != null &&
       KnowledgePanel.dragSource != KnowledgePanel.dropTarget) {
//      System.out.println("Mouse Released at "+this.getName());
      KnowledgePanel.dragSource.add(KnowledgePanel.dropTarget);
      redrawPanel();
    }
    KnowledgePanel.dragSource=null;
    KnowledgePanel.dropTarget=null;
  }

  public void mouseEntered(MouseEvent e) {
    if(KnowledgePanel.dragSource!=null) {
//      System.out.println("Mouse Entered at "+this.getName());
      KnowledgePanel.dropTarget=this;
    }
  }

  public void mouseExited(MouseEvent e) {
    KnowledgePanel.dropTarget=null;
  }

  // All Methods below are intended for JavaScript

  /**
   * Find a KFrame whose name match the given string
   */

  public KFrame findFrame(String fname) {
    if(name.equals(fname) || oldnames.contains(fname)) {
      return this;
    }
    else {
      Vector allchildren = new Vector();
      allchildren.addAll(children);
      allchildren.addAll(inactivechildren);
      for(int i=0;i<allchildren.size();i++) {
        KFrame item = ((KFrame)allchildren.elementAt(i));
        KFrame result = item.findFrame(fname);
        if(result!=null)
          return result;
      }
      return null;
    }
  }

  /**
   * Similar to findFrame but return the KFrameScript object instead
   * @param fname frame name
   * @return KFrameScript object representing the specified frame
   */
  public KFrameScript findFrameScript(String fname) {
    KFrame tmp = findFrame(fname);
    if (tmp != null)
      return new KFrameScript(tmp);
    else
      return null;
  }
  /**
   * Find a KFrame whose name matches the given string, starting from root
   */

  public KFrame rootFindFrame(String fname) {
    if(rootFrame != null)
      return rootFrame.findFrame(fname);
    else
      return findFrame(fname);
  }

  /**
   * Collect all Instance nodes starting from this node, down the hierachy tree.
   */

  public Vector Obsolete_collectInstances() {
    Vector retValue = new Vector();
    for(int i=0;i<children.size();i++) {
      KFrame node = (KFrame)children.elementAt(i);
      if(node.isInstance())
        retValue.add(node);
      else
        retValue.addAll(node.Obsolete_collectInstances());
    }
    return retValue;
  }

  /**
   * Obtain a vector of instances belonging to the specified frame or to
   * any of its derived frames.
   *
   *
   * @param fname name of the frame
   * @return a Vector containing all matched instances
   */

  public Vector findInstancesOf(String fname) {
    Vector result = new Vector();
    findInstancesOf2(fname, new Vector(), result);

    AgeComp m = new AgeComp();
    Collections.sort(result, m);
    return result;
  }

  /**
   * Obtain the first instance of this frame.
   *
   *
   * @return an instance of this frame or null
   */
  public KFrame findFirstInstance() {
    Vector allchildren = getAllChildren();
    if (allchildren.size() != 0 ) {
      int i = 0;
      while (i < allchildren.size()) {
        KFrame item =(KFrame) allchildren.elementAt(0);
        if ( item.isInstance() ) {
          return item;
        } else {
          i++;
        }
      }
    }
    return null;
  }

  private void findInstancesOf2(String fname, Vector parents, Vector out) {
    if(isInstance() && parents.contains(fname) && ! out.contains(this))
      out.add(this);
    parents.add(name);
    Vector allchildren = getAllChildren();
    for(int i=0;i<allchildren.size();i++) {
      KFrame item = ((KFrame)allchildren.elementAt(i));
      item.findInstancesOf2(fname, parents, out);
    }
    parents.removeElement(name);
  }

  public Slot getSlot(String slname) {
    return mySlotList.getSlot(slname);
  }

  /**
   * Return the slot value, if I don't have this slot then try
   * to find it from my parents
   * @param slname Slot name
   * @return slot value
   */
  public String getSlotValueRecursive(String slname) {
    String value = mySlotList.getSlotValue(slname);
    if ( value != null)
      return value;
    else {
      Vector allSlotVec = getAllSlotList().getSlotVector();
      for(int i=0;i<allSlotVec.size();i++) {
        Slot sl = (Slot) allSlotVec.elementAt(i);
        if (sl.getName().equals(slname)) {
          value = sl.getValue();
          if (value != null)
            return value;
        }
      }
    }
    return null;
  }

  public String getSlotValue(String slname) {
    return mySlotList.getSlotValue(slname);
  }

  public Vector getSlotVValue(String slname) {
    return mySlotList.getSlotVValue(slname);
  }

  public String getSlotValue(String slname, long timestamp) {
    return mySlotList.getSlotValue(slname, timestamp);
  }

  public Vector getSlotVValue(String slname, long timestamp) {
    return mySlotList.getSlotVValue(slname, timestamp);
  }

  public String getPreviousSlotValue(String slname) {
    return mySlotList.getPreviousSlotValue(slname, -1);
  }

  public String getPreviousSlotValue(String slname, int idx) {
    return mySlotList.getPreviousSlotValue(slname, idx);
  }

  public Vector getPreviousSlotVValue(String slname, int idx) {
    return mySlotList.getPreviousSlotVValue(slname, idx);
  }

  public Vector getPreviousSlotVValue(String slname) {
    return mySlotList.getPreviousSlotVValue(slname, -1);
  }

  public boolean slotValueContains(String slname, String match) {
    return mySlotList.slotValueContains(slname, match);
  }

  public WorkingMemory getWorkingMemory() {
    return wm;
  }

  /**
   * Get slot value from this frame and all the upper frames.
   * For example: this is used for collecting all "condition" slots.
   */

  public Vector getSlotValues(String slname) {
    if(!hasParentFrame()) {  // I'm root Frame
      Vector retval = new Vector();
      String slval = getSlotValue(slname);
      if(slval!=null)
        retval.add(slval);
      return retval;
    }
    else {
      Vector upper = new Vector();
      for(int i=0;i<parents.size();i++) {
        Vector subupper = ((KFrame)parents.elementAt(i)).getSlotValues(slname);
        upper.addAll(subupper);
      }
      String slval = getSlotValue(slname);

      if(slval!=null)
        upper.add(slval);
      return upper;
    }
  }

  public void setSlotValue(String slname, String value)
      throws InvalidSlotValueException {
    boolean doupdate = true;
    if ( ! isInstance || slname.indexOf("_") == 0 || slname.equals("onUpdate") )
      doupdate = false;
    setSlotValue(slname, value, doupdate);
  }

  /**
   * Set the value of a slot. This value must not break the
   * hierachical conditions applied to this slot. Set doupdate
   * to true if you want the doupdate function to be called
   * after the value is set.
   *
   * UPDATES: Ott 2005/3/3. Allows the value to break the
   * hierarchical conditions, assuming that the reInduce must
   * be done afterwards to fix the frame
   */

  public void setSlotValue(String slname, String value, boolean doupdate) {
    try {
      mySlotList.setSlotValue(slname, value);
    } catch (InvalidSlotValueException e) {
      System.err.println(e+" while adding slot "+slname+" to "+ value);
    }

    Date d = new Date();
    lastupdate = d.getTime();

    // this changing of slot might affect the condition calculation
    if (this.wm != null) {
      ForwardChaining ic = new ForwardChaining(this.wm);
      ic.removeFromConditionFailedHash(getName());
    }

    updateHash();

    // Name change must reflect on the button label
    // and slotname
    if (slname.equals("Name")) {
      name = value;
      setText(value);
    }
    if (doupdate) {
      doUpdate(false);
    }

    if (hasParentFrame()) { // Check all parents' conditions
      for (int i = 0; i < parents.size(); i++) {
        try {
          ( (KFrame) parents.elementAt(i)).checkSlotConditions(slname, value);
        } catch (InvalidSlotValueException e) {
          System.err.println(e+" while setting slot "+slname+" to "+ value);
        }
      }
    }
  }

  public void setSlotValue(String slname, Vector vvalue) {
    boolean doupdate = true;
    if ( slname.indexOf("_") == 0 )
      doupdate = false;
      setSlotVValue(slname, vvalue, doupdate);
  }

  public void addSlotValue(String slname, String value) {
    boolean doupdate = true;
    if ( slname.indexOf("_") == 0 )
      doupdate = false;
    addSlotValue(slname, value, doupdate);
  }

  public void setSlotVValue(String slname, Vector vvalue, boolean doupdate) {
    //if(hasParentFrame()) { // Check all parents' conditions
    //for(int i=0;i<parents.size();i++)
    //((KFrame)parents.elementAt(i)).checkSlotConditions(slname, value);
    try {
      mySlotList.setSlotVValue(slname, vvalue);
    } catch (InvalidSlotValueException e) {
      System.err.println(e+" while setting slot "+slname+" to "+ vvalue.toString());
    }

    Date d = new Date();
    lastupdate = d.getTime();
    if (doupdate) {doUpdate(false); }

    // this changing of slot might affect the condition calculation
    if (this.wm != null) {
      ForwardChaining ic = new ForwardChaining(this.wm);
      ic.removeFromConditionFailedHash(getName());
    }
    updateHash();
  }

  public void addSlotValue(String slname, String value, boolean doupdate) {
    if(hasParentFrame()) { // Check all parents' conditions
      for(int i=0;i<parents.size();i++) {
        try {
          ( (KFrame) parents.elementAt(i)).checkSlotConditions(slname, value);
        } catch (InvalidSlotValueException e) {
          System.err.println(e+" while adding to slot "+slname+" the value "+ value);
        }
      }
    }
    try {
      mySlotList.addSlotValue(slname, value);
    } catch (InvalidSlotValueException e) {
      System.err.println(e+" while adding slot "+slname+" to "+ value);
    }

    if (doupdate) {doUpdate(false); }

    // this changing of slot might affect the condition calculation
    if (this.wm != null) {
      ForwardChaining ic = new ForwardChaining(this.wm);
      ic.removeFromConditionFailedHash(getName());
    }
    updateHash();
  }

  public void removeSlotValue(String slname, String value) {
    boolean doupdate = true;
    if ( slname.indexOf("_") == 0 )
      doupdate = false;
    removeSlotValue(slname, value, doupdate);
  }

  public void removeSlotValue(String slname, String value, boolean doupdate) {
    mySlotList.removeSlotValue(slname, value);
    if (doupdate) {doUpdate(false); }

    // this changing of slot might affect the condition calculation
    if (this.wm != null) {
      ForwardChaining ic = new ForwardChaining(this.wm);
      ic.removeFromConditionFailedHash(getName());
    }
    updateHash();
  }

  /**
   * Check if the given value satisfies slot all the
   * hierachical slot conditions.
   */

  public void checkSlotConditions(String slname, String value)
          throws InvalidSlotValueException {
    if(!mySlotList.checkSlotValue(slname,value))
      throw new InvalidSlotValueException(slname,"in-"+name);
    if(hasParentFrame()) {
      for(int i=0;i<parents.size();i++)
        ((KFrame)parents.elementAt(i)).checkSlotConditions(slname, value);
    }
  }

  /* Check the slots of the given frame fr if they are valid
   * for this frame
   *
   */
  public boolean checkSlotCondition(KFrame fr) {
    // Copy Slots from Ancestors
    Vector allSlotVec = getAllSlotList().getSlotVector();
    for(int i=0;i<allSlotVec.size();i++) {
      Slot sl = (Slot)allSlotVec.elementAt(i);
      try {
        checkSlotConditions(sl.getName(),fr.getSlotValue(sl.getName()));
      } catch ( InvalidSlotValueException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * Obtain a vector of all hierarchical conditions ("C" prefix)
   * and onTry ("T" prefix).
   */

  public Vector getConditionAndOnTry() {
    Vector retval=new Vector();
    for(int i=0;i<parents.size();i++) {
      retval.addAll(((KFrame)parents.elementAt(i)).getConditionAndOnTry());
    }

    String slval = getSlotValue("onTry");
    if(slval!=null) {
      retval.add("T");
      retval.add(slval);
    }

    slval = getSlotValue("condition");
    if(slval!=null) {
      retval.add("C");
      retval.add(slval);
    }

    return retval;
  }

  /**
   * Obtain vector of special slot contents all up the hierarchy
   * @param slotname onInstantiate, onUpdate, onEvaluate, onDie
   * @param staringnode get the slot contents starting from startingnode
   */
  public Vector getSpecialSlot(String slotname) {
    return getSpecialSlot(slotname, this);
  }

  public Vector getSpecialSlot(String slotname, KFrame startingnode) {
    Vector v = new Vector();
    Vector parents = startingnode.getAllParentsFrames();
    if ( parents != null) {
      for (int k = 0; k < parents.size(); k++) {
        String value = ( (KFrame) parents.elementAt(k)).getSlotValue(slotname);
        if (value != null && !value.equals(""))
          v.add(value);
      }
    }
    if ( getSlotValue(slotname) != null) v.add(getSlotValue(slotname));
    return v;
  }

  public KFrame createInstance() {
    return createInstance(true, true);
  }

  public String getNewInstanceName() {
    instanceCount++;
    return name+"_"+instanceCount;
  }

  public KFrame createInstance(boolean dojscript, boolean redraw) {
    KFrame child = new KFrame(getNewInstanceName(), rootFrame);
    child.setInstance(true);
    add(child);

    // Copy Slots from Ancestors
    Vector allSlotVec = getAllSlotList().getSlotVector();
    for(int i=0;i<allSlotVec.size();i++) {
      Slot sl = (Slot) allSlotVec.elementAt(i);
      String slname = sl.getName();
      // skip special slots
      // (sl.getName().indexOf("on") == 0 &&
      if (!slname.equals("Name") &&
          slname.indexOf("_") != 0 &&
          !slname.matches("^on[A-Z].*") &&
          !slname.equals("condition") &&
          !slname.equals("directcreate") ) {
        if (sl.getType() == Slot.TYPE_INSTANCE) {
          child.addSlot(sl.getName(), sl.getValue(), sl.getType(),
                        sl.getCondition(), sl.getArgument(), sl.getRequired(),
                        sl.getRequiredAtBeginningOnly(), sl.getDontFill(), sl.getShared());
        }
        else {
          child.addSlot(sl.getName(), sl.getValue(), sl.getType(),
                        sl.COND_ANY, null, false, false, false, false);
        }
      }
    }

   // should run after everything is complete?
   if(dojscript) {  // Must be true only when called from ScriptEngine Thread.
     runSpecialSlot("onInstantiate");
   }

   String childParamHash = child.getParamHash_AllSlots();
   child.setParamHash(childParamHash);

   if(redraw)
     redrawPanel();
   return child;
 }

 /**
   * Update my hash in Working Memory
   * @param org
   * @return
   */

  public void updateHash() {
    //String myParamHash = getParamHash_AllSlots();
    //setParamHash(myParamHash);
    //wm.getInstanceHash().put(paramHash, this);
    updateHash(getParamHash_AllSlots());
  }

  /**
   * Update my hash in Working Memory
   * @param newhash manually specify the new param hash
   * @return
   */

  public void updateHash(String newhash) {
    if(wm == null) {
      System.out.println(getName()+": wm is null, not update the hash");
      return;
    }
    if(isDummy) {
      return; // no update for dummy
    }
    Hashtable instanceHash = wm.getInstanceHash();
    // remove old hash first
    removeHash();

    instanceHash.put(newhash, this);
    paramHash = newhash;
    //System.out.println(getName()+": hash update to "+paramHash);
  }

  /**
   * Remove myself from wm's instanceSlot
   */
  public void removeHash() {
    Hashtable instanceHash = wm.getInstanceHash();
    Enumeration en = instanceHash.keys();
    while(en.hasMoreElements()) {
      String key = (String) en.nextElement();
      if (instanceHash.get(key) == this) {
        instanceHash.remove(key);
      }
    }
  }

  /**
   * Add a copy of Org as a child of this KFrame
   */

  public KFrame addCopyOf(KFrame org) {
    KFrame child = new KFrame(org.getName()+"_copy", rootFrame);
    add(child);
    child.setInstance(org.isInstance());

    // Copy Slots from Ancestors
    Vector allSlotVec = org.getSlotList().getSlotVector();
    for(int i=0;i<allSlotVec.size();i++) {
      Slot sl = (Slot)allSlotVec.elementAt(i);
      if(! (sl.getName().equals("Name") || sl.getName().indexOf("_") == 0))
        child.addSlot(sl.getName(), sl.getValue(),sl.getType(),
                      sl.getCondition(),sl.getArgument(), sl.getRequired(),
                      sl.getRequiredAtBeginningOnly(), sl.getDontFill(), sl.getShared());
    }

    // Also copy children frames
    Vector orgChildren = org.getChildren();
    for(int i=0;i<orgChildren.size();i++) {
      KFrame item = ((KFrame)orgChildren.elementAt(i));
      child.addCopyOf(item);
    }
    redrawPanel();
    return child;
  }

  public KFrame createFrame(String cname) {
    KFrame child = new KFrame(cname, rootFrame);
    add(child);
    redrawPanel();
    return child;
  }

  public void writeDOT(PrintStream ps, Vector writtenItems, boolean isColor, boolean drawSlot) {
    if(writtenItems==null)
      writtenItems = new Vector();

    if(writtenItems.contains(this))
      return;

    writtenItems.add(this);

    // not traverse into children if they are hidden
    if (showChildren) {
      for (int i = 0; i < children.size(); i++) {
        KFrame item = ( (KFrame) children.elementAt(i));
        if (isColor)
          ps.println("\"" + name + "\" -> \"" + item.getName() +
                     "\" [style=bold, color=\"red\", weight=1];");
        else
          ps.println("\"" + name + "\" -> \"" + item.getName() +
                     "\" [style=solid, weight=1];");
        item.writeDOT(ps, writtenItems, isColor, drawSlot);
      }
    } else {
      ps.println("node [shape=box];");
         if (isColor)
           ps.println("\"" + name + "\" -> \""+name+"'s childen\" [style=bold, color=\"red\", weight=1];");
         else
           ps.println("\"" + name + "\" -> \""+name+"'s childen\" [style=solid, weight=1];");
    }

    StringBuffer nameList = new StringBuffer();
    StringBuffer valueList = new StringBuffer();

    boolean notfirst = false;

    for(int i=0;i<mySlotList.size();i++) {
      Slot sl = mySlotList.slotAt(i);
      String slotName = sl.getName();
      if(sl.getType()==Slot.TYPE_INSTANCE) {
        if(!this.isInstance()) {
          String target = sl.getArgument();
          if(target.equals(""))
            target = "(Any)";
          if(isColor)
            ps.println("\""+name + "\" -> \""+target+"\" [color=\"blue\", weight=0];");
          else
            ps.println("\""+name + "\" -> \""+target+"\" [style=dashed, weight=0];");
        }
        else {
          String target = sl.getValue();
          if(!target.equals("")) {
            if(isColor)
              ps.println("\""+name + "\" -> \""+target+"\" [color=\"green\", weight=0];");
            else
              ps.println("\""+name + "\" -> \""+target+"\" [style=dotted, weight=0];");
          }
        }
      }
      if(drawSlot && !slotName.equals("Name") && ! (slotName.indexOf("_") == 0)) {
        if (notfirst) {
          nameList.append("|");
          valueList.append("| ");
        } else
          notfirst = true;

        nameList.append(slotName);

        // Instance frame, must have slot values
        //if(this.isInstance()) {
          //nameList.append("\\n== ");
          //nameList.append(sl.getValue());
          //if (value != "") {
          //nameList.append(" ("+sl.getValue()+")");
          //if (sl.getValue() == "") nameList.append("\\\"\\\"");
        //} else {

        // if not instance, can have slot values too

        // Also print the slot condition, if any.
        if(sl.getCondition()!=Slot.COND_ANY) {
          String condname = sl.getConditionName();
          condname = condname.replaceAll(">", "\\\\>");
          condname = condname.replaceAll("<", "\\\\<");
          valueList.append("[cond: "+condname+" ");
          valueList.append(sl.getArgument()+"] ");
        }

        String value = sl.getValue();
        if (value != "") {
          value = value.replaceAll(">", "\\\\>");
          value = value.replaceAll("<", "\\\\<");
          value = value.replaceAll("\"", "\\\"");
          value = value.replaceAll("\\{", "\\\\{");
          value = value.replaceAll("\\}", "\\\\}");
          value = value.replaceAll("\\|", "\\\\|");
          value = value.replaceAll("\\\\", "\\\\\\\\");
          value = value.replaceAll("\"", "\\\\\"");
          value = value.replaceAll("\'", "\\\\\'");
        } else
          value = " - ";
        valueList.append(value);
      }
    }
    String slotsandvalues = "{{"+nameList.toString()+"}|{"+valueList.toString()+"}}";
    if(isColor) {
      if(this.isInstance())
        ps.println("\""+name + "\" [color=\""+"red"+"\""+
                   ",shape=record,labeljust=\"l\",label=\"{INSTANCE: "+name+"|"+slotsandvalues+"}\"];");
      else if ( drawSlot ) {
        ps.println("\""+name + "\" [color=\""+"green"+"\""+
                   ",shape=record,labeljust=\"l\",label=\"{FRAME: "+name+"|"+slotsandvalues+"}\"];");
      } else {
        ps.println("\""+name + "\" [color=\""+"green"+"\""+
                   ",shape=record,label=\"{"+name+"}\"];");
      }
    }
    else {
      if(this.isInstance()) {
        ps.println("\"" + name + "\" [shape=record,labeljust=\"l\",label=\"{INSTANCE: " +
                   name+"|"+slotsandvalues + "}\"];");
      } else if (drawSlot) {
        ps.println("\"" + name + "\" [shape=record,labeljust=\"l\",label=\"{FRAME: " +
           name+"|"+slotsandvalues + "}\"];");
      } else {
        ps.println("\"" + name + "\" [shape=record,label=\"{" +
           name+ "}\"];");
      }
    }
  }

  /**
   * Get list of unique frame object
   * Multiple-inheritance may result in multiple prinout of same object.
   */

  void getFrameObjectList(Vector flist) {
    if(!flist.contains(this)) {
      flist.add(this);
      for(int i=0;i<children.size();i++) {
        KFrame item = ((KFrame)children.elementAt(i));
        item.getFrameObjectList(flist);
      }
    }
  }

  public void writeXMLFile(String filename) {
    try {
      FileOutputStream fo = new FileOutputStream(filename);
      PrintStream ps = new PrintStream(fo, true, "UTF-8");
      ps.println("<?xml version='1.0' encoding='utf-8'?>");
      writeXML(ps);
      ps.close();
      fo.close();
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex.toString(),
                                    "Error Writing XML File", JOptionPane.WARNING_MESSAGE);
      // System.err.println("Error writing XML File: "+ex);
    }
  }

  private void writeXML(PrintStream ps) {
    Vector framelist = new Vector();
    getFrameObjectList(framelist);
    ps.println("<FRAMELIST>");
    for(int i=0;i<framelist.size();i++)
      ((KFrame)framelist.elementAt(i)).writeSelfXML(ps);
    ps.println("</FRAMELIST>");
  }

  private void writeSelfXML(PrintStream ps) {
    //System.out.println("Saving frame "+getName());
    ps.println("<FRAME>");
    ps.println("<NAME>"+name+"</NAME>");
    for(int i=0;i<parents.size();i++)
      ps.println("<ISA>"+((KFrame)parents.elementAt(i)).getName()+"</ISA>");

    ps.println("<ISINSTANCE>"+(isInstance?"TRUE":"FALSE")+"</ISINSTANCE>");
    ps.println("<SHOWCHILDREN>"+(showChildren?"TRUE":"FALSE")+"</SHOWCHILDREN>");
    if(mySlotList.size()>1) {
      ps.println("<SLOTLIST>");
      for(int i=0;i<mySlotList.size();i++) {
        Slot sl = mySlotList.slotAt(i);
        String slotName = sl.getName();
        // Skip Name slot and _xxxx system slots
        if(!slotName.equals("Name") && slotName.indexOf("_") != 0) {
          ps.println("<SLOT>");
          ps.println("<NAME>"+slotName+"</NAME>");
          ps.println("<TYPE>"+Slot.type_varnames[sl.getType()]+"</TYPE>");
          ps.println("<CONDITION>"+Slot.cond_varnames[sl.getCondition()]+"</CONDITION>");
          ps.println("<ARGUMENT>"+escapeXMLString(sl.getArgument())+"</ARGUMENT>");
          ps.println("<VALUE>"+escapeXMLString(sl.getValue())+"</VALUE>");
          ps.println("<REQUIRED>"+(sl.getRequired()?"TRUE":"FALSE")+"</REQUIRED>");
          ps.println("<REQUIREDB>"+(sl.getRequiredAtBeginningOnly()?"TRUE":"FALSE")+"</REQUIREDB>");
          ps.println("<DONTFILL>"+(sl.getDontFill()?"TRUE":"FALSE")+"</DONTFILL>");
          ps.println("<SHARED>"+(sl.getShared()?"TRUE":"FALSE")+"</SHARED>");
          ps.println("<UNIQUE>"+(sl.getUnique()?"TRUE":"FALSE")+"</UNIQUE>");
          ps.println("</SLOT>");
//          ps.println(name+".addSlot(\""+slotName+"\",\""+
//                     escapeString(sl.getValue())+"\",Slot."+
//                     Slot.type_varnames[sl.getType()]+",Slot."+
//                     Slot.cond_varnames[sl.getCondition()]+",\""+
//                     escapeString(sl.getArgument())+"\","+
//                     (sl.getRequired()?"true":"false")+","+
//                     (sl.getShared()?"true":"false")+
//                     ");");
        }
      }
      ps.println("</SLOTLIST>");
    }
    ps.println("</FRAME>");
  }

  public void writeJS(PrintStream ps, String parentName) {
    if(parentName!=null) {  // Not a Root Node
      if(isInstance) {
        ps.println(name+"="+parentName+".createFrame(\""+name+"\");");
        ps.println(name+".setInstance(true);");
      }
      else
        ps.println(name+"="+parentName+".createFrame(\""+name+"\");");
    }
    for(int i=0;i<mySlotList.size();i++) {
      Slot sl = mySlotList.slotAt(i);
      String slotName = sl.getName();
      if(!slotName.equals("Name"))
        ps.println(name+".addSlot(\""+slotName+"\",\""+
          escapeString(sl.getValue())+"\",Slot."+
          Slot.type_varnames[sl.getType()]+",Slot."+
          Slot.cond_varnames[sl.getCondition()]+",\""+
          escapeString(sl.getArgument())+"\","+
          (sl.getRequired()?"true":"false")+","+
          (sl.getShared()?"true":"false")+","+
          (sl.getUnique()?"true":"false")+
          ");");
    }
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      item.writeJS(ps, name);
    }
  }

  /**
   * Remove all instance nodes
   */

  public void Obsolete_removeInstance() {
    for(int i=0;i<children.size();i++) {
      KFrame item = ((KFrame)children.elementAt(i));
      if(item.isInstance()) {
        System.out.println("Removing "+item.getName());
        inactivechildren.add(children.elementAt(i));
        children.removeElementAt(i);
        i--;
      }
      else
        item.Obsolete_removeInstance();
    }
    //instanceCount = 0;
  }

  private String escapeString(String sin) {
    String sout = "";
    while(sin.length()>0) {
      int idx = sin.indexOf("\"");
      if(idx>=0) {
        sout+=sin.substring(0,idx);
        sout+="\\\"";
        sin = sin.substring(idx+1);
      }
      else {
        sout+=sin;
        break;
      }
    }
    return sout;
  }

  private String escapeXMLString(String sin) {
    StringBuffer sb;
    if ( sin == null) {
      sb = new StringBuffer("");
    } else {
      sb = new StringBuffer(sin);
    }
    for (int j=0; j<sb.length(); j++) {
      if (sb.charAt(j) == '<') {
        sb.setCharAt(j, '&');
        sb.insert(j+1, "lt;");
        j += 3;
      }
      else if (sb.charAt(j) == '>') {
        sb.setCharAt(j, '&');
        sb.insert(j+1, "gt;");
        j += 3;
      }
      else if (sb.charAt(j) == '&') {
        sb.setCharAt(j, '&');
        sb.insert(j+1, "amp;");
        j += 4;
      }
    }
    return new String(sb);
  }

  /**
   * Get the latest ID
   * @return
   */
  public int getIDCount() {
    return idcount;
  }

  /**
   * Set the latest ID
   * @return
   */
  public void setIDCount(int id) {
    idcount = id;
  }

  /**
   * Retrieve the age of this frame since the creation time
   *
   * @return age in seconds
   */

  public long getAge() {
    Date d = new Date();
    return (d.getTime() - createtime) /1000;
  }

  /**
   * Retrieve the age of this frame since last update
   *
   * @return age in seconds
   */

  public long getAgeSinceLastUpdate() {
    Date d = new Date();
    return (d.getTime() - lastupdate) /1000;
  }

  /**
   * Get list of all hierarchical slots
   * Return: Hash map of slot shortname -> Fullname
   * put hash map of slot fullname -> Object in the argument, if given
   * slotHash: hash from FullName to Slot object
   */

  public Hashtable getAllSlotFullnames() {
    return getAllSlotFullnames(null);
  }

  private Hashtable doGetAllSlotFullnames(Hashtable slotHash) {
    Hashtable mine = new Hashtable();
    Vector slVec = mySlotList.getSlotVector();
    for(int i=0;i< slVec.size();i++) {
      Slot sl = ((Slot)slVec.elementAt(i));
      String slname = sl.getName();
      if( ! slname.equals("Name") &&
          slname.indexOf("_") != 0 &&
          !slname.matches("^on[A-Z].*") &&
          !slname.equals("condition")) {
        String slfname = name + "-" + slname;
        // Just overwrite if there is already an entry
        mine.put(slname, slfname);
        if (slotHash != null) {
          slotHash.put(slfname, sl);
        }
      }
    }
    return mine;

  }
  public Hashtable getAllSlotFullnames(Hashtable slotHash) {
    Hashtable fromParent = new Hashtable();
    /*
     // this doesn't work because later put slots will override the previous
    for(int i=0;i<parents.size();i++) {
      fromParent.putAll( ( (KFrame) parents.elementAt(i)).getAllSlotFullnames(slotHash));
    }
    if (isInstance) return fromParent;
*/
    if ( parents.size() != 0 ) {
      // collect parents' slot first
      Vector allparents = getAllParentsFrames();
      // frames with higher distance from me first
      for (int i = allparents.size()-1; i >= 0; i--) {
        fromParent.putAll( ( (KFrame) allparents.elementAt(i)).doGetAllSlotFullnames(slotHash));
      }
    }
    if ( !isInstance ){
      // then include my slots
      fromParent.putAll(doGetAllSlotFullnames(slotHash));
    }
    return fromParent;
  }

  public boolean getInused() {
    return inused>0;
  }

  /**
   * Call the WorkingMemory::rereadRoot (i.e. reset)
   */
  public void resetWM() {
    if (wm != null ) wm.rereadRoot();
  }

  public void setInused(boolean i) {
    inused += i?1:(-1);
    if(inused < 0) {
      System.out.println("Negative instance inuse count in: " + this.getName());
      //System.out.println("Set to 0");
      //inused = 0;
    }

  }

  /**
   * Collect all "doUpdate" slots up the inheritance hierarchy.
   * And process them.
   *
   * Note that this function is called from an INSTANCE (not a
   * Frame definition). As a result, the code below starts collecting
   * doUpdate from its parent, ignoring its own doUpdate.
   *
   * Hierarchical collection is processed by calling getSpecialSlot()
   * in runSpecialSlot().
   */

  private void selfDoUpdate(boolean dowait) {
    System.out.println("Entering DoUpdate: "+getName());
    runSpecialSlot("onUpdate",dowait);

    Date d = new Date();
    lastupdate = d.getTime();
  }

  public void doUpdate() {
    doUpdate(true);
  }

  public void doUpdate(boolean dowait) {
    Vector doUpdateList = new Vector();
    Vector queueList = new Vector();
    Vector seenList = new Vector();

    // First run doUpdate of this instance
    queueList.add(this);
    while(queueList.size()>0) {
      getDoUpdateList(queueList, seenList, doUpdateList);
    }

    for(int i=0;i<doUpdateList.size();i++)
      ((KFrame)doUpdateList.elementAt(i)).selfDoUpdate(dowait);
  }

  private void getDoUpdateList(Vector queue, Vector seen, Vector result) {
    KFrame item = (KFrame)queue.remove(0);
    if(seen.contains(item))
      return;

    seen.add(item);
    Vector depender = item.getDependerList();
    queue.addAll(depender);
    result.add(item);
    result.addAll(depender);

    // Remvoe redundant entries from result
    // Keep only the one farther away to the right (process later)
    int i=0;
    while(i<result.size()) {
      KFrame item2 = (KFrame)result.elementAt(i);
      if(result.indexOf(item2, i+1)>=0) { // Contains redundant item
        result.removeElementAt(i);
      }
      else
        i++;
    }
  }

  /**
   * Collect values of "condition" and "onTry" slots
   * up the inheritance hierarchy.
   *
   * Note that this function is called from an INSTANCE (not a
   * Frame definition). As a result, the code below starts collecting
   * C&O from its parent, ignoring its own C&O.
   */

  public boolean checkCondition() {
    // Get all condition Strings in the hierachy tree up from this Frame.
    Vector cmds = new Vector();
    for(int i=0;i<parents.size();i++)
      cmds.addAll(((KFrame)parents.elementAt(i)).getConditionAndOnTry());
    if(cmds.size()>0) {
      System.out.println("Condition process Size: "+cmds.size());
      //wm.putJSVariable("s", getKFrameScript());
      //wm.putJSCommand("var S = Root.findFrame(s.Name)");
      String intro = "var S = Root.findFrame(\""+getName()+"\");var s = S.getKFrameScript();";

      for(int i=0;i<cmds.size();i++) {
        String cmd = (String)cmds.elementAt(i++);
        //System.out.println("Process "+cmd+" "+(String)cmds.elementAt(i));
        if(cmd.equalsIgnoreCase("C")) {
          System.out.println("Condition: "+cmd);
          wm.putJSCommand(intro+(String)cmds.elementAt(i));
          wm.waitJSDone();
          if(!wm.getJSResult().startsWith("true"))
            return false;
        }
        else {
          System.out.println("onTry: "+cmd);
          wm.putJSCommand(intro+(String)cmds.elementAt(i));
          wm.waitJSDone();
          if(wm.getJSResult().startsWith("false"))
            return false;
        }
      }
    }
    return true;
  }

  /**
   * Similar to checkCondition() but check the condition in case of my parent
   * is fr instead of the real parent
   *
   */
  public boolean checkCondition(KFrame fr) {
    Vector cmds = new Vector();
    cmds.addAll(fr.getConditionAndOnTry());
    if(cmds.size()>0) {
      System.out.println("Condition and onTry process Size: "+cmds.size());
      String intro = "var S = Root.findFrame(\""+getName()+"\");var s = S.getKFrameScript();";

      for(int i=0;i<cmds.size();i++) {
        String cmd = (String)cmds.elementAt(i++);
        System.out.println("Process "+cmd+" "+(String)cmds.elementAt(i));
        if(cmd.equalsIgnoreCase("C")) {
          System.out.println("Condition: "+cmd);
          wm.putJSCommand(intro+(String)cmds.elementAt(i));
          wm.waitJSDone();
          if(!wm.getJSResult().startsWith("true"))
            return false;
        }
        else {
          System.out.println("onTry: "+cmd);
          wm.putJSCommand(intro+(String)cmds.elementAt(i));
          wm.waitJSDone();
        }
      }
    }
    return true;
  }

  public void removeInstance(KFrame ins) {
    // if it is just instantiated for testing condition purpose then
    // we should not decrease the count
    synchronized (this) {
      if (children.indexOf(ins) == children.size() - 1 && ins.getAge() < 5) {
        instanceCount--;
      }
      children.removeElement(ins);
    }
  }

  /**
   * Set Hash String of slots used for instantiating this Instance.
   * Note that the hash string may contain only <b>required</b> slots
   * which are required at the time of instantiation.
   */
  public void setParamHash(String s) {
    // System.err.println("HashSet "+this.getName()+" to "+s);
    paramHash = s;
  }

  public String getParamHash() {
    return paramHash;
  }

  /**
   * Get the current param hash in the form of Framename-Slot=Value
   * @return Vector of [slotname, slotvalue] pairs
   */
  public Vector getCurrentParamHash() {
    if(this.isInstance) {
      Vector retval = new Vector();
      StringBuffer retvalue1 = new StringBuffer();

      Hashtable slotHash = new Hashtable(); // Map fullname -> Slot Object
      Hashtable allHash = getAllSlotFullnames(slotHash); //Map Sname -> Fname
      Hashtable shortname = new Hashtable(); //Map Fname -> Sname
      Vector allSlots = new Vector(); // Full name of all slots of this Frame

      Enumeration en = allHash.keys();
      while (en.hasMoreElements()) {
        String sname = (String) en.nextElement(); // Short Name
        String fname = (String) allHash.get(sname); // Full Name
        allSlots.add(fname);
        shortname.put(fname, sname);
      }

      Collections.sort(allSlots);

      for (int i = 0; i < allSlots.size(); i++) {
        String fname = (String) allSlots.elementAt(i);
        String sname = (String) shortname.get(fname);
        Slot sl = (Slot) slotHash.get(fname);

        if ( this.getSlotValue(sname) != null &&  this.getSlotValue(sname) != "") {
          Vector tmp = new Vector();
          tmp.add(fname);
          tmp.add(this.getSlotValue(sname));
          retval.add(tmp);
        }
      }
      return retval;
    }
    return null;
  }

  public String getParamHash_AllSlots() {
    return getParamHash_AllSlots(this);
  }

  public String getParamHash_AllSlots(KFrame frame) {
    StringBuffer retvalue1;
    if(frame.isInstance()) {
      retvalue1 = new StringBuffer(frame.getParentName());
    } else {
      retvalue1 = new StringBuffer(frame.getName());
    }
    StringBuffer retvalue2 = new StringBuffer();
    Hashtable slotHash = new Hashtable(); // Map fullname -> Slot Object
    Hashtable allHash = getAllSlotFullnames(slotHash); //Map Sname -> Fname
    Hashtable shortname = new Hashtable(); //Map Fname -> Sname
    Vector allSlots = new Vector(); // Full name of all slots of this Frame

    Enumeration en = allHash.keys();
    while (en.hasMoreElements()) {
      String sname = (String) en.nextElement(); // Short Name
      String fname = (String) allHash.get(sname); // Full Name
      allSlots.add(fname);
      shortname.put(fname, sname);
    }

    Collections.sort(allSlots);

    for (int i = 0; i < allSlots.size(); i++) {
      String fname = (String) allSlots.elementAt(i);
      String sname = (String) shortname.get(fname);
      Slot sl = (Slot) slotHash.get(fname);

      if (sl.getRequired()) { // Check only Required Slots
        if (sl.getType() == Slot.TYPE_INSTANCE) {
          retvalue2.append(":" + fname + "=" + this.getSlotValue(sname));
        }
        else {
          retvalue1.append(":" + fname + "=" + this.getSlotValue(sname));
        }
      }
    }
    return retvalue1.toString() + retvalue2.toString();
  }

  /**
   * get the hash of myself
   * @return a Hashtable of <slot, value> of all my slots
   */
  public Hashtable getHash() {
    Hashtable myhash = new Hashtable(); // shortname --> value

    Vector myslots = mySlotList.getSlotVector();

    for (int i = 0; i < myslots.size(); i++) {
      String sname = (String) ((Slot) myslots.elementAt(i)).getName();
      myhash.put(sname,this.getSlotValue(sname));
    }
    return myhash;
  }

  /**
   * Try if the given Hashtable can result in an instance of mine
   * @return 0 if the hash match this frame's condition
   * @return -1 if the hash does not match this frame's condition
   * @return -2 if there is no proper condition to match this frame,
   * which means it has no own required slots AND no condition slot
   * AND (has single parent OR multiple parents but still they don't have
   * required slots)
   */
  public int tryMe(Hashtable hash) {
    // check own slot first
    Vector ownslots = mySlotList.getSlotVector();
    int numrequiredslots = 0;
    boolean hascondition = false;

    for(int i=0;i<ownslots.size();i++) {
      if ( ((Slot) ownslots.elementAt(i)).getRequired()) {
        numrequiredslots++;
      }
      if ( ((Slot) ownslots.elementAt(i)).getName().compareTo("condition") == 0)
        hascondition = true;
    }

    if (numrequiredslots == 0 && ! hascondition && parents.size() == 1) {
      // NEITHER OWN required slots NOR condition AND single parent
      //System.out.println(getName()+": NEITHER OWN required slots NOR condition AND single parent");
      return -2;
    }

    Vector myslots = getAllSlotList().getSlotVector();
    numrequiredslots = 0;
    // check all slot conditions
    for(int i=0;i<myslots.size();i++) {
      Slot sl = (Slot) myslots.elementAt(i);
      String sname = sl.getName();
      String slotvalue = null;
      if (hash.containsKey(sname)) {
        slotvalue = (String) hash.get(sname);
      }
      if (sl.getRequired()) { // Check only Required Slots
        //System.out.println("Checking slot "+sname);
        numrequiredslots++;
        // in case of instance slot, if the value has not been set,
        // we can help a bit, but when should we help?
        // 1) we share the same non-root parent frame(s)
        if (slotvalue == null || slotvalue == "") {
          if (sl.getType() == Slot.TYPE_INSTANCE) {
            Vector tmpparents = Slot.stringToVector( (String) hash.get("_ISA"));
            boolean letshelp = false;
            for (int j = 0; j < tmpparents.size(); j++) {
              String tmpp = (String) tmpparents.elementAt(j);
              for (int k = 0; k < parents.size(); k++) {
                String pname = ( (KFrame) parents.elementAt(k)).getName();
                //System.out.println("tmpp is "+tmpp+ ", pname is "+pname);
                if (pname != "Root" && pname.equals(tmpp)) {
                  letshelp = true;
                }
              }
            }

            if (letshelp) {
              // FIXME, still not cover all possible choices
              // FIXME, onUpdate stuff has to be taken care of
              // FIXME, use instanceCombination?
              Vector ins = wm.findInstancesOf(sl.getArgument());
              //System.err.println("Total "+ins.size()+" candiate(s) for "+sl.getArgument());
              for (int j = 0; j < ins.size(); j++) {
                KFrame cand = (KFrame) ins.elementAt(j);
                //System.err.println("Checking candidate: " + cand.getName());
                Vector dependerList = cand.getDependerList();
                boolean alreadyused = false;
                for (int k = 0; k < dependerList.size(); k++) {
                  //System.err.println("Comparing "+((KFrame) dependerList.elementAt(k)).getParentName() + " and "+f.getName());
                  // checking if it is already used to create meself or my children
                  KFrame tmpelement = (KFrame) dependerList.elementAt(k);
                  if (tmpelement.getParentName().equals(getName()) ||
                      inChildList(tmpelement.getParentFrame()) ||
                      inParentList(tmpelement.getParentFrame())) {
                    //System.err.println("As " + cand.getName() + " was used in creating " +
                    //( (KFrame) dependerList.elementAt(k)).getName() + " already, now not used again");
                    alreadyused = true;
                    break;
                  }
                }
                if ( (sl.getShared() || (!cand.getInused()))) {
                  KFrame tmpframe = rootFrame.findFrame( (String) hash.get(
                      "Name"));
                  //System.out.println("Finding "+ (String) hash.get("Name"));
                  if (tmpframe != null) {
                    //System.out.println("Found!");
                    if (slotvalue == null) {
                      // add a slot to it
                      tmpframe.addSlot(sl.getName(), "", Slot.TYPE_INSTANCE,
                                       sl.getCondition(), sl.getArgument(),
                                       sl.getRequired(),
                                       sl.getRequiredAtBeginningOnly(),
                                       sl.getDontFill(),
                                       sl.getShared());
                    }
                    slotvalue = cand.getName();
                    tmpframe.setSlotValue(sl.getName(), slotvalue,false);
                  }
                }
              }
            }
          }
        }

        if (slotvalue == null) {
          // required slot needs a value
          //System.err.println("NO value for: " + sname);
          return -1;
        }
      }

      // non-required slots may not have a value, but if it does
      // check its value
      if ( slotvalue != null && ! sl.checkValue(slotvalue)) {
        //System.out.println("tryMe: failed at slot "+sname);
        //System.out.println("tryMe: hash is "+hash.get(sname));
        //System.out.println("tryMe: checkvalue returns "+sl.checkValue((String) hash.get(sname) ));
        return -1;
      }

    }

    if (numrequiredslots == 0 && ! hascondition ) {
      //System.out.println(getName() + ": No own and parents' required slots and no condition");
      return -2; // NO required slots
    }
    //System.out.println("No. of required slots is "+numrequiredslots);

    KFrame inst = createInstance(false, false);
    inst.setDummy(true); // so that setSlotValue won't update wm.instanceHash
    for(int i=0;i<myslots.size();i++) {
      Slot sl = (Slot) myslots.elementAt(i);
      String sname = sl.getName();
      if ( hash.containsKey(sname) && sname.indexOf("_") != 0) {
        //System.out.println("Setting slot "+sname+ " to "+ (String) hash.get(sname));
        inst.setSlotValue(sname, (String) hash.get(sname), false);
      }
    }

    // check the frame condition
    boolean result = inst.checkCondition() ;
    inst.removeHash(); // inst should actually never exist in wm's instanceHash
    removeInstance(inst);
    if (result) return 0; else return -1;
  }

  /**
   * This method is supposed to be called before this instance is freed,
   * or deleted. It walks through all slots of type instance and decrease
   * the inused counter of those instances.
   */
  public void freeInstancesInUse() {
    for(int i=0;i<this.mySlotList.size();i++) {
      Slot sl = this.mySlotList.slotAt(i);
      if(sl.getType() == Slot.TYPE_INSTANCE) {
        String instanceName = sl.getValue();
        KFrame instance = this.rootFindFrame(instanceName);
        if (instance != null) instance.setInused(false);
      }
    }
  }

  public void runSpecialSlot(String slotname) {
    runSpecialSlot(slotname, this, true);
  }

  public void runSpecialSlot(String slotname, boolean dowait) {
    runSpecialSlot(slotname, this, dowait);
  }

  public void runSpecialSlot(String slotname, KFrame startingnode) {
    runSpecialSlot(slotname, startingnode, true);
  }

  /**
   * execute the contents of the specified special slot of me and parents, if any
   *
   * @param slotname name of the special slot
   * @param startingnode starts to run the special slot from starting node upwards
   * @param dowait if we should wait until all the JavaScript execution is done
   */
  public void runSpecialSlot(String slotname, KFrame startingnode, boolean dowait) {
    Vector cmds;
    if (slotname.indexOf("onTransition") == 0) {
      // it's much less complicated if we don't do recursive here
      cmds = new Vector();
      String tmpstring = startingnode.getSlotValue(slotname);
      if (tmpstring != null && tmpstring != "" )
        cmds.add(tmpstring);
    } else {
      cmds = getSpecialSlot(slotname, startingnode);
    }

    System.out.println(startingnode.getName()+"'s "+" process Size: " + cmds.size());
    if (cmds.size() > 0) {
      for (int i = cmds.size() -1; i >=0; i--) { // run the parents's first
        String cmd = (String) cmds.elementAt(i);
        //wm.putJSVariable("s", getKFrameScript());
        System.out.println("Process: " + cmd + " " + (String) cmds.elementAt(i));
        String intro = "var S = Root.findFrame(\""+getName()+"\");var s = S.getKFrameScript();";
        wm.putJSCommand( intro + (String) cmds.elementAt(i));
        if (dowait)
          wm.waitJSDone();
      }
    }
  }

  class AgeComp implements Comparator  {
      public int compare(Object e1, Object e2) {
          KFrame k1 = (KFrame) e1;
          KFrame k2 = (KFrame) e2;

          if(k1.getAge() < k2.getAge())
            return -1;
          else if(k1.getAge() > k2.getAge())
            return 1;
          else
            return 0;
      }
  }

  class DepthComp implements Comparator  {
    public int compare(Object e1, Object e2) {
        Vector k1 = (Vector) e1;
        Vector k2 = (Vector) e2;
        return ((Integer) k1.elementAt(1)).compareTo((Integer) k2.elementAt(1));
    }
  }
}
