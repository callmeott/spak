package spak;
import java.util.*;

/**
 * <p>Title: Slot</p>
 * <p>Description: Class defining a frame slot</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 *
 * $Log: Slot.java,v $
 * Revision 1.2  2005-08-05 17:31:57  pattara
 * -onUpdate will not be executed if the frame is not an instance
 * -Slot::getPreviousValue will return null if there is no history
 *
 * Revision 1.1  2005/08/02 07:30:34  pattara
 * -first import
 *
 * Revision 1.28  2005/07/20 08:04:21  pattara
 * -0.99a
 *
 * -First phase code clean up done.
 * -Ready for extensive test, heading towards 1.0
 *
 * Revision 1.27  2005/07/08 05:37:42  pattara
 * -to prevent segfault
 *
 * Revision 1.26  2005/04/08 10:23:46  pattara
 * -new slot flag: DF (Don't Fill). In case of the R (Required flag) is set,
 *  when inducing, if this flag is set, the inference engine will NOT try to
 *  find a value for it, otherwise it will help finding a value (default behavior)
 *
 * Revision 1.25  2005/04/07 13:43:28  pattara
 * -Instance-slot value of the slot is allowed to be Frame (not-instance) that
 *  is equal to the requirement argument itself.
 *
 * Revision 1.24  2005/04/01 08:28:44  pattara
 * -fix bug in KFrame::tryMe()
 *  Summary:
 * -A slot value must exist if the Required flag is set.
 * -A slot value, if exists, must match the slot condition (if exists).
 *
 * Revision 1.23  2005/03/30 12:51:47  pattara
 * -handling of white space
 *
 * Revision 1.22  2005/03/24 17:03:11  pattara
 * -revert to the old way, instances still have own slot conditions, in case
 *  of instance slots, slot conditions are somehow easier to implement using
 *  own instance slots at the moment (R, BO flags stuff)
 *
 * Revision 1.21  2005/03/22 14:48:12  pattara
 * -required slots should have not-null values
 *
 * Revision 1.20  2005/03/04 04:53:36  pattara
 * -add IS_A special slots to every frames to show my parents
 *
 * Revision 1.19  2005/03/04 03:33:35  pattara
 * -add the _ID slot to every frames as their identities (because the Name slot can be changed)
 *
 * Revision 1.18  2005/03/03 17:14:21  pattara
 * -major revamp on the ForwardChaining::reInduce(), using KFrame::tryMe()
 *  instead of tryFrame
 * -KFrame::tryMe uses short slotname in hash instead of long
 *
 * Revision 1.17  2005/03/02 17:46:57  pattara
 * -Allow setting of slot value that violates the condition, assuming
 *  that the reinduce will be run to fix the frame. By allowing we mean
 *  it is set to the new value and an exception still occurs.
 * -In case of setting slot value from network (9900), the reinduce
 *  will be started automatically if such event happened.
 * -In case of editing in the knowledge editor, a message saying please
 *  reinduce is shown
 *
 * Revision 1.16  2004/12/08 04:31:40  vuthi
 * Fix CR-LF problems by removing all 0xD from Unix side
 *
 * Revision 1.15  2004/11/25 16:22:08  pattara
 * -OK, Beginning Only flag is working now
 *
 * Revision 1.14  2004/11/25 11:19:39  pattara
 * -the new flag "Beginning Only" (related with Required) added
 *
 * Revision 1.13  2004/11/19 18:30:17  pattara
 * -getSpecialSlot(xxx) instead of getOnEvaluate, getOnInstantiate
 * -FIXME stll need more extensive test
 *
 * Revision 1.12  2004/10/15 10:33:59  pattara
 * -add COND_IN support
 * -COND_SUBSET, COND_SUPERSET, COND_COVER partly added but not yet implemented
 * -the condition field is a bit too tight
 *
 * Revision 1.11  2004/10/13 16:55:11  pattara
 * -LIST data type is more or less working
 *
 * Revision 1.10  2004/10/13 09:40:55  pattara
 * -CONFLICT at KFRAME, need to be fixed
 * -adding support for "List" data type
 *
 * Revision 1.9  2004/09/02 13:34:58  pattara
 * -bug in the comment
 *
 * Revision 1.8  2004/09/02 04:43:28  pattara
 * -new method KFrame, SlotList::getPreviousSlotValue(slname, idx),
 * Slot::getPreviousValue(idx)
 *
 *
 *     * Retrieve the previous slot values according to the given index
 *     * idx >= 0 --> return the latest value
 *     * idx = -1 --> return the previous value
 *     * idx = -2 --> return the value before the previous value
 *     * and so on...
 *   *
 * -KFrame::getSlotValue(time) now will assume time in seconds in case that
 *  negative value is given (instead of milliseconds in the past)
 *
 * Revision 1.7  2004/09/01 12:30:03  pattara
 * -For implementing Command Processor using SPAK
 * -add getAge() to KFrame, KFrameScript and Slot
 *
 * Revision 1.6  2004/08/05 14:03:32  pattara
 * -robota i hope it works
 *
 * Revision 1.5  2004/08/01 17:53:42  pattara
 * -seems to work now
 *
 * Revision 1.4  2004/08/01 17:22:20  pattara
 * -adding a vector to keep old slot values, and related changes
 *
 * Revision 1.3  2004/07/28 04:59:20  vuthi
 * 1) Add file filter for .xml, .js, etc.
 * 2) Improve slot editor. Disable editing of non-related attributes.
 * 3) Implement UNIQUE attribute. Allow network client to update
 *     non-unique required values of existing Instances.
 * 4) Add/Implement "onUpdate" slot.
 *
 * Revision 1.2  2004/07/26 03:32:44  vuthi
 * Add "Unique" field and ability to save/load in/out of XML file.
 * Maintain compatibility with earlier Knowledge file with no UNIQUE tag.
 *
 */

public class Slot {
  public static final int COND_ANY = 0;
  public static final int COND_EQ  = 1;
  public static final int COND_LT  = 2;
  public static final int COND_LE  = 3;
  public static final int COND_GT  = 4;
  public static final int COND_GE  = 5;
  public static final int COND_NE  = 6;
  public static final int COND_RANGE = 7;
  public static final int COND_INSTANCEOF = 8;
  public static final int COND_SUBSET = 9;
  public static final int COND_SUPERSET = 10;
  public static final int COND_IN = 11;
  public static final int COND_COVER = 12;
  public static final int COND_COUNT = 13;
  public static final String cond_varnames[] = {
      "COND_ANY", "COND_EQ","COND_LT","COND_LE",
      "COND_GT", "COND_GE","COND_NE","COND_RANGE",
      "COND_INSTANCEOF", "COND_SUBSET",
      "COND_SUPERSET", "COND_IN", "COND_COVER", "COND_COUNT"
  };
  public static final String cond_names[] = {
    "ANY", "=", "<", "<=", ">", ">=",
    "!=", "<->", "InstanceOf", "SUBSET", "SUPERSET",
    "IN", "COVER", "C"};

  public static final int TYPE_STR = 0;
  public static final int TYPE_INT = 1;
  public static final int TYPE_INSTANCE = 2;
  public static final int TYPE_PROCEDURE = 3;
  public static final int TYPE_LIST = 4;
  public static final int TYPE_COUNT = 5;
  public static final String type_varnames[] = {
    "TYPE_STR", "TYPE_INT", "TYPE_INSTANCE", "TYPE_PROCEDURE",
    "TYPE_LIST"
  };

  public static final String type_names[] = { "String", "Integer",
    "Instance", "Procedure", "List" };

  String name = null;
  String value= "";
  Vector listvalue= null;
  Vector oldvalues = null;
  String argument = "";
  KFrame owner = null;
  int condition = COND_ANY;
  int type = TYPE_STR;
  boolean unique = true;
  boolean required = true;
  boolean requiredAtBeginningOnly = false;
  boolean dontfill = false;
  boolean shared = false;    // If type is Instance, this flag indicates whether
                             // the slot allows shared instance.

  Vector nameChangeListener = new Vector();
  long createtime;
  long lastupdate;

  public Slot(String n, String v, KFrame o) {
    name = n;
    value = v;
    owner = o;
    listvalue = new Vector();
    oldvalues = new Vector();

    Date d = new Date();
    createtime = d.getTime();

    if(n.equals("Name") || n.indexOf("_") == 0 ) {
      unique = false;
      required = false;
      shared = false;
    }
  }

  public Slot(String n, String v, KFrame o, int t, int cond, String ar,
         boolean req, boolean reqb, boolean df, boolean sh, boolean uni) {
    this(n,v,o,t,cond,ar);
    required = req;
    requiredAtBeginningOnly = reqb;
    dontfill = df;
    shared = sh;
    unique = uni;

    if(n.equals("Name") || n.indexOf("_") == 0 ) {
      unique = false;
      required = false;
      shared = false;
    }
  }

  public Slot(String n, String v, KFrame o, int t, int cond, String ar) {
    name = n;
    value = v;
    owner = o;
    type = t;
    listvalue = new Vector();
    condition = cond;
    argument = ar;
    oldvalues = new Vector();

    if(n.equals("Name") || n.indexOf("_") == 0 ) {
      unique = false;
      required = false;
      shared = false;
    }
  }

  public void addNameChangeListener(SlotList sl) {
    nameChangeListener.add(sl);
  }

  public String toString() {
    return name+"="+value;
  }

  public long getLastUpdateTime() {
    return lastupdate;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    if ( type_varnames[type].equals("TYPE_LIST")) {
      return listvalue.toString();
    } else {
      return value;
    }
  }

  public Vector getVValue() {
    if (type_varnames[type].equals("TYPE_LIST")) {
      return listvalue;
    }
    else {
      System.err.println("Attempt to getVValue from a scalar slot");
      return null;
    }
  }

  public boolean valueContains(String match) {
    if ( type_varnames[type].equals("TYPE_LIST")) {
      return listvalue.contains(match);
    } else {
      return value.equals(match);
    }
  }

  class SlotTimeValuePair {
    long timestamp;
    Object value;
  }

  public String getValue(long timestamp) {
    int i = oldvalues.size();
    if ( i == 0 ) return value; // this should not actually happen

    SlotTimeValuePair tmpoldslot;
    do {
      tmpoldslot = (SlotTimeValuePair) oldvalues.elementAt(--i);
    }
    while (timestamp < tmpoldslot.timestamp && i > 0);
    return tmpoldslot.value.toString();
  }

  public Vector getVValue(long timestamp) {
    int i = oldvalues.size();
    if ( i == 0 ) return listvalue; // this should not actually happen

    SlotTimeValuePair tmpoldslot;
    do {
      tmpoldslot = (SlotTimeValuePair) oldvalues.elementAt(--i);
    } while ( timestamp < tmpoldslot.timestamp && i > 0);
    return (Vector) tmpoldslot.value;
  }

  /**
   * Retrieve the previous slot values according to the given index
   * idx >= 0 --> return the latest value
   * idx = -1 --> return the previous value
   * idx = -2 --> return the value before the previous value
   * and so on...
   */

  public String getPreviousValue(int idx) {
    int size = oldvalues.size();
    int realidx = size + idx -1;

    if ( size < 2 ) { // have only current value
      return null;
    }

    if ( realidx < 0 ) {
      realidx = 0;
    } else if ( realidx > size - 1 ) {
      realidx = size -1;
    }
    Object tmp = ((SlotTimeValuePair) oldvalues.elementAt(realidx)).value;
    if ( type_varnames[type].equals("TYPE_LIST")) {
      return ( (Vector) tmp).toString();
    } else {
      return (String) tmp;
    }
  }

  public Vector getPreviousVValue(int idx) {
    int size = oldvalues.size();
    int realidx = size + idx -1;

    if ( realidx < 0 ) {
      realidx = 0;
    } else if ( realidx > size - 1 ) {
      realidx = size -1;
    }
    return (Vector) ( (SlotTimeValuePair) oldvalues.elementAt(realidx)).value;
  }


  public KFrame getOwner() {
    return owner;
  }

  public int getCondition() {
    return condition;
  }

  public String getConditionName() {
    return cond_names[condition];
  }

  public void setConditionName(String name)
         throws ConditionInvalidateSlotValueException {
    for(int i=0;i<COND_COUNT;i++) {
      if(name.equals(cond_names[i])) {
        condition=i;
        if(!checkValue(value)) {
          //throw the exception but better let the value as it is
          //value= "";
          throw new ConditionInvalidateSlotValueException(name, owner.getName());
        }
        break;
      }
    }
  }

  public int getType() {
    return type;
  }

  public void setType(int t) {
    type=t;
  }

  public void setRequired(boolean r) {
    required = r;
  }

  public void setRequiredAtBeginningOnly(boolean r) {
    requiredAtBeginningOnly = r;
  }

  public boolean getRequired() {
    return required;
  }

  public boolean getRequiredAtBeginningOnly() {
    return requiredAtBeginningOnly;
  }

  public boolean getDontFill() {
    return dontfill;
  }

  public void setDontFill(boolean r) {
    dontfill = r;
  }

  public void setShared(boolean i) {
    shared = i;
  }

  public boolean getShared() {
    return shared;
  }

  public void setUnique(boolean r) {
    unique = r;
  }

  public boolean getUnique() {
    return unique;
  }

  public String getTypeName() {
    return type_names[type];
  }

  public void setTypeName(String name) {
    for(int i=0;i<TYPE_COUNT;i++)
      if(name.equals(type_names[i]))
        type=i;
  }

  private void saveValueToHistory() {
    SlotTimeValuePair tmpslot = new SlotTimeValuePair();
    Date d = new Date();
    tmpslot.timestamp = d.getTime();
    if (type_varnames[type].equals("TYPE_LIST")) {
      Vector tmp=new Vector();
      tmp.addAll(listvalue);
      tmpslot.value = tmp;
    } else {
      tmpslot.value = value;
    }
    oldvalues.add(tmpslot);
    lastupdate = tmpslot.timestamp;
    //System.out.println("Slot::setValue: timestamp: "+ tmpslot.timestamp);
  }

  public void setValue(Vector vval) throws InvalidSlotValueException {
    if (type_varnames[type].equals("TYPE_LIST")) {
      if(checkVValue(vval)) {
        listvalue = vval;
        saveValueToHistory();
      }
    } else {
      System.err.println("Error, cannot assign a vector value to scalar variable");
    }

  }

  // should it really be here?
  public static Vector stringToVector(String val) {
    //System.err.println("Parsing string to vector: "+val);
    int begin = val.indexOf("[");
    int end = val.lastIndexOf("]");
    if ( begin == -1 || end == -1 || begin + 1 >= val.length()) {
      // unparsable
      return null;
    }
    String substr = val.substring(begin + 1, end);
    //System.err.println("substr is: "+substr);
    Vector tmp = new Vector(Arrays.asList(substr.split("[,\\s]+")));
    return tmp;
  }

  public void setValue(String val) throws InvalidSlotValueException {
    if ( type_varnames[type].equals("TYPE_LIST")) {
      Vector tmplistvalue = stringToVector(val);

      listvalue = tmplistvalue;
      //System.err.println("listvalue is now: "+listvalue.toString());
      saveValueToHistory();

      if(! checkVValue(tmplistvalue)) {
        throw new InvalidSlotValueException(name, owner.getName());
      }
    } else {
      value = val;
      saveValueToHistory();
      if (!checkValue(val)) {
        throw new InvalidSlotValueException(name, owner.getName());
      }
    }
  }


  public void addValue(String newvalue) {
    if ( type_varnames[type].equals("TYPE_LIST")) {
      if(checkValue(newvalue)) {
        listvalue.add(newvalue);
        saveValueToHistory();
      }
    } else {
      System.err.println("Error, please use Slot::setValue for non-vector type");
    }
  }

  public void removeValue(String value) {
  if ( type_varnames[type].equals("TYPE_LIST")) {
      listvalue.remove(value);
  } else {
    System.err.println("Error, please use Slot::setValue for non-vector type");
  }
}


  public void setName(String val) {
    for(int i=0;i<nameChangeListener.size();i++)
      ((SlotList)nameChangeListener.elementAt(i)).changeSlotName(name,val);
    name=val;
  }

  public long getAge() {
    Date d = new Date();
    return d.getTime() - createtime;
  }

  public String getArgument() {
    return argument;
  }

  public void setArgument(String val)
         throws ArgumentInvalidateSlotValueException {
    argument=val;
    if( !checkValue(value)) {
      //value= "";
      throw new ArgumentInvalidateSlotValueException(name, owner.getName());
    }
  }

  public void setOwner(KFrame o) {
    owner = o;
  }

  public boolean checkValue(String v) {
    //System.err.println("Check type: "+type+" val: "+v+" arg: "+argument);
    if (condition == COND_ANY ) {
      if (required && ! requiredAtBeginningOnly ) {
        // if required then it shouldn't be null
        return (v != null && !v.equals(""));
      } else {
        return true;
      }
    }

    // is this a good thing to do?
    if (v == null)
        v = "";

    //if (v.equals("") && condition != COND_ANY) // then it cannot be true
    //  return false;

    switch(type) {
      case TYPE_INT:
        try {
          if(v.equals(""))  // No check if no value has been entered.
            return false; // shouldn't it be false
          int val = Integer.parseInt(v);
          int arg = 0, arg2 = 0;
          if(condition!=COND_ANY) {
            if(condition == COND_RANGE) {
              int comma = argument.indexOf(",");
              arg =Integer.parseInt(argument.substring(0,comma));
              arg2=Integer.parseInt(argument.substring(comma+1));
            }
            else
              arg=Integer.parseInt(argument);
          }
          switch(condition) {
            case COND_EQ: return (val==arg);
            case COND_LE: return (val<=arg);
            case COND_LT: return (val<arg);
            case COND_GE: return (val>=arg);
            case COND_GT: return (val>arg);
            case COND_NE: return (val!=arg);
            case COND_RANGE: return ((val>=arg)&&(val<=arg2));
          }
        }
        catch(Exception e) {
          System.err.println("Error check int: "+e);
          return false;
        }
        break;
      case TYPE_STR: {
        if (condition == COND_RANGE) {
          String arg1 = null, arg2 = null;
          int comma = argument.indexOf(",");
          arg1 = argument.substring(0, comma);
          arg2 = argument.substring(comma + 1);
          return ( (v.compareTo(arg1) >= 0) && (v.compareTo(arg2) <= 0));
        }
        if (condition == COND_IN) {
          if (v.equals("")) // No check if no value has been entered.
            return true;
          Vector vargument = stringToVector(argument);
          if (vargument != null) {
            return vargument.contains(v);
          }
          else {
            return true;
          }
        }
        int val = v.compareTo(argument);
        switch (condition) {
          case COND_EQ:
            return val == 0;
          case COND_LE:
            return (val <= 0);
          case COND_LT:
            return (val < 0);
          case COND_GE:
            return (val >= 0);
          case COND_GT:
            return (val > 0);
          case COND_NE:
            return (val != 0);
        }
      }
      break;
      case TYPE_INSTANCE: {
        if (required && ! requiredAtBeginningOnly && v.equals("")) {
          // If required then we need a (correct) value
          return false;
        }
        // no value is considered true
        if (v.equals(""))
          return true;
        // now if it has a value, it must be the right one
        KFrame tmpframe = owner.rootFindFrame(v);
        if ( tmpframe == null ||
             ! (tmpframe.inParentList(owner.rootFindFrame(argument))
                || tmpframe == owner.rootFindFrame(argument))
             )
          return false;
        break;
      }
    }
    return true;
  }

  /**
   * Check condition for TYPE_LIST and COND_SUBSET OR COND_SUPERSET
   * @param vv the new vector
   * @return true if the condition matches
   */
  public boolean checkVValue(Vector vv) {
    System.err.println("checkVValue: NOT YET IMPLEMENTED");
    // case TYPE_LIST: {
    // check only for COND_IN, COND_COVER
    return true;
  }

  public static int findTypeValue(String name) {
    for(int i=0;i<TYPE_COUNT;i++)
      if(name.equals(type_varnames[i]))
        return i;
    return -1;
  }

  public static int findConditionValue(String name) {
    for(int i=0;i<COND_COUNT;i++)
      if(name.equals(cond_varnames[i]))
        return i;
    return -1;
  }
}