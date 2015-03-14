package spak;
import java.util.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class SlotList {
  private Vector myList = new Vector();
  private Hashtable myHash = new Hashtable();

  public SlotList() {
  }

  public void add(Slot s) {
    if ( myHash.containsKey(s.getName())) {
      // the new overrides the old one in case of same name
      // take it out first
      myHash.remove(s.getName());
      for (int i=0; i<myList.size();i++) {
        if ( ((Slot) myList.elementAt(i)).getName().equals(s.getName())) {
             myList.remove(i);
             i--;
        }
      }
    }
    myList.addElement(s);
    myHash.put(s.getName(), s);
    s.addNameChangeListener(this);
  }

  public void add(SlotList sl) {
    Vector nv = sl.getSlotVector();
    for (int i=0; i< nv.size();i++) {
      add( (Slot) nv.elementAt(i));
    }
  }

  public Vector getSlotVector() {
    return myList;
  }

  public Hashtable getSlotHash() {
    return myHash;
  }

  public int size() {
    return myList.size();
  }

  public Vector vectorAt(int i) {
    Vector v = new Vector();
    v.addElement( ( (Slot) myList.elementAt(i)).getName());
    v.addElement( ( (Slot) myList.elementAt(i)).getValue());
    return v;
  }

  public Slot slotAt(int i) {
    return (Slot) myList.elementAt(i);
  }

  public void changeSlotName(String old, String name) {
    Slot sl = (Slot) myHash.get(old);
    myHash.remove(old);
    myHash.put(name, sl);
  }

  public int countSlotMatch(Vector inNames) {
    int count = 0;
    for (int i = 0; i < myList.size(); i++)
      if (inNames.contains( ( (Slot) myList.elementAt(i)).getName()))
        count++;
    return count;
  }

  public void removeSlotAt(int i) {
    myHash.remove( ( (Slot) myList.elementAt(i)).getName());
    myList.removeElementAt(i);
  }

  public Slot getSlot(String slname) {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
      return myslot;
    else
      return null;
  }

  public String getSlotValue(String slname) {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
      return myslot.getValue();
    else
      return null;
  }

  public Vector getSlotVValue(String slname) {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
      return myslot.getVValue();
    else
      return null;
  }

  public String getPreviousSlotValue(String slname, int idx) {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
      return myslot.getPreviousValue(idx);
    else
      return null;
  }

  public Vector getPreviousSlotVValue(String slname, int idx) {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
      return myslot.getPreviousVValue(idx);
    else
      return null;
  }

  public boolean slotValueContains(String slname, String match) {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
      return myslot.valueContains(match);
    else
      return false;
  }

  /**
   * Get the slot value at the given timestamp
   * @param slname slotname
   * @param timestamp if the value is greater than zero, it will be threat as
   * absolute timestamp (milliseconds from January 1, 1970). If less than zero,
   * it will mean the value at the last <timestamp> seconds ago.
   * @return
   */
  public String getSlotValue(String slname, long timestamp) {
    Slot myslot = (Slot)myHash.get(slname);
    if(myslot!=null)
      if ( timestamp < 0 ) {
        timestamp *= 1000;
        Date d = new Date();
        return myslot.getValue(d.getTime() + timestamp);
      } else {
        return myslot.getValue(timestamp);
      }
    else
      return null;
  }

  public Vector getSlotVValue(String slname, long timestamp) {
    Slot myslot = (Slot)myHash.get(slname);
    if(myslot!=null)
      if ( timestamp < 0 ) {
        timestamp *= 1000;
        Date d = new Date();
        return myslot.getVValue(d.getTime() + timestamp);
      } else {
        return myslot.getVValue(timestamp);
      }
    else
      return null;
  }

  public void setSlotValue(String slname, String value)
       throws InvalidSlotValueException {
    Slot myslot = (Slot) myHash.get(slname);
    if (myslot != null)
        myslot.setValue(value);
  }

  public void setSlotVValue(String slname, Vector vvalue)
         throws InvalidSlotValueException {
    Slot myslot = (Slot)myHash.get(slname);
    if(myslot!=null)
        myslot.setValue(vvalue);
  }

  public boolean checkSlotValue(String slname, String value) {
    Slot myslot = (Slot)myHash.get(slname);
    if(myslot!=null)
      return myslot.checkValue(value);
    else
      return true;
  }

  public void addSlotValue(String slname, String value)
      throws InvalidSlotValueException {
    Slot myslot = (Slot)myHash.get(slname);
    if(myslot!=null)
      myslot.addValue(value);
  }

  public void removeSlotValue(String slname, String value) {
    Slot myslot = (Slot)myHash.get(slname);
    if(myslot!=null)
      myslot.removeValue(value);
  }
}