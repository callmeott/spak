package spak;
import java.util.*;
import org.mozilla.javascript.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

/**
 * Working Memory stores temporary data for processing
 * One loaded knowledge file will have one working memory.
 *
 * To avoid resource contention, before doing actions that can
 * change the data (e.g., induce, reinduce), one must call wm.lock
 * and wm.unlock beforehands (Any better way to enforce this?).
 */
public class WorkingMemory {
  KFrame rootFrame = null;
  Vector depthList = new Vector();

  // Hash value of frame-slot=value data received during operation
  Hashtable mySlots = new Hashtable();
  int maxdepth = -1;

  // Hash value of all existing instances
  // hashvalue -> instance
  Hashtable instanceHash = new Hashtable();
  Hashtable conditionFailedHash = new Hashtable();
  ZeroConsole zc=null;
  ScriptEngine se = null;
  Evaluator ev = null;
  Vector eventsLog = new Vector();
  private KFrame DummyFrame = new KFrame("dummy",null);

  // prevent race conditions
  private boolean inuse = false;

  public WorkingMemory(KFrame rn) {
    rootFrame = rn;
    rootFrame.setWorkingMemory(this);
    buildFrameInfo(rootFrame,0);
    DummyFrame.setDummy(true);
//    if(se!=null && se.isAlive())
//      se.setStopme();

    se = new ScriptEngine(rootFrame);
    se.start();
  }

  public synchronized void lock() {
    while (inuse == true) {
      try {
        wait();
      } catch (InterruptedException e) { }
    }
    //System.out.println("WM is locked");
    inuse = true;
  }

  public synchronized void unlock() {
    //System.out.println("WM is UNlocked");
    inuse = false;
    notifyAll();
  }

  /*******************************************
   * JavaScript Script Engine related methods
   *******************************************
   */

  /**
   * Return the current script engine object
   */
  public ScriptEngine getScriptEngine() {
    return se;
  }

  /**
   * Put the given JavaScript command in the execution queue
   * the queue will be read and processed by the SE Thread
   */

  public void putJSCommand(String cmd) {
    se.putCommand(cmd);
  }

  /**
   * Execute commands in the given JavaScript file
   */

  public void putJSFile(String filename) {
    se.putSource(filename);
  }

  /**
   * Wait until the JS command put earlier (by putJSCommand())
   * complete execution.
   */

  public void waitJSDone() {
    se.waitDone();
  }

  /**
   * Return the result of executed JS command. Usually, called after
   * putJSCommand and waitJSDone.
   */

  public String getJSResult() {
    return se.getResult();
  }

  /**
   * Immediately execute the JS command using the current Thread
   * Usually this is called from inside the SE Thread itself.
   */

  public void execJSCommand(String cmd) {
    se.processOneLine(cmd);
  }

  /**
   * Introduce a new variable into JavaScript namespace, that variable
   * called "varname" is mapped to KFrameScript object "obj"
   */

  public void putJSVariable(String varname, Scriptable obj) {
    se.putVar(varname, obj);
  }

  /**
   * Introduce a new variable into JavaScript namespace, that variable
   * called "varname" is mapped to KFrame object "fr"
   */

  public void putJSVariable(String varname, KFrame fr) {
    se.putVar(varname, fr);
  }

  public KFrame getRootFrame() {
    return rootFrame;
  }

  /**
   * Check if frame is already in "depthlist"
   * Return: depth level
   */
  public int inDepthList(KFrame frame) {
    for(int i=0;i<=maxdepth;i++)
      if(((Vector)depthList.elementAt(i)).contains(frame))
        return i;
    return -1;
  }

  public void buildFrameInfo(KFrame frame, int depth) {
    if(depth>maxdepth) {
      maxdepth = depth;
      depthList.add(new Vector());
    }
    Vector sub = frame.getChildren();
    for(int i=0;i<sub.size();i++) {
      KFrame kf = (KFrame)sub.elementAt(i);
      if(!kf.isInstance())
        buildFrameInfo(kf,depth+1);
    }
    int indepth = inDepthList(frame);
    if(indepth < 0)
      ((Vector)depthList.elementAt(depth)).add(frame);
    else {
      if(depth > indepth) {
        ((Vector)depthList.elementAt(depth)).add(frame);
        ((Vector)depthList.elementAt(indepth)).remove(frame);
      }
    }
  }

  public void reset(KFrame newroot) {
    rootFrame = newroot;
    rootFrame.setWorkingMemory(this);
    mySlots.clear();
    instanceHash.clear();
    conditionFailedHash.clear();
    rereadRoot();
    out("WorkingMemory Hard Reset");

  }

  /**
   * After the knowledge hierarchy has been changed. This function should
   * be called.
   */

  public void rereadRoot() {
    depthList.removeAllElements();
    maxdepth = -1;
    buildFrameInfo(rootFrame,0);
    out("WorkingMemory resets Knowledge Hierarchy");
  }

  protected void out(String s) {
    if(zc!=null)
      zc.println(s);
  }

  public void setZeroConsole(ZeroConsole zcon) {
    zc = zcon;
  }

  public ZeroConsole getZeroConsole() {
    return zc;
  }

  /**
   * Remove all instances in the KB.
   */
  public void removeInstances() {
    rootFrame.Obsolete_removeInstance();
    instanceHash.clear();
  }

  /**
   * Remove the specified instance (frame and its subframes)
   * from WM and update those frames who are using me
   */
  public void removeInstance(KFrame inst) {
    Vector instances = collectInstances();
    Vector tobeUpdatedList = new Vector();
    String instname = inst.getName();
    boolean changed = false;

    Hashtable useList = collectUseList(instances);

    if(useList.containsKey(inst))
      tobeUpdatedList.addAll((Vector)useList.get(inst));

    for (int i = 0;i< tobeUpdatedList.size();i++) {
      // make changes to slots of those who use me
      KFrame item = (KFrame) tobeUpdatedList.elementAt(i);
      Vector slotlist = item.getSlotList().getSlotVector();
      for (int j=0;j<slotlist.size();j++) {
        Slot slotitem = (Slot) slotlist.elementAt(j);
        if ( slotitem.getValue().equals(instname)) {
          changed = true;
          try {
            slotitem.setValue("");
            System.out.println("Update slot "+slotitem.getName()+ " of frame "+item.getName());
          }
          catch (InvalidSlotValueException e) {
            System.err.println("Exception when emptying slot value " +
                               slotitem.getName());
          }
        }
      }
    }

    // now do remove meself
    if (instanceHash.containsValue(inst))
      instanceHash.remove(inst.getParamHash());
    else {
      System.err.println("WorkingMemory::removeInstances, Unknown paramHash: " + inst.getParamHash());
      System.err.println("HashSize "+instanceHash.size());
    }
    inst.parentsRemove();  // Ask all parents to remove me
    inst.childrenRemove(); // Ask all children to remove me
   }

  /**
   * Get Vector of Frames sorted by Depth (deepest frames first)
   */
  public Vector getFrameOrder() {
    Vector retval = new Vector();
    for(int i=maxdepth;i>=0;i--) {
      Vector dl = (Vector)depthList.elementAt(i);
      retval.addAll(dl);
    }
    return retval;
  }

  class PriorityComp implements Comparator  {
      public int compare(Object e1, Object e2) {
          KFrame k1 = (KFrame) e1;
          KFrame k2 = (KFrame) e2;
          int p1,p2;
          if ( k1.getSlotValue("priority") == null ||
               k1.getSlotValue("priority") == "" ) {
              p1 = 0;
          }
          else {
              p1 = Integer.parseInt(k1.getSlotValue("priority"));
          }
          if ( k2.getSlotValue("priority") == null ||
               k2.getSlotValue("priority") == "" ) {
              p2 = 0;
          }
          else {
              p2 = Integer.parseInt(k2.getSlotValue("priority"));
          }
          if (p1 > p2) {
            return -1;
          }
          else if (p1 == p2 ) {
            return 0;
          }
          else {
            return 1;
          }
      }
  }

  /**
   * Get Vector of Frames ordered by 1) pririty, 2) depth (deepest frames first)
   */
  public Vector getFrameOrderPriority() {
    Vector retval = getFrameOrder();
    PriorityComp m = new PriorityComp();
    Collections.sort(retval, m);
    //System.out.println("getFrameOrderPriority called...");
    //for (int i = 0; i < retval.size();i++) {
    //  KFrame tmp = (KFrame) retval.elementAt(i);
    //  System.out.println( tmp.getSlotValue("Name"));
    //}
    return retval;
  }

  public Vector getInstances() {
    Vector retval = new Vector();
    Hashtable fromhash = getInstanceHash();
    //System.out.println("wm.getInstances called:\n"+fromhash.toString());
    for (Enumeration e = fromhash.elements() ; e.hasMoreElements() ;) {
      KFrame fr = (KFrame) (e.nextElement());
      retval.add(fr);
    }
    // sort by priority
    PriorityComp m = new PriorityComp();
    Collections.sort(retval, m);
    return retval;
  }

  public Vector getInstanceNames() {
    Vector result = new Vector();
    Vector instances = getInstances();
    for (int i = 0;i < instances.size();i++) {
      result.add(((KFrame)instances.elementAt(i)).getName());
    }
    return result;
  }

  public void removeInstancesOfFrame(String frame) {
    Vector instances = collectInstances();
    Vector removeList = findInstancesOf(frame);

    // We may end up removing the same Instance several times but
    // this causes no serious problem...
    Hashtable useList = collectUseList(instances);
    while(!removeList.isEmpty()) {
      KFrame item = (KFrame)removeList.remove(0);
      if(useList.containsKey(item))
        removeList.addAll((Vector)useList.get(item));
      item.parentsRemove();  // Ask all parents to remove me
      item.childrenRemove(); // Ask all children to remove me
      instanceHash.remove(item.getParamHash());
    }
  }

  /**
   * Walk thru all the instances, and come up with a used-by
   * instance relationship Map:instance->instance
   */

  public Hashtable collectUseList(Vector instances) {
    Hashtable map = new Hashtable();
    Hashtable nameMap = new Hashtable();

    // Fill up the name->object map
    for(int i=0;i<instances.size();i++) {
      KFrame ins = (KFrame)instances.elementAt(i);
      nameMap.put(ins.getName(),ins);
    }

    for(int i=0;i<instances.size();i++) {
      KFrame ins = (KFrame)instances.elementAt(i);
      SlotList sll = ins.getSlotList();
      for(int j=0;j<sll.size();j++) {
        Slot sl = sll.slotAt(j);
//        if(sl.getType()==Slot.TYPE_INSTANCE) {
        if(sl.getType()==Slot.TYPE_INSTANCE && ! sl.requiredAtBeginningOnly) {
          KFrame targetFrame = (KFrame)nameMap.get(sl.getValue());
          Vector usedby;
          if(targetFrame != null) {
            if(map.containsKey(targetFrame))
              usedby = (Vector)map.get(targetFrame);
            else {
              usedby = new Vector();
              map.put(targetFrame, usedby);
            }
            usedby.add(ins);
          }
        }
      } //Slot Loop
    } //Instance Loop
    return map;
  }

  public Vector findInstancesOf(String name) {
    return rootFrame.findInstancesOf(name);
  }

  public Vector collectInstances() {
    return rootFrame.Obsolete_collectInstances();
  }

  /**
   * Enter new frame-slot=value to the working memory
   * @param key frame-slot
   * @param data value
   */
  public void mySlotsPut(String key, String data) {
    mySlots.put(key, data);

    Date d = new Date();
    String log = d.getTime()+":"+key+"="+data;
    eventsLog.add(log);
  }

  public boolean mySlotsContainKey(String key) {
    return mySlots.containsKey(key);
  }

  public String mySlotsGet(String key) {
    return (String)mySlots.get(key);
  }

  public void mySlotsRemove(String key) {
    mySlots.remove(key);
  }

  public String printMySlots() {
    StringBuffer retval = new StringBuffer();
    Enumeration en = mySlots.keys();
    while(en.hasMoreElements()) {
      String key = (String) en.nextElement();
      retval.append(key);
      retval.append("=");
      retval.append((String)mySlots.get(key));
      retval.append("\n");
    }
    return retval.toString();
  }

  public void clearMySlots() {
    mySlots.clear();
  }

  public Hashtable getInstanceHash() {
    return instanceHash;
  }

  public Hashtable getConditionFailedHash() {
    return conditionFailedHash;
  }

  /**
   * Start the Evaluator Thread which will check the onEvaluate
   * slot of every instances and execute them every an interval
   * of time (set by Evaluator::setPeriod(int milliseconds)).
   */
  public void startEval() {
    ev = new Evaluator(rootFrame, this);
    ev.start();
  }

  /**
    * Start the Evaluator Thread, run once and exit
    */
   public void oneShotEval() {
     Evaluator myev = new Evaluator(rootFrame, this);
     myev.doEvaluate();
   }

  /**
   * Stop the Evaluator Thread
   */
  public void stopEval() {
    if (ev != null) ev.stopRun();
  }

  /**
   * Get the event log
   */
  public String getEventsLog() {
    String output = new String();
    for (int i = 0; i < eventsLog.size(); i++) {
      output += (String) eventsLog.elementAt(i)+"\n";
    }
    return output;
  }

  /**
   * reset the event log
   */
  public void resetEventsLog() {
    eventsLog.clear();
  }

  /**
   * Return the dummyframe object for this working memory
   */
  public KFrame getDummyFrame() {
    return this.DummyFrame;
  }
}