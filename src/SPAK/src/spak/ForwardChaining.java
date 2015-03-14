package spak;
import java.util.*;
import javax.swing.*;

/**
 * <p>Title: ForwardChaining</p>
 * <p>Description: Scan frames hierarchy and see if we can instantiate an
 * instance (forward-chaining). Process the induce() request.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: NII</p>
 * @author Vuthichai
 * @version 1.0
 */

public class ForwardChaining {
  WorkingMemory wm = null;
  Vector mySlotsMarkRemove = null; // For marking used slots
  ZeroConsole zc=null;
  Hashtable seenInstanceHash = null;
  Hashtable conditionFailedHash = null; // keep the full hash forever

  private Hashtable tmpFailedCondition = null; // keep only short hash when trying each frame
  private Vector tmpAddedToMySlots = null;   // what I added to wm.mySlots when trying
  private KFrame DummyFrame = null;

  public ForwardChaining(WorkingMemory x) {
    wm = x;
    this.DummyFrame = wm.getDummyFrame();
    mySlotsMarkRemove = new Vector();
    seenInstanceHash = wm.getInstanceHash();
    conditionFailedHash = wm.getConditionFailedHash();
    tmpAddedToMySlots = new Vector();
  }

  public void reset(WorkingMemory newwm) {
    wm = newwm;
    this.DummyFrame = wm.getDummyFrame();
    mySlotsMarkRemove.removeAllElements();
    seenInstanceHash = wm.getInstanceHash(); // Bug Fix: Added 2003-12-02
    conditionFailedHash = wm.getConditionFailedHash();
    wm.setZeroConsole(zc);
  }

  public void setZeroConsole(ZeroConsole zcon) {
    zc = zcon;
    wm.setZeroConsole(zc);
  }

  /**
   * Trying to create frame instances whose slot requirement matches the
   * slots in mySlots.
   */
  public int induce() {
    return induce(false);
  }

  /* doAsk: Ask for missing slot ? */
  public synchronized int induce(boolean doAsk) {
    wm.lock();
    int count=0;
    String paramHash;
    Vector dl = wm.getFrameOrderPriority();
    tmpFailedCondition = new Hashtable();
    int conditionFailedHash_size = 0;

    //System.out.println("Total "+dl.size()+" frames to try...");
    System.out.println("Induce: with wm's slots contents\n"+wm.mySlots);
    for(int j=0;j<dl.size();j++) {
      KFrame frame=(KFrame)dl.elementAt(j);
      //System.err.println("Induce -- Trying "+j+": "+frame.getName());
      //System.out.println("WM Slots: "+wm.printMySlots());
      String oldParamHash = null; // to prevent loop
      conditionFailedHash_size = conditionFailedHash.size();

      mySlotsMarkRemove.removeAllElements();

      while((paramHash=tryFrame(frame, doAsk))!=null ) {
        oldParamHash = paramHash;
        System.out.println("Passed tryFrame "+frame.getName()+", result="+paramHash);
        mySlotsMarkRemove.removeAllElements();
        KFrame inst = frame.createInstance(false, false);
        fillInstanceSlotValues(inst);
        inst.setParamHash(paramHash);
        seenInstanceHash.put(paramHash, inst);

        if( ! conditionFailedHash.containsKey(inst.getParamHash_AllSlots()) &&
            inst.checkCondition()) { // Check "condition" and execute "onTry" slots
          out("Got "+frame.getName());

          // mark who I depend on
          inst.updateDependency();

          count++;
          removeMarkedMySlots();

          // now run the onInstantiate slot
          inst.runSpecialSlot("onInstantiate");

          if(doAsk)
            if(JOptionPane.showConfirmDialog(null,"Continue backward chaining ?",
                "Confirmation", JOptionPane.YES_NO_OPTION)>0)
              doAsk=false;  // Don't return yet, just repeat to use up all slot values
        }
        else {
          System.out.println("Condition failed...");
          frame.removeInstance(inst);
          seenInstanceHash.remove(paramHash);
          tmpFailedCondition.put(paramHash, "-");
          if (! conditionFailedHash.containsKey(inst.getParamHash_AllSlots()))
              conditionFailedHash.put(inst.getParamHash_AllSlots(), "-");
          inst.freeInstancesInUse();
          for (int i=0; i< tmpAddedToMySlots.size(); i++) {
            wm.mySlotsRemove((String) tmpAddedToMySlots.elementAt(i));
          }
        }
      }
      // if there is change to conditionFailedHash then we should not
      // return 0, so that it will do the induce again;
      if ( conditionFailedHash_size != conditionFailedHash.size() ) {
        count++;
      }
    }
    System.err.println("induce: returning "+count);
    wm.unlock();
    return count;
  }

  /**
   * Remove all instances of Frame, and of its children frames.
   */
  public void removeInstancesOfFrame(String frame) {
    wm.lock();
    wm.removeInstancesOfFrame(frame);
    wm.unlock();
  }

  /**
   * Dump Instance name and its slot values of instance matching mySlots
   * called when use enters "show" command
   */
  public String dumpInstances() {
    Vector instances = wm.collectInstances();
    String retval = new String();
    Vector matchList = new Vector();

    for(int i=0;i<instances.size();i++) {
      KFrame item = (KFrame) instances.elementAt(i);
      // System.out.println("Try removing: "+item.getName());
      Hashtable allHash = item.getAllSlotFullnames();
      Enumeration en = allHash.keys();
      boolean match = true;
      mySlotsMarkRemove.removeAllElements();

      while(en.hasMoreElements()) {
        String sname = (String)en.nextElement();
        String fname = (String)allHash.get(sname);
        Slot sl = item.getSlot(sname);
        if(sl.getRequired() &&
           (!sl.getValue().equals(wm.mySlotsGet(fname)))) {
          match=false;
          break;
        }
        mySlotsMarkRemove.add(fname);
      }
      if(match) {
        matchList.add(item);
        removeMarkedMySlots();
      }
    }

    while(!matchList.isEmpty()) {
      KFrame frame = (KFrame)matchList.remove(0);
      retval += frame.getName()+"\n";

      for(int i=0;i<frame.getSlotList().size();i++) {
        Slot sl = frame.getSlotList().slotAt(i);
        retval += (sl.getRequired()?" *":"  ")+sl.getName()+"="+sl.getValue()+"\n";
      }
    }
    return retval;
  }

  /**
   * Doing the reverse of induce(). Removing all instances invalidated by the
   * removals of slots in mySlots.
   */
  public void removeInstances() {
    wm.lock();
    Vector instances = wm.collectInstances();

    // Now find the first Instance to remove
    Vector removeList = new Vector();
    for(int i=0;i<instances.size();i++) {
      KFrame item = (KFrame) instances.elementAt(i);
      // System.out.println("Try removing: "+item.getName());
      Hashtable allHash = item.getAllSlotFullnames();
      Enumeration en = allHash.keys();
      boolean match = true;
      mySlotsMarkRemove.removeAllElements();

      while(en.hasMoreElements()) {
        String sname = (String)en.nextElement();
        String fname = (String)allHash.get(sname);
        Slot sl = item.getSlot(sname);
        if(sl.getRequired() &&
           (!sl.getValue().equals(wm.mySlotsGet(fname)))) {
          match=false;
          break;
        }
        mySlotsMarkRemove.add(fname);
      }
      if(match) {
        removeList.add(item);
        removeMarkedMySlots();
      }
    }

    // We may end up removing the same Instance several times but
    // this causes no serious problem...
    Hashtable useList = wm.collectUseList(instances);
    while(!removeList.isEmpty()) {
      KFrame item = (KFrame)removeList.remove(0);
      if(useList.containsKey(item))
        removeList.addAll((Vector)useList.get(item));
      item.parentsRemove();
      item.childrenRemove();
      seenInstanceHash.remove(item.getParamHash());
    }
    wm.unlock();
  }

  private void removeMarkedMySlots() {
    for(int i=0;i<mySlotsMarkRemove.size();i++)
      wm.mySlotsRemove((String)mySlotsMarkRemove.elementAt(i));
  }

  /**
   * Return string showing all Frames and required Slots in the KB
   */
  public String dumpFrameSlot() {
    String retval = new String();
    Vector dl = (Vector)wm.getFrameOrder();
    for(int j=0;j<dl.size();j++) {
      KFrame frame=(KFrame)dl.elementAt(j);
      retval+="Frame: "+frame.getName()+"\n";
      Hashtable allHash = frame.getAllSlotFullnames();
      Enumeration en = allHash.keys();
      while(en.hasMoreElements()) {
        String sname = (String)en.nextElement();    // Short Name
        String fname = (String)allHash.get(sname);  // Full Name
        // fname is in the format FRAME-SLOT
        int dashloc = fname.indexOf("-");
        if(wm.getRootFrame().findFrame(fname.substring(0,dashloc)).getSlot(sname).getRequired())
          retval+=("  ")+fname+" *\n";
        else
          retval+=("  ")+fname+"\n";
      }
    }
    retval += "--END--\n";
    return retval;
  }

  private void  fillInstanceSlotValues(KFrame inst) {
    Hashtable allHash = inst.getAllSlotFullnames();  // Slots to fill
    Enumeration en = allHash.keys();  // Short Names
    while(en.hasMoreElements()) {
      String sname = (String)en.nextElement();    // Short Name
      String fname = (String)allHash.get(sname);  // Full Name
      if(wm.mySlotsContainKey(fname)) {
        inst.setSlotValue(sname, wm.mySlotsGet(fname), false);
        // out("Fill Slot: "+sname+" with "+(String)mySlots.get(fname));
        // Mark Remove from current working memory
        mySlotsMarkRemove.add(fname);
      }
    }
  }

  // Need to implement onUpdate() here? No, handled elseware
  // Should we check if new values satisfy condition ? No, let reInduce do its job
  private void  updateInstanceSlotValues(KFrame inst) {
    Hashtable allHash = inst.getAllSlotFullnames();  // Slots to fill
    Enumeration en = allHash.keys();  // Short Names
    while(en.hasMoreElements()) {
      String sname = (String)en.nextElement();    // Short Name
      String fname = (String)allHash.get(sname);  // Full Name
      if(wm.mySlotsContainKey(fname)) {
        inst.setSlotValue(sname, wm.mySlotsGet(fname),false);
        //System.out.println("Fill Slot: " + sname + " with " + fname + " and remove it from WM");
        // Mark Remove from current working memory
        mySlotsMarkRemove.add(fname);
      }
    }
    removeMarkedMySlots();
    out(inst.getName()+" updated");
  }

  protected void addSlotValue(String name, String value) {
    wm.mySlotsPut(name,value);
  }

  /**
   * Check each instance, regenerate its hash and call
   * reInduceFrame() for each frame
   * @return number of reinduced instances
   */
  public int reInduce() {
    wm.lock();
    int count=0;
    Date d = new Date();
    System.out.println("Reinduce run at Time:" + d.getTime());
    Vector instances = wm.getInstances();
    System.out.println("Found total " + instances.size() + " instances: ");
    for (int i = 0; i < instances.size(); i++) {
      KFrame fr = (KFrame) (instances.elementAt(i));
      if ( fr.getSlotValue("Name") == "dummy" ) {
        System.err.println("Skipping dummy");
      } else {
        System.out.println("Examining " + fr.getSlotValue("Name"));
        while (count < 20 && reInduceFrame(fr) != 0) count++;
        if ( count == 20 )
          out("Too many reInduce() occurred, maybe endless loop");
        fr.redrawPanel();
      }
    }
    wm.unlock();
    return count;
  }

  /**
   * 1. Check if the instance frame fr should still be a child of its parent frame(s),
   * if not then remove the parent that the condition doesn't match anymore. If no more
   * parent then move it to be a child of Root
   *
   * 2. Check if it should be a child of other frames and update it as needed
   * @param fr the frame to be reInduced
   * @return -1 for error, 0 for no change, 1 if reinduce has activity
   */
  public int reInduceFrame(KFrame fr) {
    if (fr == null) {
      System.err.println("Error: can't reInduceFrame null.");
      return -1;
    }
    if (fr.getParentName() == "" || fr.getParentName() == null) {
      System.err.println("Null parent name, stop reInducing.");
      return -1;
    }

    // FIXME sort by priority
    Vector myparents = fr.getParentFrames();
    Vector possiblechoices = new Vector();
    Vector morespecialized = new Vector();
    Vector failedframes = new Vector();
    boolean singleparent = false;

    // everything is possible
    possiblechoices.addAll(wm.getRootFrame().getAllChildren());

    // check condition
    int parentcount = fr.parents.size();
    if (parentcount == 1)
      singleparent = true;

    for(int i=0;i< fr.parents.size();i++) {
      KFrame tmpparent = (KFrame) fr.parents.elementAt(i);
      if (! (tmpparent.checkSlotCondition(fr)
             && fr.checkCondition(tmpparent))) {
        System.out.println("Warning: condition is now invalid for parent " +
                           tmpparent.getName());
        //System.out.println("Dumping slots\n"+fr.getKFrameScript());
        // leaving this parent branch
        out("Condition failed, removing parent "+tmpparent.getName());
        fr.runSpecialSlot("onTransition", tmpparent);
        fr.removeParent(tmpparent);

        possiblechoices.removeElement(tmpparent); // otherwise loop
        parentcount--;
      } else {
        //System.out.println("Condition is still valid, proceed further...");
      }
    }
    // it is possible that the code in onTransition already killed myself
    // so check it!
    if (! wm.getInstanceHash().containsValue(fr)) {
      // do nothing, I don't exist anymore
      System.out.println("Found no hash of myself in WM, stop reInduceFrame()");
      return 0;
    }

    if (parentcount == 0) {
      if ( singleparent && fr.getSlotValue("needfirstparent") != null &&
           fr.getSlotValue("needfirstparent") != "" ) {
        // first parent must exist, I must die now
        System.out.println("needfirstparent special slot is set and I have no more parent now, deleting myself ");
        fr.selfDelete();
        return 0;
      } else {
        System.out.println("No more parent left, change my parent to Root!");
        fr.setToNewParent(wm.getRootFrame());
      }
    }

    // condition is null OR still valid
    //System.out.println("Try if it can be also a child of other frames...");
    int count = 0;
    String paramHash;

    // remove what I already am
    //System.out.println("possiblechoices are "+possiblechoices);
    for (int i = 0; i < possiblechoices.size(); i++) {
      //System.out.println("checking "+((KFrame) possiblechoices.elementAt(i)).getName());
      KFrame tmpcandidate = (KFrame) possiblechoices.elementAt(i);
      if (  tmpcandidate.isInstance() || fr.inParentList(tmpcandidate) ) {
        //System.out.println("Removing "+ ( (KFrame) possiblechoices.elementAt(i)).getName()+ " from possiblechoices");
        possiblechoices.removeElementAt(i);
        i--;
      }
    }

    // morespecialized should include all my parent frames's children
    for (int i = 0; i < myparents.size(); i++) {
      KFrame tmpparent = (KFrame) myparents.elementAt(i);
      if ( tmpparent.getName() != "Root" ) {
        for (int j=0; j< tmpparent.parents.size();j++) {
          Vector children = ((KFrame) tmpparent.parents.elementAt(j)).getAllChildren();
          for (int k =0; k<children.size();k++) {
            if (! morespecialized.contains((KFrame) children.elementAt(k)))
                morespecialized.add((KFrame) children.elementAt(k));
          }
        }
      }
    }

    Vector dl = new Vector();
    for (int i = 0; i < possiblechoices.size(); i++) {
      // only frame, not instance
      if (! ( (KFrame) possiblechoices.elementAt(i)).isInstance()) {
        // not try if the slot directcreate == true
        String directcreate = ((KFrame) possiblechoices.elementAt(i)).getSlotValueRecursive("directcreate");
        if (directcreate == null || ! directcreate.equalsIgnoreCase("true")) {
          dl.add(possiblechoices.elementAt(i));
        }
      }
    }

    if (dl.size() != 0) {
      tmpFailedCondition = new Hashtable();

      //System.out.println("reInduceFrame: Trying list: "+dl);
      for (int j = 0; j < dl.size(); j++) {
        KFrame frame = (KFrame) dl.elementAt(j);
        boolean nottry = false;
        for (int k = 0; k < failedframes.size(); k++) {
          if (frame.inParentList( ( (KFrame) failedframes.elementAt(k))))
            nottry = true;
        }
        // if oneparent is set, try only the more specialized one if parentcount is not 0
        if ( fr.getSlotValue("oneparent") != null && fr.getSlotValue("oneparent") != "" ) {
          //System.out.println("Coming here: checking "+frame.getName());
          nottry = true;
          // if no parent left, try my upper parents, otherwise try only my specialized
          if ( (parentcount == 0 && fr.inParentList(frame)) || morespecialized.contains(frame)) {
            nottry = false;
          }
        }
        if (!nottry) {
          //System.out.println("ReInduceFrame -- Trying: " + frame.getName());
          int tryresult = frame.tryMe(fr.getHash());
          if (tryresult == 0 ) {
            out("ReInduce got " + frame.getName());
            System.out.println("Reinduce got " + frame.getName());

            if (parentcount == 0 ) {
              // Just changed from root
              fr.removeParent(wm.getRootFrame());
              fr.setToNewParent(frame);
            } else if (morespecialized.contains(frame)) {
              // remove the current parent this is the parent
              // of this new specialized frame
              for (int k = 0; k < myparents.size(); k++) {
                KFrame tmpparent = (KFrame) myparents.elementAt(k);
                //System.out.println("Checking parent: "+tmpparent.getName());
                if (frame.inParentList(tmpparent)) {
                  fr.removeParent(tmpparent);
                  //System.out.println("Removing "+tmpparent.getName());
                  // before leaving this frame
                  fr.runSpecialSlot("onTransition", tmpparent);
                }
              }
              fr.setToNewParent(frame);
            }
            else {
              fr.addParentFrame(frame);
              frame.add(fr);
            }
            fr.redrawPanel();
            // now run the onTransition slot
            System.out.println("Executing onTransitioned slot of "+frame.getName()+"...");
            fr.runSpecialSlot("onTransitioned", frame);
            return 1;
          } else if (tryresult == -1 ) {
            failedframes.add(frame);
          } // else do nothing, just loop
        }
      }
    }

    if (parentcount == 0 && fr.getSlotValue("oneparent") != "") {
      // one parent frame, reaching here means I have no parent! delete!
      fr.selfDelete();
    }

    // if you can reach here, it means we can induce nothing new
    return 0;
  }

  /**
   * Try if we have enough information to instantiate a Frame
   * return Hashvalue of unique & required parameters if success,
   * null if not.
   */
  private String tryFrame(KFrame f, boolean doAsk) {
    //System.out.println("Entering tryFrame: "+f.getName());

    Vector allSlots = new Vector();  // Full name of all slots of this Frame
    Hashtable slotHash = new Hashtable();  // Map fullname -> Slot Object
    Hashtable allHash = f.getAllSlotFullnames(slotHash);  //Map Sname -> Fname
    Hashtable shortname = new Hashtable();  //Map Fname -> Sname
    Hashtable instanceHash = null;
    Hashtable instanceHashForUpdate = null;
    boolean checkedUpdate = false;
    String hashString = new String(f.getName());
    boolean requireInstance = false;
    boolean requireOnlyInstance = true;

    SlotCombination instanceCombination = new SlotCombination();

    if(doAsk)
      out("Try: "+f.getName());

    Enumeration en = allHash.keys();
    while(en.hasMoreElements()) {
      String sname = (String)en.nextElement();    // Short Name
      String fname = (String)allHash.get(sname);  // Full Name
      allSlots.add(fname);
      shortname.put(fname, sname);
    }

    int requiredCount = 0;
    // how many instances we need
    // how many instances (among the required instances) that have already
    // been used to instantiate this frame
    for(int i=0;i<allSlots.size();i++) {
      String fname = (String)allSlots.elementAt(i);
      String sname = (String)shortname.get(fname);
      Slot sl = (Slot)slotHash.get(fname);
      if(sl.getRequired()) {  // Check only Required Slots
        requiredCount++;
        // if the instance slot's value is not specified and DF flag is not set
        // then we help find it
        if(sl.getType()==Slot.TYPE_INSTANCE &&
           !wm.mySlotsContainKey(fname) &&
           !sl.getDontFill()
           ) {
          requireInstance = true;
          if (!instanceCombination.hasMore()) {
            Vector ins = wm.findInstancesOf(sl.getArgument());
            boolean gotone = false;
            //System.out.println("Total "+ins.size()+" candiate(s) for "+sl.getArgument());
            for (int j = 0; j < ins.size(); j++) {
              KFrame cand = (KFrame) ins.elementAt(j);
              //System.out.println("Checking candidate: "+cand.getName());
              Vector dependerList = cand.getDependerList();
              boolean alreadyused = false;
              for (int k = 0; k< dependerList.size(); k++) {
                //System.err.println("Comparing "+((KFrame) dependerList.elementAt(k)).getParentName() + " and "+f.getName());
                // checking if it is already used to create meself or my children
                KFrame tmpelement = (KFrame) dependerList.elementAt(k);
                if ( tmpelement.getParentName().equals(f.getName()) ||
                     f.inChildList(tmpelement.getParentFrame()) ||
                     f.inParentList(tmpelement.getParentFrame()) ) {
                  //System.out.println("As "+cand.getName()+" was used in creating "+
                    //                 ((KFrame) dependerList.elementAt(k)).getName()+" already, now not used again");
                  alreadyused = true;
                  break;
                }
              }
              if ((sl.getShared() || (!cand.getInused())) ) {
                //System.out.println("Adding "+fname+" to instanceCombination");
                instanceCombination.add(fname, cand, sl.getUnique(), alreadyused);
                gotone = true;
              }
            }
            if (!gotone) {
              //System.out.println("NO candidate for: "+fname);
              return null;
            }
          }
        }
        else { // Non-Instance/Ordinary Slot
          requireOnlyInstance = false;
          if(!wm.mySlotsContainKey(fname)) {
            //out("Need slot: "+fname);
             if(doAsk) {
              String inputValue = JOptionPane.showInputDialog("Enter value for slot: "+fname);
              if(inputValue!=null)
                wm.mySlotsPut(fname, inputValue);
              else {
                return null;
              }
            }
            else {
              //System.err.println("Need slot: " + fname);
              return null;
            }
          }
          try {
            String val = wm.mySlotsGet(fname);
            f.checkSlotConditions(sname,val);
            if(sl.getUnique()) // This Slot must be UNIQUE ?
              hashString+=":"+fname+"="+val;
          }
          catch(InvalidSlotValueException e) {
            System.err.println("Exception: "+e.toString());
            if(doAsk)
              out("Fail: "+e.getSource());
            return null;
          }
        } // End of ordinary slot
      } // End of Required slot
//      else
//        out("Not required: "+fname);
    } // for

    if(requiredCount>0) {
     if(requireInstance) {
       // we proceed if
       // 1) at least 1 instance to be used is new (not used before)
       // 2) it does not only require instance (i.e. also require basic slots
        String fullHashString = null;
        instanceCombination.reset(requireOnlyInstance);
        while(instanceCombination.hasMore()) {
          fullHashString = instanceCombination.getHashString(hashString);
          //System.out.println("Combi:"+f.getName()+"="+ fullHashString);
          if(!seenInstanceHash.containsKey(fullHashString)) {
            //System.out.println("Combi not in seenInstanceHash, good");
            //System.out.println("Now check tmpFailedCondition");
            //System.out.println(tmpFailedCondition.toString());
            if ( ! tmpFailedCondition.containsKey(fullHashString)) {
              instanceHash = instanceCombination.getHash();
              System.out.println("Use");
              //seenInstanceHash.add(fullHashString);
              break;
            }
          }
          else { // Do Update
            if ((!checkedUpdate) && (instanceHashForUpdate == null)) {
              if ( ! tmpFailedCondition.containsKey(fullHashString)) {
                KFrame seenInstance = (KFrame) seenInstanceHash.get(fullHashString);
              //if(!seenInstance.isDummy()) {
                String newHash = instanceCombination.getHashString_AllSlots(hashString);
                String foo = seenInstance.getParamHash_AllSlots();
                if (!foo.equals(newHash)) {
                  System.out.println("I Update!!!: " + foo + "--" + newHash);
                  instanceHashForUpdate = instanceCombination.getHash();
                }
                checkedUpdate = true;
              }
            }
          }

         //System.out.println("Skip");
         instanceCombination.next();

        }
//        System.err.println("InHash: "+instanceHash);
//        System.err.println("FullHash: "+fullHashString);
        if(instanceHash==null) {
          if (instanceHashForUpdate != null) {
            Enumeration enu = instanceHashForUpdate.keys();
            while(enu.hasMoreElements()) {
              String keyfullname= (String)enu.nextElement();
              KFrame inst = (KFrame)instanceHashForUpdate.get(keyfullname);
              wm.mySlotsPut(keyfullname, inst.getName());
            }
            //System.out.println("Hash: " + fullHashString);
            //System.out.println("WM Slots (3): "+wm.printMySlots());
            //System.out.println("mySlotsMarkRemove (3): "+mySlotsMarkRemove);
            // Update slot value of existing instance
            if ( ! tmpFailedCondition.containsKey(fullHashString)) {
              KFrame seenInstance = (KFrame) seenInstanceHash.get(
                  fullHashString);
              //if(!seenInstance.isDummy()) {

              // set the frame parent to this frame if it not yet is
              if (seenInstance.getParentFrame() != f) {
                // AS KFrame::setToNewParent is changed
                // something needed to be added here
                seenInstance.removeParent(seenInstance.getParentFrame());
                seenInstance.setToNewParent(f);
                updateInstanceSlotValues(seenInstance);

                System.out.println("Executing onTransitioned slot...");
                seenInstance.runSpecialSlot("onTransitioned");
              } else {
                updateInstanceSlotValues(seenInstance);
                seenInstance.doUpdate();
              }
              removeFromConditionFailedHash(seenInstance.getName());
            }
            //System.err.println("VTry Fail");
          }
          //System.err.println("instanceHash is null");
          return null;
        }

        Enumeration enu = instanceHash.keys();
        while(enu.hasMoreElements()) {
          String keyfullname= (String)enu.nextElement();
          KFrame inst = (KFrame)instanceHash.get(keyfullname);
          wm.mySlotsPut(keyfullname, inst.getName());

          //System.out.println("Adding "+keyfullname+" to tmpAddedToMySlots vector...");
          tmpAddedToMySlots.add(keyfullname);

          inst.setInused(true);
         }
        return fullHashString;
      }

      else { // Requires no Instance (basic slots only)
        //System.err.println("VTry "+hashString);
        if(seenInstanceHash.containsKey(hashString)) {
          // Update slot value of existing instance
          KFrame seenInstance = (KFrame)seenInstanceHash.get(hashString);
          //if(!seenInstance.isDummy()) {
            updateInstanceSlotValues(seenInstance);
            seenInstance.doUpdate();
            removeFromConditionFailedHash(seenInstance.getName());
          //}
          //System.err.println("VTry Fail");
          return null;
        }
        if ( ! tmpFailedCondition.containsKey(hashString)) {
          //System.err.println("VTry Okay");
          // seenInstanceHash.add(hashString);
          return hashString;
        } else {
          //System.err.println("VTry Failed");
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Remove frames which depend on <framename> from the conditionFailedHash
   * return the number of entries remove
   * @param framename
   */
  public int removeFromConditionFailedHash(String framename) {
    //System.out.println("Entering removeFromConditionFailedHash..."+framename);
    // me is update, hence who depends on me should be out
    // from any black list
    int count = 0;
    Enumeration tmpfail = conditionFailedHash.keys();
    while(tmpfail.hasMoreElements()) {
      String key = (String) tmpfail.nextElement();
      //System.out.println("conditionFailedHash member: " + key);
      //
      // FIXME this has a bug in that Framename_1 will also match Framename_11
      //
      if ( key.indexOf(framename) != -1 ) {
        System.out.println("Removing "+key);
        conditionFailedHash.remove(key);
        count++;
      }
    }
    return count;
  }

  protected void out(String s) {
    if(zc!=null)
      zc.println(s);
  }
}
