package spak;
import javax.swing.table.*;

/**
 * <p>Title: SlotTableModel</p>
 * <p>Description: Define Table Model for Slot Editing Form</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 *
 * $Log: SlotTableModel.java,v $
 * Revision 1.2  2005-08-03 05:13:53  pattara
 * -add the onLoad to the list of special slots in SlotTableModel
 *
 * Revision 1.1  2005/08/02 07:30:34  pattara
 * -first import
 *
 * Revision 1.10  2005/04/08 10:23:46  pattara
 * -new slot flag: DF (Don't Fill). In case of the R (Required flag) is set,
 *  when inducing, if this flag is set, the inference engine will NOT try to
 *  find a value for it, otherwise it will help finding a value (default behavior)
 *
 * Revision 1.9  2005/03/04 04:53:36  pattara
 * -add IS_A special slots to every frames to show my parents
 *
 * Revision 1.8  2005/03/02 17:46:57  pattara
 * -Allow setting of slot value that violates the condition, assuming
 *  that the reinduce will be run to fix the frame. By allowing we mean
 *  it is set to the new value and an exception still occurs.
 * -In case of setting slot value from network (9900), the reinduce
 *  will be started automatically if such event happened.
 * -In case of editing in the knowledge editor, a message saying please
 *  reinduce is shown
 *
 * Revision 1.7  2004/11/25 11:19:39  pattara
 * -the new flag "Beginning Only" (related with Required) added
 *
 * Revision 1.6  2004/11/04 07:44:33  pattara
 * -add support for onDie
 *
 * Revision 1.5  2004/09/28 12:25:56  pattara
 * -onInstantiate now became onTry and onInstantiate
 *
 * Revision 1.4  2004/09/15 05:47:53  vuthi
 * Adjust column widths in Slot Editor
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

public class SlotTableModel extends AbstractTableModel {
  String columnNames[] = {"Name","Type","Value","Cond",
                       "Argument","R", "BO", "DF", "S","U"};
  SlotList sl;
  KFrame myFrame;

  public SlotTableModel(SlotList slist, KFrame kf) {
    sl=slist;
    myFrame = kf;

  }

  public String getColumnName(int col) {
      return columnNames[col];
  }

  public int getRowCount() {
    return sl.size();
  }

  public int getColumnCount() { return 10; }

  public Object getValueAt(int row, int col) {
    switch(col) {
      case 0: return sl.slotAt(row).getName();
      case 1: return sl.slotAt(row).getTypeName();
      case 2: return sl.slotAt(row).getValue();
      case 3: return sl.slotAt(row).getConditionName();
      case 4: return sl.slotAt(row).getArgument();
      case 5: return new Boolean(sl.slotAt(row).getRequired());
      case 6: return new Boolean(sl.slotAt(row).getRequiredAtBeginningOnly());
      case 7: return new Boolean(sl.slotAt(row).getDontFill());
      case 8: return new Boolean(sl.slotAt(row).getShared());
      case 9: return new Boolean(sl.slotAt(row).getUnique());
    }
    return null;
  }

  public boolean isCellEditable(int row, int col) {
    if(sl.slotAt(row).getOwner().equals(myFrame)) {
      // special slots beginning with _ are not editable
      if ( ((String) getValueAt(row,0)).indexOf("_") == 0 )
        return false;
      if(col==5) // Required is not editable for "Name"
        return row>0;
      if(col==6) // Required is needed
        return (row>0 && sl.slotAt(row).getRequired());
      if(col==7) // Required is needed
        return (row>0 && sl.slotAt(row).getRequired());
      if(col==8) // Shared is valid only for type Instance
        return sl.slotAt(row).getType()==Slot.TYPE_INSTANCE;
      if(col==9) // Unique is valid only for REQUIRED slots
        return ((row>0) &&
                sl.slotAt(row).getRequired());
      return true;
    }
    return false;
  }

  public void setValueAt(Object value, int row, int col) {
    // System.err.println("Object Type: "+value.getClass());
    try {
      if(col==0) {
        String nname = (String) value;
        sl.slotAt(row).setName(nname);
        // Unset attributes for special slot
        if (nname.equals("onInstantiate") ||
            nname.equals("onLoad") ||
            nname.equals("onTry") ||
            nname.equals("onBTry") ||
            nname.equals("onDie") ||
            nname.equals("onEvaluate") ||
            nname.equals("condition") ||
            nname.equals("onUpdate")) {
          Slot s=sl.slotAt(row);
          s.setShared(false);
          s.setRequired(false);
          s.setUnique(false);
        }
      }
      else if(col==1) {
        sl.slotAt(row).setTypeName( (String) value);

        // Shared has no meaning for non-Instance type
        if(sl.slotAt(row).getType()!=Slot.TYPE_INSTANCE)
          sl.slotAt(row).setShared(false);
      }
      else if(col==2)
        myFrame.setSlotValue(sl.slotAt(row).getName(),(String)value);
      else if(col==3)
        sl.slotAt(row).setConditionName((String)value);
      else if(col==4)
        sl.slotAt(row).setArgument((String)value);
      else if(col==5) {
        sl.slotAt(row).setRequired( ( (Boolean) value).booleanValue());

        // Unique has no meaning for Non-required slot
        if(!sl.slotAt(row).getRequired())
          sl.slotAt(row).setUnique(false);
      }
      else if(col==6)
        sl.slotAt(row).setRequiredAtBeginningOnly(((Boolean)value).booleanValue());
      else if(col==7)
        sl.slotAt(row).setDontFill(((Boolean)value).booleanValue());
      else if(col==8)
        sl.slotAt(row).setShared(((Boolean)value).booleanValue());
      else if(col==9)
        sl.slotAt(row).setUnique(((Boolean)value).booleanValue());
    }
    catch(InvalidSlotValueException e) {
      new MessageBox("Invalid slot value in "+e.getSource()+", please reinduce");
    }
    catch(ArgumentInvalidateSlotValueException e) {
      new MessageBox("New Argument invalidates slot value in "+e.getSource()+", please reinduce");
    }
    catch(ConditionInvalidateSlotValueException e) {
      new MessageBox("New Condition invalidates slot value in "+e.getSource()+", please reinduce");
    }
    fireTableRowsUpdated(row,row);
    // fireTableCellUpdated(row, col);
  }

  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }
}