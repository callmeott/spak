package spak;
import java.util.*;
import javax.swing.*;

/**
 * <p>Title: SPAK Backward Chaining</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class BackwardChaining {
  WorkingMemory wm = null;
  ZeroConsole zc=null;
  Vector mySlotsMarkRemove = new Vector(); // For marking used slots
  private Vector tmpAddedToMySlots = null; // what added to wm.mySlots when trying

  // Frame Name -> Instances
  // of all instances created during this Backward chaining session.
  Hashtable createdInstances = new Hashtable();

  public BackwardChaining(WorkingMemory x) {
    wm = x;
  }

  public void reset(WorkingMemory newwm) {
    wm = newwm;
  }

  public void setZeroConsole(ZeroConsole zcon) {
    zc = zcon;
    wm.setZeroConsole(zc);
  }

  protected void out(String s) {
    if(zc!=null)
      zc.println(s);
  }

  /**
   * Apply backward chaining toward the goal of
   * instantiating "frame".
   */

  public int backward(String frame) {
    int count = 0;
    wm.lock();
    boolean gotone = false;

    createdInstances.clear();
    KFrame startPoint = wm.getRootFrame().findFrame(frame);
    if (startPoint != null) {
      KFrame newInst = null;
      tmpAddedToMySlots = new Vector();
      while ( (newInst = makeInstance(startPoint)) != null) {
        newInst.updateHash(newInst.getParamHash_AllSlots());
        System.out.println("TryGot: " + newInst.getName());

        //wm.clearMySlots();
        // clean up ALL I added to wm (used or not used) but not more
        // BEFORE running onInstantiate
        for (int i=0; i< tmpAddedToMySlots.size(); i++) {
          wm.mySlotsRemove( (String) tmpAddedToMySlots.elementAt(i));
        }
        tmpAddedToMySlots.clear();

        newInst.runSpecialSlot("onInstantiate");
        gotone = true;
        count++;
        //if (JOptionPane.showConfirmDialog(null,
        //                                  "Continue trying on: " + frame + " ?",
        //                                  "Confirmation",
        //                                  JOptionPane.YES_NO_OPTION) !=
        //    JOptionPane.YES_OPTION)
          break;
      }
    }
    else // startPoint is Null
      JOptionPane.showConfirmDialog(null, "Frame not found: " + frame,
                                    "Invalid Frame Name",
                                    JOptionPane.ERROR_MESSAGE);

    // clean up (for unsuccessful cases)
    if (!gotone) {
      for (int i = 0; i < tmpAddedToMySlots.size(); i++) {
        wm.mySlotsRemove( (String) tmpAddedToMySlots.elementAt(i));
      }
      tmpAddedToMySlots.clear();
    }

    wm.unlock();
    return count;
  }

  KFrame testAllConditions(KFrame frame, String paramHash) {
    mySlotsMarkRemove.removeAllElements();
    KFrame inst = frame.createInstance(false, false);
    fillInstanceSlotValues(inst);
    inst.setParamHash(paramHash);
    if(inst.checkCondition()) { // Check "condition" and execute "onTry" slots
      out("BackGot "+frame.getName());
      System.out.println("BackGot "+frame.getName());

//    removeMarkedMySlots(); // obsolete
      return inst;
    }
    else {
      frame.removeInstance(inst);
      return null;
    }
  }

  /**
   * Attempt to create an instance of "frame".
   * Return the newly-created instance if succeeds, null
   * if fails.
   */

  KFrame makeInstance(KFrame frame) {
    System.out.println("MakeInstance: " + frame.getName());
    Hashtable slotHash = new Hashtable();  // Map slot fullname -> Slot Object
    Hashtable fullName = frame.getAllSlotFullnames(slotHash);  //Map Sname -> Fname
    Hashtable instanceHash = null;
    KFrame result=null;

    String hashString = new String(frame.getName());
    SlotCombination instanceCombination = new SlotCombination();

    Vector allSlots = new Vector();  // Full name of all slots of this Frame
    Hashtable shortName = new Hashtable();  //Map Fname -> Sname

    // Fill up allSlots and shortName
    Enumeration en = fullName.keys();
    while(en.hasMoreElements()) {
      String sname = (String)en.nextElement();    // Short Name
      String fname = (String)fullName.get(sname);  // Full Name
      allSlots.add(fname);
      shortName.put(fname, sname);
    }

    // check if onBTry exists
    for(int i=0;i<allSlots.size();i++) {
      String fname = (String)allSlots.elementAt(i);
      String sname = (String)shortName.get(fname);
      if (sname.equals("onBTry")) {
        Slot sl = (Slot)slotHash.get(fname);
        System.out.println("onBTry slot found: "+sl.getValue());
        System.out.println("Submit to JavaScript queue...");
        String eval = "s = Root.findFrame(\""+frame.getName()+"\").getKFrameScript() ; "+sl.getValue();
        wm.putJSCommand(eval);
        return null;
      }
    }
    // Get required slot values
    for(int i=0;i<allSlots.size();i++) {
      String fname = (String)allSlots.elementAt(i);
      String sname = (String)shortName.get(fname);
      Slot sl = (Slot)slotHash.get(fname);

      if(sl.getRequired()) {  // Check only Required Slots
        System.out.println("Check slot: "+sname);
        if(sl.getType()!=Slot.TYPE_INSTANCE || (sl.getType()==Slot.TYPE_INSTANCE && sl.getDontFill()) ) {
          if(!wm.mySlotsContainKey(fname)) {
            String inputValue = JOptionPane.showInputDialog("Enter value for slot: "+fname);
            if(inputValue!=null) {
              wm.mySlotsPut(fname, inputValue);
              tmpAddedToMySlots.add(fname);
              System.out.println("Put "+fname+" = " +wm.mySlotsGet(fname)+" in to wm.");
            } else
              return null;
          }
          try {
            String val = wm.mySlotsGet(fname);
            frame.checkSlotConditions(sname,val);
            //hashString+=":"+fname+"="+val;
          }
          catch(InvalidSlotValueException e) {
            out("Fail: "+e.getSource());
            return null;
          }
        } // if is not Instance
      } // if required
    } // Slot loop

    // Get required Instances
    boolean requireInstance = false; // Whether this frame requires an instance
    for(int i=0;i<allSlots.size();i++) {
      String fname = (String)allSlots.elementAt(i);
      String sname = (String)shortName.get(fname);
      Slot sl = (Slot)slotHash.get(fname);

      if(sl.getRequired()) {  // Check only Required Slots
        if(sl.getType()==Slot.TYPE_INSTANCE && ! sl.getDontFill()) {
          requireInstance = true;
          // if we need an instance of another frame, first find it
          // if not found then create it (via backward chainning!

          boolean gotone = false;
          if (wm.findInstancesOf(sl.getArgument()).size() != 0 ) {
            Vector instancesinwm = wm.findInstancesOf(sl.getArgument());
            for (int j =0; j < instancesinwm.size();j++) {
              KFrame ins = (KFrame) instancesinwm.elementAt(j);

              Vector dependerList = ins.getDependerList();
              boolean alreadyused = false;
              for (int k = 0; k< dependerList.size(); k++) {
                //System.err.println("Comparing "+((KFrame) dependerList.elementAt(k)).getParentName() + " and "+f.getName());
                // checking if it is already used to create meself or my children
                KFrame tmpelement = (KFrame) dependerList.elementAt(k);
                if ( tmpelement.getParentName().equals(frame.getName()) ||
                     frame.inChildList(tmpelement.getParentFrame()) ||
                     frame.inParentList(tmpelement.getParentFrame()) ) {
                  System.out.println("As "+ins.getName()+" was used in creating "+
                                   ((KFrame) dependerList.elementAt(k)).getName()+" already, now not used again");
                  alreadyused = true;
                  break;
                }
              }

              if ( (sl.getShared() && !alreadyused) || (!ins.getInused())) {
                instanceCombination.add(fname, ins, sl.getUnique());
                gotone = true;
              }
            }
          }
          if (!gotone) {
            KFrame ins = null;
            if (createdInstances.containsKey(sl.getArgument()))
              ins = (KFrame) createdInstances.get(sl.getArgument());
            else {
              ins = makeInstance(
                  wm.getRootFrame().findFrame(sl.getArgument()));
              if (ins != null)
                createdInstances.put(sl.getArgument(), ins);
            }
            if (ins != null) {
              if (sl.getShared() || (!ins.getInused())) {
                instanceCombination.add(fname, ins, sl.getUnique());
              }
            }
            else
              return null;
          }
        } // if is an Instance
      } // if required
    } // Slot loop

    // Fill in instance slots
    // and Check Frame Condition
    if(requireInstance) {
      String fullHashString = null;
      instanceCombination.reset();
      Vector seenInstanceHash = new Vector();
      boolean foundGoodInstances = false;

      while(instanceCombination.hasMore()) {
        fullHashString = instanceCombination.getHashString(hashString);
//          System.out.println("Combi:"+fullHashString);
        if(!seenInstanceHash.contains(fullHashString)) {
//            System.out.println("Use");
          instanceHash = instanceCombination.getHash();
          seenInstanceHash.add(fullHashString);

          // Fill instances into slots
          Enumeration enu = instanceHash.keys();
          while(enu.hasMoreElements()) {
            String keyfullname= (String)enu.nextElement();
            KFrame inst = (KFrame)instanceHash.get(keyfullname);
            wm.mySlotsPut(keyfullname, inst.getName());
            inst.setInused(true);
            tmpAddedToMySlots.add(keyfullname);
          }
          if((result=testAllConditions(frame, fullHashString))!=null) {
            foundGoodInstances = true;
            break;
          }

          // Remove from working memory
          enu = instanceHash.keys();
          while(enu.hasMoreElements()) {
            String keyfullname= (String)enu.nextElement();
            KFrame inst = (KFrame)instanceHash.get(keyfullname);
            wm.mySlotsRemove(keyfullname);
            inst.setInused(false);
          }
        }
        instanceCombination.next();
      }
      if(!foundGoodInstances)
        return null;
    }
    else { // Doesn't require an instance
      if((result=testAllConditions(frame, hashString))==null)
        return null;
    }

    // Instantiate myself
    result.updateDependency();
    return result;
  }

  public void  fillInstanceSlotValues(KFrame inst) {
    Hashtable allHash = inst.getAllSlotFullnames();  // Slots to fill
    Enumeration en = allHash.keys();  // Short Names
    while(en.hasMoreElements()) {
      String sname = (String)en.nextElement();    // Short Name
      String fname = (String)allHash.get(sname);  // Full Name
      //System.out.println("Fill Slot: " + sname + " ("+fname+") with " + (String) wm.mySlotsGet(fname));
      if(wm.mySlotsContainKey(fname)) {
        inst.setSlotValue(sname, wm.mySlotsGet(fname), false);
        //out("Fill Slot: "+sname+" with "+(String)mySlots.get(fname));
        System.out.println("Filled Slot: " + fname + " with " + (String) wm.mySlotsGet(fname));
      }
      // Mark Remove from current working memory -- obsolete
      //mySlotsMarkRemove.add(fname);
    }
  }

  /**
   * Removed used slot from working memory
   */

  private void removeMarkedMySlots() {
    for(int i=0;i<mySlotsMarkRemove.size();i++)
      wm.mySlotsRemove((String)mySlotsMarkRemove.elementAt(i));
  }
}