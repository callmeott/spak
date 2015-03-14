package spak;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class InvalidSlotValueException extends Exception {
  String source = null;

  public InvalidSlotValueException(String slotName, String frameName) {
    super();
    source = frameName+":"+slotName;
  }

  public String getSource() {
    return source;
  }
}