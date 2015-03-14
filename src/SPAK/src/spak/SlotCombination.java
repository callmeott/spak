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

public class SlotCombination  {
  // Map SlotName -> Vector of candidate Instances for that Slot
  Hashtable data = new Hashtable();
  Hashtable seenHash = new Hashtable();
  Hashtable isUnique = new Hashtable();
  Hashtable isUsed = new Hashtable();
  int count=0;
  int size[] = null;
  int index[] = null;
  String names[] = null;
  Vector vector[] = null;
  boolean samegroup[] = null;
  boolean hasmore;
  boolean requireonlyinstance;

  public SlotCombination() {
  }

  /**
   * Add a candidate instance for a slotname.
   * @param name slotname
   * @param value candidate instance for this slot
   * @param unique this is a unique slot ?
   * @parm alreadyused this candidate has been used already to instantiate
   * same type of frame
   */
   public void add(String name, Object value, boolean unique) {
     add(name,value,unique,false);
   }

  public void add(String name, Object value, boolean unique, boolean alreadyused) {
    Vector val;
    if(data.containsKey(name))
      val = (Vector)data.get(name);
    else {
      val = new Vector();
      data.put(name, val);
    }
    isUnique.put(name, new Boolean(unique));
    isUsed.put(value, new Boolean(alreadyused));
    val.add(value);
  }


  public void reset() {
    reset(false);
  }

  public void reset(boolean _requireonlyinstance) {
    requireonlyinstance = _requireonlyinstance;
    count = data.size();
    size = new int[count];
    index = new int[count];
    names = new String[count];
    vector = new Vector[count];
    samegroup = new boolean[count];
    seenHash.clear();
    String pGroupname = null;

    //Enumeration en = data.keys();

    // Sort hashtable.
    Vector v = new Vector(data.keySet());
    Collections.sort(v);

    // Display (sorted) hashtable.
    Enumeration en = v.elements();

    int i=0;
    while(en.hasMoreElements()) {
      names[i] = (String)en.nextElement();
      String fname = names[i];
      Vector val = (Vector)data.get(fname);
      int uscore = fname.indexOf('_');
      String groupname = (uscore>=0) ?
        fname.substring(0,uscore):fname;
      vector[i] = val;
      index[i]=0;
      size[i]=val.size();
      if(i>0)
        samegroup[i-1]=groupname.equals(pGroupname);
      pGroupname = groupname;
      i++;
    }
    if(i>0) samegroup[i-1]=false;
    hasmore=count>0;
    while(hasMore() && checkOutput())
      next(0);
  }

  public Hashtable getHash() {
    if(hasmore) {
      Hashtable ret = new Hashtable();
      for(int i=0;i<count;i++)
        ret.put(names[i],vector[i].elementAt(index[i]));
      return ret;
    }
    return null;
  }

  public String getHashString(String prefix) {
    String ret = new String(prefix);
    if(hasmore) {
      for(int i=0;i<count;i++)
        if(((Boolean)isUnique.get(names[i])).booleanValue())
          ret+=":"+names[i]+"="+((KFrame)(vector[i].elementAt(index[i]))).getName();
    }
    return ret;
  }

  public String getHashString_AllSlots(String prefix) {
    String ret = new String(prefix);
    if(hasmore) {
      for(int i=0;i<count;i++)
        ret+=":"+names[i]+"="+((KFrame)(vector[i].elementAt(index[i]))).getName();
    }
    return ret;
  }

  public boolean hasMore() {
    return hasmore;
  }

  public void next() {
    next(0);
    while(hasMore() && checkOutput())
      next(0);
  }

  boolean checkOutput() {
    Hashtable objlist = new Hashtable();
    for(int i=0;i<count;i++) {
      Object obj = vector[i].elementAt(index[i]);
      if(objlist.containsKey(obj))
        return true;
      else
        objlist.put(obj,obj);
    }
    // if requireonlyinstance is set all the candidates must be new
    boolean allareused = false;
    if ( requireonlyinstance ) {
      int usecount=0;
      for(int i=0;i<count;i++) {
        Object obj = vector[i].elementAt(index[i]);
        if(((Boolean)(isUsed.get(obj))).booleanValue() )
          usecount++;
      }
      if(usecount==count)
        allareused = true;
    }
    return allareused;
  }

  void next(int from) {
    index[from]++;
    if(index[from]>=size[from]) {
      if(from+1<count) {
        next(from+1);
        index[from]= samegroup[from]?index[from+1]:0;
      }
      else
        hasmore=false;
    }
  }
}