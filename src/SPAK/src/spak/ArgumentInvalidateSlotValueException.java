package spak;

/**
 * <p>Title: ArgumentInvalidateSlotValueException</p>
 * <p>Description: Exception thrown when invalid slot value is detected.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 *
 * $Log: ArgumentInvalidateSlotValueException.java,v $
 * Revision 1.1  2005-08-02 07:30:34  pattara
 * -first import
 *
 * Revision 1.2  2004/07/28 04:59:20  vuthi
 * 1) Add file filter for .xml, .js, etc.
 * 2) Improve slot editor. Disable editing of non-related attributes.
 * 3) Implement UNIQUE attribute. Allow network client to update
 *     non-unique required values of existing Instances.
 * 4) Add/Implement "onUpdate" slot.
 *
 *
 */

public class ArgumentInvalidateSlotValueException extends Exception {
  String source=null;

  public ArgumentInvalidateSlotValueException(String slotName, String frameName) {
    super();
    source = frameName+":"+slotName;
  }

  public String getSource() {
    return source;
  }
}