package spak;
import java.io.*;
import java.util.Hashtable;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class ZeroReader {
  KFrame rootFrame = null;
  Hashtable frameHash = new Hashtable();
  Hashtable childHash = new Hashtable();

  public ZeroReader(String filename) {
    byte data [] = new byte[1024000];
    int read=0, len=0;
    try {
      FileInputStream fi = new FileInputStream(filename);
      while(read>=0) {
        read=fi.read(data,len,10000);
        if(read>0)
          len+=read;
      }
      fi.close();
      System.out.println("File size: "+read);
    }
    catch (Exception e) {
      rootFrame = null;
      System.out.println("Error reading Zero file: "+e);
    }
    parseData(data, len);
  }

  public KFrame getRootNode() {
    return rootFrame;
  }

  private void parseData(byte data[], int size) {
    String str = new String(data, 0, size);
    ParseList pl = new ParseList(str);
    System.out.println("# Classes: "+pl.size());
    for(int i=0;i<pl.size();i++) {
      System.out.println(": "+i);
      parseClass(pl.elementAt(i));
    }
    buildChild((KFrame)frameHash.get("ROOT"));
    rootFrame = (KFrame)frameHash.get("ROOT");

    // All Knowledge bases must have the same root name
    try {
      rootFrame.setSlotValue("Name","Root");
    }
    catch(InvalidSlotValueException e) {}
  }

  private void buildChild(KFrame kf) {
    String clist = (String)childHash.get(kf.getName());
    ParseList pl = new ParseList(clist);
    for(int i=0;i<pl.size();i++) {
      KFrame child = (KFrame)frameHash.get(pl.elementAt(i));
      if(child!=null) {
        buildChild(child);
        kf.add(child);
      }
    }
  }

  private void parseClass(String str) {
    ParseList pl = new ParseList(str);

    String className = pl.elementAt(0);
    String classType = pl.elementAt(1);
    String parent    = pl.elementAt(2);
    String children  = pl.elementAt(3);
    System.out.println("Name: "+className);
    System.out.println("Type: "+classType);
    System.out.println("Parent: "+parent);
    System.out.println("Children: "+children);

    KFrame f = new KFrame(className,null);
    for(int i=7;i<pl.size();i++) {
      ParseList pSlot = new ParseList(pl.elementAt(i));
      if(pSlot.size()>4)
        f.addSlot(pSlot.elementAt(0),pSlot.elementAt(4));
      // else
      //  f.addSlot(pSlot.elementAt(0),"-NA-");
    }
    frameHash.put(className, f);
    childHash.put(className,children);
  }
}