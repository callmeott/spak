package spak;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title: ApplicationFrame</p>
 * <p>Description: Main Frame representing SPAK UI</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 *
 * $Log: ApplicationFrame.java,v $
 * Revision 1.1  2005-08-02 07:30:34  pattara
 * -first import
 *
 * Revision 1.28  2005/07/20 08:04:21  pattara
 * -0.99a
 *
 * -First phase code clean up done.
 * -Ready for extensive test, heading towards 1.0
 *
 * Revision 1.27  2005/07/08 18:52:35  pattara
 * -0.96k: numerous bug fixes including introduction of race conditions
 *  prevention
 *
 * Revision 1.26  2005/06/15 19:17:24  pattara
 * -0.96j: fix the bug that parallel lines won't be created if lines that are
 *  not parallel has been created first. ==> ForwardChaining: add the
 *  tmpAddToMySlots vector to maintain temporary slot values that SPAK adds
 *  to wm.mySlots during the trying process. If the try is failed (e.g., the
 *  condition check fails), these slot values should be removed, otherwise
 *  they will exist and interfere with the next try.
 *
 * Revision 1.25  2005/03/28 06:16:09  pattara
 * -code cleanup
 * -0.96i
 *
 * Revision 1.24  2005/03/06 16:25:46  pattara
 * -Not save _ID and _ISA (actually all slots beginning with _ )
 * -Initialize condition argument for _ISA with "" instead of null
 * -checking of null string in escapeXML when saving (avoid exception)
 *
 * Revision 1.23  2004/12/08 08:57:26  vuthi
 * Add new function "writeXMLFile()" and use only this function for writing KB.
 *
 * Revision 1.22  2004/11/12 06:16:04  pattara
 * -add OneShotEvaluate in Tool
 * -fix double instance bug in Backward Chaining (which I added some days ago)
 *
 * Revision 1.21  2004/09/25 09:43:01  pattara
 * -reinduce feature added
 *
 * Revision 1.20  2004/09/08 15:45:42  pattara
 * -yesterday forgot to add the file
 *
 * Revision 1.19  2004/09/08 08:51:11  vuthi
 * Remove args from jbInit() to fix Design view editing
 *
 * Revision 1.18  2004/09/08 08:10:19  pattara
 * -remove event log temporary coz I forgot to add the file last nite
 *
 * Revision 1.17  2004/09/07 14:50:22  pattara
 * -new feature: Tool/Show Events Log -- to see all the incoming events so far
 * -set the weight in the main jsplit panel in favor of the kscrollpanel
 *
 * Revision 1.16  2004/09/04 17:57:27  pattara
 * -add Reload into the menu
 *
 * Revision 1.15  2004/08/26 14:22:55  pattara
 * -add a menu item to stop the Evaluator thread
 *
 * Revision 1.14  2004/08/18 02:25:39  vuthi
 * Version 0.96h.
 * Bug fix: Save XML file in UTF-8 encoding.
 * The file was saved using default (S-JIS) encoding while
 * SPAK can open only UTF-8 files.
 *
 * Revision 1.13  2004/08/17 12:44:10  pattara
 * -onEvaluate feature now can be started by the menu Tool/StartEval
 *
 * Revision 1.12  2004/07/28 05:48:51  vuthi
 * Add "reset" menu.
 * Fix bug in WM losing Console when a new KB is loaded.
 *
 * Revision 1.11  2004/07/28 04:59:20  vuthi
 * 1) Add file filter for .xml, .js, etc.
 * 2) Improve slot editor. Disable editing of non-related attributes.
 * 3) Implement UNIQUE attribute. Allow network client to update
 *     non-unique required values of existing Instances.
 * 4) Add/Implement "onUpdate" slot.
 *
 */

public class ApplicationFrame extends JFrame implements
                DropTargetListener, MouseListener, MouseMotionListener {
  JPanel contentPane;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenuItem jMenuFileExit = new JMenuItem();
  JMenu jMenuHelp = new JMenu();
  JMenuItem jMenuHelpAbout = new JMenuItem();
  JLabel statusBar = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  JMenu jMenuTool = new JMenu();
  KnowledgePanel kp = new KnowledgePanel();
  JScrollPane kScrollPane = new JScrollPane(kp);
  JScrollPane consoleScroll = null;
  JSplitPane splitPane = null;
  JMenuItem jMenuItem1 = new JMenuItem();
  JMenuItem jMenuItem2 = new JMenuItem();
  JMenu jMenuEdit = new JMenu();
  JMenuItem jMenuItemClearAll = new JMenuItem();
  JMenuItem jMenuItemAddBox = new JMenuItem();
  JMenuItem jMenuItem3 = new JMenuItem();
  JMenuItem jMenuItemSaveJS = new JMenuItem();
  JMenuItem jMenuItemRedraw = new JMenuItem();
  JMenuItem jMenuItemOpenJS = new JMenuItem();
  JMenuItem jMenuItem4 = new JMenuItem();
  JMenuItem jMenuItemReInduce = new JMenuItem();
  JMenuItem jMenuItemSaveXML = new JMenuItem();
  JMenuItem jMenuItemOpenXML = new JMenuItem();
  JMenuItem jMenuItemReload = new JMenuItem();
  JMenuItem jMenuItemSave = new JMenuItem();
  JMenuItem jMenuItemBackwardInduction = new JMenuItem();
  JMenuItem jMenuItemReset = new JMenuItem();
  JMenuItem jMenuItemShowEventsLog = new JMenuItem();
  JMenuItem jMenuItemStartEval = new JMenuItem();
  JMenuItem jMenuItemOneShotEval = new JMenuItem();
  JMenuItem jMenuItemStopEval = new JMenuItem();
  JMenuItem jMenuItem6 = new JMenuItem();

  ZeroConsole zeroConsole=null;
  NetworkMonitor instanceMonitor = null;
  JScriptShell jss = null;
  static JFileChooser jFileChooser1 = new
      JFileChooser(System.getProperty("user.dir"));
  EventsLogWindow evw = null;

  String dataFileName = new String();

  /**
   * Return version number of this JavaZero build.
   */

  String getVersion() {
    return "0.99a";
  }

  //Construct the frame
  public ApplicationFrame(String[] args) {
    if ( args.length == 1 ) {
      dataFileName = args[0];
    } else {
      dataFileName = "";
    }
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    setDropTarget(new DropTarget(this, this));
    kp.addMouseListener(this);
    kp.addMouseMotionListener(this);
    if ( dataFileName != "" ) {
      loadXML(dataFileName);
      updateTitle();
    }
    this.launchInstanceMonitor();
  }

  //Component initialization
  //setIconImage(Toolkit.getDefaultToolkit().createImage(ApplicationFrame.class.getResource("[Your Icon]")));

  private void jbInit() throws Exception  {
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(740, 480));
    this.setTitle("SPAK Version "+getVersion());
    statusBar.setText("Ready...");
    jMenuFile.setText("File");
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jMenuHelp.setText("Help");
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jMenuTool.setText("Tool");
    jMenuTool.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuTool_actionPerformed(e);
      }
    });
    kScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    kScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    kScrollPane.setOpaque(false);
    jMenuItem1.setText("Open Zero++");
    jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem1_actionPerformed(e);
      }
    });
    jMenuItem2.setText("Show Slots");
    jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenuEdit.setText("Edit");
    jMenuItemClearAll.setText("Clear All");
    jMenuItemClearAll.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemClearAll_actionPerformed(e);
      }
    });
    jMenuItemAddBox.setToolTipText("");
    jMenuItemAddBox.setText("Add Test");
    jMenuItemAddBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemAddBox_actionPerformed(e);
      }
    });
    jMenuItem3.setText("JavaScript Shell");
    jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem3_actionPerformed(e);
      }
    });
    jMenuItemSaveJS.setEnabled(false);
    jMenuItemSaveJS.setText("Save JS");
    jMenuItemSaveJS.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemSaveJS_actionPerformed(e);
      }
    });
    jMenuItemRedraw.setText("Redraw");
    jMenuItemRedraw.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemRedraw_actionPerformed(e);
      }
    });
    jMenuItemOpenJS.setText("Open JS");
    jMenuItemOpenJS.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemOpenJS_actionPerformed(e);
      }
    });
    jMenuItem4.setText("Instance Induction");
    jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem_InstanceInduction(e);
      }
    });
    jMenuItemReInduce.setText("Reinduce");
    jMenuItemReInduce.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem_ReInduce(e);
      }
    });

    jMenuItem6.setText("Clear Instances");
    jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem6_actionPerformed(e);
      }
    });
    jMenuItemReload.setText("Reload");
    if ( dataFileName == "") {
      jMenuItemReload.setEnabled(false);
    }
    jMenuItemReload.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemReload_actionPerformed(e);
      }
    });

    jMenuItemSave.setText("Save");
    if ( dataFileName == "") {
      jMenuItemSave.setEnabled(false);
    }
    jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemSave_actionPerformed(e);
      }
    });
    jMenuItemSaveXML.setText("Save XML");
    jMenuItemSaveXML.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemSaveXML_actionPerformed(e);
      }
    });
    jMenuItemOpenXML.setText("Open XML");
    jMenuItemOpenXML.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemOpenXML_actionPerformed(e);
      }
    });
    jMenuItemBackwardInduction.setText("Backward Induction");
    jMenuItemBackwardInduction.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemBackwardInduction_actionPerformed(e);
      }
    });
    jMenuItemReset.setText("UpdateKB Cache (reset)");
    jMenuItemReset.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemReset_actionPerformed(e);
      }
    });
    jMenuItemShowEventsLog.setText("Show Events Log");
    jMenuItemShowEventsLog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemShowEventsLog_actionPerformed(e);
      }
    });

    jMenuItemStartEval.setText("Start Eval Thread");
    jMenuItemStartEval.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemStartEval_actionPerformed(e);
      }
    });
    jMenuItemOneShotEval.setText("Do Evaluate (once)");
    jMenuItemOneShotEval.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemOneShotEval_actionPerformed(e);
      }
    });

    jMenuItemStopEval.setText("Stop Eval Thread");
    jMenuItemStopEval.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItemStopEval_actionPerformed(e);
      }
    });

    jMenuFile.add(jMenuItemOpenJS);
    jMenuFile.add(jMenuItemOpenXML);
    jMenuFile.add(jMenuItemReload);
    jMenuFile.add(jMenuItemSave);
    jMenuFile.add(jMenuItemSaveJS);
    jMenuFile.add(jMenuItemSaveXML);
    jMenuFile.add(jMenuItem1);
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuEdit);
    jMenuBar1.add(jMenuTool);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    jMenuTool.add(jMenuItem2);
    jMenuTool.add(jMenuItem3);
    jMenuTool.add(jMenuItem4);
    jMenuTool.add(jMenuItemBackwardInduction);
    jMenuTool.add(jMenuItemReInduce);
    jMenuTool.add(jMenuItemReset);
    jMenuTool.add(jMenuItemShowEventsLog);
    jMenuTool.add(jMenuItemStartEval);
    jMenuTool.add(jMenuItemOneShotEval);
    jMenuTool.add(jMenuItemStopEval);
    jMenuEdit.add(jMenuItemClearAll);
    jMenuEdit.add(jMenuItemAddBox);
    jMenuEdit.add(jMenuItemRedraw);
    jMenuEdit.add(jMenuItem6);

    consoleScroll = new JScrollPane(this.getZeroConsole());
    kScrollPane.setPreferredSize(new Dimension(550,480));
    consoleScroll.setPreferredSize(new Dimension(150,480));
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               kScrollPane, consoleScroll);

    splitPane.setResizeWeight(0.85);
    contentPane.add(statusBar, BorderLayout.SOUTH);
    contentPane.add(splitPane, BorderLayout.CENTER);
  }
  //File | Exit action performed
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }
  //Help | About action performed
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    AboutFrame abf = new AboutFrame(getVersion());

    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = abf.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    abf.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    abf.setVisible(true);

    this.getZeroConsole().println(this.getAboutText());
  }
  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }

  void jMenuTool_actionPerformed(ActionEvent e) {
  }

  void jMenuItemAddBox_actionPerformed(ActionEvent e) {
    KFrame froot  = new KFrame("Root",null);
    KFrame frobot = new KFrame("Robot",froot);
    froot.add(frobot);
    KFrame fuser  = new KFrame("User",froot);
    froot.add(fuser);
    KFrame fsoftware  = new KFrame("Software",froot);
    froot.add(fsoftware);

    KFrame frobot_mobile  = new KFrame("Mobile",froot);
    frobot.add(frobot_mobile);
    KFrame frobot_mobile_scout  = new KFrame("Scout",froot);
    frobot_mobile.add(frobot_mobile_scout);
    KFrame frobot_mobile_robovie  = new KFrame("Robovie",froot);
    frobot_mobile.add(frobot_mobile_robovie);

    KFrame frobot_arm  = new KFrame("Arm",froot);
    frobot.add(frobot_arm);
    KFrame frobot_arm_melfa  = new KFrame("Melfa",froot);
    frobot_arm.add(frobot_arm_melfa);

    KFrame frobot_human  = new KFrame("Humanoid",froot);
    frobot.add(frobot_human);
    KFrame frobot_human_hoap1  = new KFrame("HOAP1",froot);
    frobot_human.add(frobot_human_hoap1);
    KFrame frobot_human_pino  = new KFrame("PINO",froot);
    frobot_human.add(frobot_human_pino);

    KFrame fuser_vuthi  = new KFrame("Vuthichai",froot);
    fuser.add(fuser_vuthi);
    KFrame fuser_alamin  = new KFrame("Alamin",froot);
    fuser.add(fuser_alamin);

    KFrame fsoftware_facerec  = new KFrame("FaceRecognition",froot);
    fsoftware.add(fsoftware_facerec);
    KFrame fsoftware_feature  = new KFrame("FeatureExtraction",froot);
    fsoftware.add(fsoftware_feature);

    gotNewRoot(froot);
    repaint();
  }

  void jMenuItemClearAll_actionPerformed(ActionEvent e) {
    clearAll();
  }

  // Open Zero++ File
  void jMenuItem1_actionPerformed(ActionEvent e) {
    SPAKFileFilter filter = new SPAKFileFilter("ZERO Knowledge File: *.kb");
    filter.addExtension("kb");
    jFileChooser1.setFileFilter(filter);
    if (JFileChooser.APPROVE_OPTION == jFileChooser1.showOpenDialog(this)) {
      // Display the name of the opened directory+file in the statusBar.
      String filename = jFileChooser1.getSelectedFile().getPath();
      ZeroReader zr = new ZeroReader(filename);
      KFrame newRoot = zr.getRootNode();
      if(newRoot != null) {
        gotNewRoot(newRoot);
        statusBar.setText("Opened zerofile: "+filename);
        repaint();
      }
      else {
        statusBar.setText("Error opening zerofile: "+filename);
      }
    }
  }

  // Show Slots
  void jMenuItem2_actionPerformed(ActionEvent e) {
    Vector names = kp.getAllSlotNames();
    FrameSystemSlot fs = new FrameSystemSlot(names, kp);

    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = fs.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    fs.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    fs.setVisible(true);
  }

  // Open a JavaScript Shell Window
  void jMenuItem3_actionPerformed(ActionEvent e) {
    if(jss==null) {
      ScriptEngine se = kp.getWorkingMemory().getScriptEngine();
      jss = new JScriptShell(se);
      se.setShell(jss);

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = jss.getSize();
      if (frameSize.height > screenSize.height) {
        frameSize.height = screenSize.height;
      }
      if (frameSize.width > screenSize.width) {
        frameSize.width = screenSize.width;
      }
      jss.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
    jss.setVisible(true);
  }

  void jMenuItemSaveJS_actionPerformed(ActionEvent e) {
    try {
      SPAKFileFilter filter = new SPAKFileFilter("SPAK JS Knowledge File: *.js");
      filter.addExtension("js");
      jFileChooser1.setFileFilter(filter);

      if (JFileChooser.APPROVE_OPTION == jFileChooser1.showSaveDialog(this)) {
         String filename = jFileChooser1.getSelectedFile().getPath();
         FileOutputStream fo = new FileOutputStream(filename);
         PrintStream ps = new PrintStream(fo);
         kp.getRootNode().writeJS(ps,null);
         ps.close();
         fo.close();
         statusBar.setText("JS Saved to "+filename);
      }
    }
    catch(Exception ex) {}
  }

  /**
   * Edit/Redraw menu callback
   */

  void jMenuItemRedraw_actionPerformed(ActionEvent e) {
    kp.redrawFrame();
  }

  /**
   * Edit/ClearAll menu callback
   * Remove the opened knowledge base, and open an empty one.
   */

  void clearAll() {
    KFrame nr = new KFrame("Root",null);
    gotNewRoot(nr);
    repaint();
    dataFileName = "";
    updateTitle();
  }

  /**
   * For File/Open JS command
   */

  void jMenuItemOpenJS_actionPerformed(ActionEvent e) {

    SPAKFileFilter filter = new SPAKFileFilter("SPAK JS Knowledge File: *.js");
    filter.addExtension("js");
    jFileChooser1.setFileFilter(filter);
    if (JFileChooser.APPROVE_OPTION == jFileChooser1.showOpenDialog(this)) {
      String filename = jFileChooser1.getSelectedFile().getPath();
      loadJS(filename);
      updateTitle();
    }
  }

  private void loadJS(String filename) {
    KFrame newroot= new KFrame("Root",null);
    WorkingMemory wm = new WorkingMemory(newroot);
    wm.putJSFile(filename);
    wm.waitJSDone();
    gotNewRoot(wm.getRootFrame());
    statusBar.setText("Opened JS: "+filename);
  }

  /**
   * This should be called after a new KB root node has been installed
   * with all subnodes loaded.
   */

  private void gotNewRoot(KFrame newroot) {
    kp.setRootNode(newroot);
    if(instanceMonitor!=null)
      instanceMonitor.reset(kp.getWorkingMemory());

    // Attach the new ScriptEngine to JavaScriptShell if displayed
    if(jss!=null) {
      ScriptEngine se = kp.getWorkingMemory().getScriptEngine();
      jss.setSE(se);
      se.setShell(jss);
    }
  }

  public ZeroConsole getZeroConsole() {
    if(zeroConsole==null || (!zeroConsole.isVisible())) {
      ZeroConsole zc = new ZeroConsole();
      zeroConsole=zc;
      zc.setVisible(true);
      return zc;
    }
    return zeroConsole;
  }

  /**
   * Tool/Instance Induction Menu
   * Do forward induction
   */

  void jMenuItem_InstanceInduction(ActionEvent e) {
    ForwardChaining ic = new ForwardChaining(kp.getWorkingMemory());
    ic.setZeroConsole(getZeroConsole());
    while(ic.induce()>0) ;
    kp.redrawFrame();
  }

  /**
   * Do the backward instance induction
   * Prompt for slot values if needed.
   */

  void jMenuItemBackwardInduction_actionPerformed(ActionEvent e) {
    ForwardChaining ic = new ForwardChaining(kp.getWorkingMemory());
    ic.setZeroConsole(getZeroConsole());
    ic.induce(true);
    kp.redrawFrame();
  }

  /**
   * Tool/Instance Induction Menu
   * Do reinduce the existing instances
   */

  void jMenuItem_ReInduce(ActionEvent e) {
    ForwardChaining ic = new ForwardChaining(kp.getWorkingMemory());
    ic.setZeroConsole(getZeroConsole());
    ic.reInduce();
    kp.redrawFrame();
  }

  /**
   * Send reset command to network gateway.
   * This should be called after there is structural changes in
   * frame hierarchy or slot structure.
   */

  void jMenuItemReset_actionPerformed(ActionEvent e) {
    kp.getWorkingMemory().rereadRoot();
  }

  /**
   * Start the Evaluator Thread which will check the onEvaluate
   * slot of every instances and execute them every an interval
   * of time (set by Evaluator::setPeriod(int milliseconds)).
   */

  void jMenuItemStartEval_actionPerformed(ActionEvent e) {
    kp.getWorkingMemory().startEval();
  }

  void jMenuItemOneShotEval_actionPerformed(ActionEvent e) {
    kp.getWorkingMemory().oneShotEval();
  }

  /**
   * Show the window containing event log
   */
  void jMenuItemShowEventsLog_actionPerformed(ActionEvent e) {
    System.out.println("Events log:");
    System.out.println(kp.getWorkingMemory().getEventsLog());
    if ( evw == null) {
      evw = new EventsLogWindow(kp);
      evw.setTitle("Events Log");
      evw.setSize(300,300);
      //Center the window

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = evw.getSize();
      if (frameSize.height > screenSize.height) {
        frameSize.height = screenSize.height;
      }
      if (frameSize.width > screenSize.width) {
        frameSize.width = screenSize.width;
      }

      evw.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
    evw.setVisible(true);
    evw.update();
  }


  /**
   * Stop the Evaluator Thread
   */

  void jMenuItemStopEval_actionPerformed(ActionEvent e) {
    kp.getWorkingMemory().stopEval();
  }

  /**
   * Launch the network gateway
   */

  private void launchInstanceMonitor() {
    if(instanceMonitor==null) {
      instanceMonitor = new NetworkMonitor(kp.getWorkingMemory());
      instanceMonitor.setZeroConsole(getZeroConsole());
      instanceMonitor.start(9900);
    }
  }

  /**
   * Menu: Edit/Clear Instance
   */

  void jMenuItem6_actionPerformed(ActionEvent e) {
    kp.getWorkingMemory().removeInstances();
    kp.redrawFrame();
  }

  /******************************************************************/
  /* DRAG & DROP: Handle Dropping File                              */
  /******************************************************************/

  public void dragEnter(DropTargetDragEvent e) {
    e.acceptDrag(DnDConstants.ACTION_COPY);
    // System.out.println("DragEnter");
  }

  public void dragOver(DropTargetDragEvent e) {
    e.acceptDrag(DnDConstants.ACTION_COPY);
    // System.out.println("DragOver");
  }

  public void dragExit(DropTargetEvent e) {
    repaint();
    // System.out.println("DragExit");
  }

  public void drop(DropTargetDropEvent dtde) {
    String filename = null;
    System.out.println("Drag-Drop");
    DropTargetContext dtc =
                dtde.getDropTargetContext();
    boolean outcome = false;

    if ((dtde.getSourceActions()
                & DnDConstants.ACTION_COPY) != 0) {
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      System.out.println("AcceptDrop");
    }
    else {
      dtde.rejectDrop();
      System.out.println("RejectDrop");;
    }

    DataFlavor[] dfs = dtde.getCurrentDataFlavors();
    DataFlavor   tdf = null;

    for (int i = 0; i < dfs.length; i++) {
      System.out.println("Flavor "+dfs[i].toString());
      if (DataFlavor.javaFileListFlavor.equals(dfs[i])) {
        tdf = dfs[i];
        break;
      }
    }

    if (tdf != null) {
      Transferable t  = dtde.getTransferable();
      java.util.List fileList = null;

      try {
        fileList = (java.util.List)t.getTransferData(tdf);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        dtc.dropComplete(false);

        return;
      } catch (UnsupportedFlavorException ufe) {
        ufe.printStackTrace();
        dtc.dropComplete(false);

        repaint();
        return;
      }

      if (fileList != null) {
        try {
          File myFile = (File)fileList.get(0);
          filename = myFile.toString();
          Pattern pJS = Pattern.compile("\\.(xml|js)$",Pattern.CASE_INSENSITIVE);
          if(!pJS.matcher(filename).find())
            throw new Exception();
          outcome = true;
        }
        catch(Exception e) {
          statusBar.setText("Cannot open dropped file "+filename);
        }
      }
    }
    repaint();
    dtc.dropComplete(outcome);

    if(outcome) {
      if (filename.indexOf(".xml") >= 0) {
        dataFileName = filename;
        loadXML(filename);
        updateTitle();
      }
      else {
        loadJS(filename);
        updateTitle();
      }
    }
  }

  public void dragScroll(DropTargetDragEvent e) {
    System.out.println("dragScroll");
  }

  public void dropActionChanged(DropTargetDragEvent e) {
    System.out.println("dropActionChanged");
  }

  /* Process Mouse Events in the Knowledge Panel
     :- Dragging
  */

  public void mouseClicked(MouseEvent e) {
  }
  public void mouseReleased(MouseEvent e) {
  }
  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
  }

  int dragStartX=0, dragScrollX=0;  //Screen Co-Ordinate & Scroll Position
  int dragStartY=0, dragScrollY=0;  //at the start of Dragging

  public void mousePressed(MouseEvent e) {
    dragStartX=e.getX(); // Canvas Co-Ordinate
    dragStartY=e.getY();
    dragScrollX=kScrollPane.getHorizontalScrollBar().getValue();
    dragScrollY=kScrollPane.getVerticalScrollBar().getValue();
    dragStartX-=dragScrollX; // Real Screen Co-Ordinate
    dragStartY-=dragScrollY; // Real Screen Co-Ordinate
  }

  /* Following two functions are from MouseMotionListener */
  public void mouseDragged(MouseEvent e) {
    int x=e.getX()-kScrollPane.getHorizontalScrollBar().getValue();
    int y=e.getY()-kScrollPane.getVerticalScrollBar().getValue();
    // System.out.println("Dif "+(x-dragStartX)+" "+(y-dragStartY));
    kScrollPane.getHorizontalScrollBar().setValue(dragScrollX-x+dragStartX);
    kScrollPane.getVerticalScrollBar().setValue(dragScrollY-y+dragStartY);
  }

  public void mouseMoved(MouseEvent e) {
  }

  /**
   * Save Knowledge Content in XML format
   */

  void jMenuItemSaveXML_actionPerformed(ActionEvent e) {
    try {
      SPAKFileFilter filter = new SPAKFileFilter("SPAK XML Knowledge File: *.xml");
      filter.addExtension("xml");
      jFileChooser1.setFileFilter(filter);

      if (JFileChooser.APPROVE_OPTION == jFileChooser1.showSaveDialog(this)) {
         String filename = jFileChooser1.getSelectedFile().getPath();
         kp.getRootNode().writeXMLFile(filename);
         statusBar.setText("XML Saved to "+filename);
         dataFileName = filename;
         updateTitle();
      }
    }
    catch(Exception ex) {
      System.err.println("Exception while saving XML file: "+ex.toString());
    }
  }

  /**
   * Save Knowledge Content (in case of previously saved in XML format)
   */

  void jMenuItemSave_actionPerformed(ActionEvent e) {
    try {
      if (dataFileName == "" ) {
        jMenuItemSaveXML_actionPerformed(e);
      } else {
        kp.getRootNode().writeXMLFile(dataFileName);
        statusBar.setText("XML Saved to "+dataFileName);
      }
    }
    catch(Exception ex) {}
  }

  /**
   * File/OpenXML Menu Command
   */

  void jMenuItemOpenXML_actionPerformed(ActionEvent e) {
    SPAKFileFilter filter = new SPAKFileFilter("SPAK XML Knowledge File: *.xml");
    filter.addExtension("xml");
    jFileChooser1.setFileFilter(filter);
    if (JFileChooser.APPROVE_OPTION == jFileChooser1.showOpenDialog(this)) {
      // Display the name of the opened directory+file in the statusBar.
      String filename = jFileChooser1.getSelectedFile().getPath();
      dataFileName = filename;
      loadXML(filename);
      updateTitle();
    }
  }

  /**
   * Reload the existing XML file
   */

  void jMenuItemReload_actionPerformed(ActionEvent e) {
    if ( dataFileName == null ) {
      System.out.println("Error, dataFileName is null");
    } else {
      loadXML(dataFileName);
    }
  }

  private void loadXML(String filename) {
    XMLReader zr = new XMLReader();
    zr.parseFile(filename);
    KFrame newRoot = zr.getRootNode();
    if(newRoot != null) {
      gotNewRoot(newRoot);
      statusBar.setText("Opened XML file: "+filename);

      jMenuItemReload.setEnabled(true);
      repaint();
    }
    else {
      statusBar.setText("Error opening XML file: "+filename);
    }
  }

  public static String getAboutText() {
    StringBuffer sb = new StringBuffer();
    sb.append("*** SystemInfo ");
    sb.append("\n");
    sb.append("VM Version : "+System.getProperty("java.version"));
    sb.append("\n");
    sb.append("VM Vendor  : "+System.getProperty("java.vendor"));
    sb.append("\n");
    sb.append("OS Name    : "+System.getProperty("os.name"));
    sb.append("\n");
    sb.append("OS Arch    : "+System.getProperty("os.arch"));
    sb.append("\n");
    sb.append("OS Version : "+System.getProperty("os.version"));
    sb.append("\n");
    sb.append("FreeMemory : "+Runtime.getRuntime().freeMemory());
    sb.append("\n");
    sb.append("MaxMemory  : "+Runtime.getRuntime().maxMemory());
    sb.append("\n");
    sb.append("TotalMemory: "+Runtime.getRuntime().totalMemory());
    sb.append("\n");
    sb.append("#CPUs      : "+Runtime.getRuntime().availableProcessors());
    sb.append("\n");
    sb.append("WorkingDir : "+System.getProperty("user.dir"));
    sb.append("\n");
    sb.append("***");
    sb.append("\n");
    return sb.toString();
  }

  private void updateTitle() {
    String filename = "";
    jMenuItemSave.setEnabled(false);
    if(this.dataFileName!=null) {
      if(dataFileName != "") {
        File f = new File(dataFileName);
        filename = ": "+f.getName();
        jMenuItemSave.setEnabled(true);
      }
    }
    this.setTitle("SPAK Version "+getVersion()+filename);
  }
}
