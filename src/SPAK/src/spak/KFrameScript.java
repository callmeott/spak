package spak;
import org.mozilla.javascript.*;
import java.util.*;
import java.lang.Long;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class KFrameScript extends ScriptableObject {
  KFrame frame=null;

  /**
   * Returns the name of this JavaScript class, "KFrameScript".
   */
  public String getClassName() {
      return "KFrameScript";
  }

  /**
   * The Java constructor, also used to define the JavaScript constructor.
   */
  public KFrameScript(KFrame r) {
    frame = r;
    String[] names = { "version" };
//    try {
//        defineFunctionProperties(names, KFrameScript.class,
//                                       ScriptableObject.DONTENUM);
//    } catch (PropertyException e) {
//    }
  }

  /**
   * Defines the "dim" property by returning true if name is
   * equal to "dim".
   * <p>
   * Defines no other properties, i.e., returns false for
   * all other names.
   *
   * @param name the name of the property
   * @param start the object where lookup began
   */
  public boolean has(String name, Scriptable start) {
    return frame.getSlotList().getSlotHash().containsKey(name);
  }

  /**
   * Defines all numeric properties by returning true.
   *
   * @param index the index of the property
   * @param start the object where lookup began
   */
  public boolean has(int index, Scriptable start) {
      return false;
  }

  /**
   * Get the named property.
   * <p>
   * Handles the "dim" property and returns NOT_FOUND for all
   * other names.
   * @param name the property name
   * @param start the object where the lookup began
   */
  public Object get(String name, Scriptable start) {
    if(name.equals("die")) {
      frame.selfDelete();
      return NOT_FOUND;
    }
    if(name.equals("children_list")) {
      StringBuffer sb = new StringBuffer();
      Vector vc = frame.getChildren();
      for(int i=0;i<vc.size();i++) {
        sb.append(((KFrame)vc.elementAt(i)).getName());
        sb.append(" ");
      }
      return sb.toString();
    }
    if(name.equals("parent")) {
      // return the first parent
      return frame.getParentName();
    }
    if(name.equals("parents")) {
      // Should return array of all parents
      return frame.getParentFrames();
    }
    if(name.equals("getAge")) {
      long age = frame.getAge();
      return String.valueOf(age);
    }
    if(name.equals("getAgeSinceLastUpdate")) {
      long age = frame.getAgeSinceLastUpdate();
      return String.valueOf(age);
    }


    Slot sl = frame.getSlotList().getSlot(name);
    if(sl!=null) {
      switch(sl.getType()) {
        case Slot.TYPE_STR:
          return sl.getValue();
        case Slot.TYPE_INT:
          if(sl.getValue().equals(""))
            return new Integer(0);
          else
            return new Integer(sl.getValue());
        case Slot.TYPE_LIST:
          return sl.getValue();
        case Slot.TYPE_INSTANCE:
          KFrame kf = frame.rootFindFrame(sl.getValue());
          if ( kf == null ) {
            System.err.println("Error: frame "+sl.getValue()+" NOT FOUND!!");
            return null;
          }
          return kf.getKFrameScript();
        case Slot.TYPE_PROCEDURE:
          Context cx = Context.getCurrentContext();

          Object output=null;
          try {
//            output=cx.evaluateString(ApplicationFrame.se.getGlobal(),
//              sl.getValue(), "slotproc", 1, null);
          }
          catch(Exception e) {
            System.out.println("SlotProc: "+e);
          }
          return output;
      }
    }
    return NOT_FOUND;
  }

  /**
   * Get the indexed property.
   * <p>
   * Look up the element in the associated vector and return
   * it if it exists. If it doesn't exist, create it.<p>
   * @param index the index of the integral property
   * @param start the object where the lookup began
   */
  public Object get(int index, Scriptable start) {
    return NOT_FOUND;
  }

  /**
   * Set a named property.
   *
   * We do nothing here, so all properties are effectively read-only.
   */
  public void put(String name, Scriptable start, Object value) {
    Slot sl = frame.getSlotList().getSlot(name);
    if(sl!=null) {
      try {
        switch(sl.getType()) {
          case Slot.TYPE_STR:
            try {
              sl.setValue((String) value);
            } catch (Exception ee) {
              System.err.println("Exception in KFrameScript while setting slot " +sl.getName()+ " to "+value.toString());
              System.err.println(ee);
            }
            break;
          case Slot.TYPE_INT:
            if(value instanceof Double) {
              Double x = (Double)value;
              sl.setValue(Integer.toString(x.intValue()));
            }
            else if(value instanceof Integer) {
              sl.setValue(((Integer)value).toString());
            }
            break;
          case Slot.TYPE_INSTANCE:
            try {
              sl.setValue(( (KFrameScript) value).getFrameName());
            } catch(Exception e) {
              try {
                sl.setValue((String) value);
              } catch (Exception ee) {
                System.err.println("Exception in KFrameScript while setting slot " +sl.getName()+ " to "+value.toString());
                System.err.println(ee);
              }
            }
            break;
        }
      }
      catch(InvalidSlotValueException e) {
        System.err.println(e);
      }
      frame.updateHash();
    }
  }

  public String getFrameName() {
    return frame.getName();
  }

  /**
   * Set an indexed property.
   *
   * We do nothing here, so all properties are effectively read-only.
   */
  public void put(int index, Scriptable start, Object value) {
  }

  /**
   * Remove a named property.
   *
   * This method shouldn't even be called since we define all properties
   * as PERMANENT.
   */
  public void delete(String id) {
  }

  /**
   * Remove an indexed property.
   *
   * This method shouldn't even be called since we define all properties
   * as PERMANENT.
   */
  public void delete(int index) {
  }

  /**
   * Get properties.
   *
   * We return an empty array since we define all properties to be DONTENUM.
   */
  public Object[] getIds() {
      return new Object[0];
  }

  /**
   * Default value.
   *
   * Use the convenience method from Context that takes care of calling
   * toString, etc.
   */
  public Object getDefaultValue(Class typeHint) {
    String retval = "[Frame] Name="+frame.getName()+"\n";
    for(int i=0;i<frame.getSlotList().size();i++) {
      Slot sl = frame.getSlotList().slotAt(i);
      retval += (sl.getRequired()?" *":"  ")+sl.getName()+"="+sl.getValue()+"\n";
    }
    return retval;
  }

  /**
   * This function is provided for script user. The script object
   * can call this function like this  "r.findFrame(....)".
   */

  public Object jsFunction_findFrame(String fname) {
    return frame.findFrame(fname).getKFrameScript();
  }

  public void jsFunction_test() {
  }

  public static double version(Context cx, Scriptable thisObj,
                               Object[] args, Function funObj)
  {
      double result = (double) cx.getLanguageVersion();
      if (args.length > 0) {
          double d = cx.toNumber(args[0]);
          cx.setLanguageVersion((int) d);
      }
      return result;
  }


// The following is needed if this class "implements Scriptable" instead of
// "extends Scriptable Object". //Hui

//  /**
//   * Get parent.
//   */
//  public Scriptable getParentScope() {
//      return parent;
//  }
//
//  /**
//   * Set parent.
//   */
//  public void setParentScope(Scriptable parent) {
//      this.parent = parent;
//  }
//
//
//  /**
//   * Get prototype.
//   */
//  public Scriptable getPrototype() {
//      return prototype;
//  }
//
//  /**
//   * Set prototype.
//   */
//  public void setPrototype(Scriptable prototype) {
//      this.prototype = prototype;
//  }
//
//  public boolean hasInstance(Scriptable value) {
//      Scriptable proto = value.getPrototype();
//      while (proto != null) {
//          if (proto.equals(this))
//              return true;
//          proto = proto.getPrototype();
//      }
//
//      return false;
//  }
//
//  /**
//   * Some private data for this class.
//   */
//  private Scriptable prototype, parent;

}