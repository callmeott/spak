package spak;

import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;


/**
 * <p>Title: SPAKFileFilter</p>
 * <p>Description: File name filtering class for FileOpen Dialog</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: National Institute of Informatics</p>
 * @author Vuthichai
 * @version 1.0
 *
 * $Log: SPAKFileFilter.java,v $
 * Revision 1.1  2005-08-02 07:30:34  pattara
 * -first import
 *
 * Revision 1.1  2004/07/28 04:59:20  vuthi
 * 1) Add file filter for .xml, .js, etc.
 * 2) Improve slot editor. Disable editing of non-related attributes.
 * 3) Implement UNIQUE attribute. Allow network client to update
 *     non-unique required values of existing Instances.
 * 4) Add/Implement "onUpdate" slot.
 *
 */

public class SPAKFileFilter extends javax.swing.filechooser.FileFilter {
  Vector extList = new Vector();
  String desc = "SPAK Knowledge File";  // Default description

  public SPAKFileFilter() {
  }

  public SPAKFileFilter(String des) {
    desc = des;
  }

  public void addExtension(String ext) {
    extList.add(ext.toUpperCase());
  }

  public boolean accept(File f) {
    // Accept all Directories
    if (f.isDirectory()) {
        return true;
    }
    String filename = f.getName();
    int dot = filename.lastIndexOf('.');
    if(dot>=0) {
      String ext = filename.substring(dot+1).toUpperCase();
      return extList.contains(ext);
    }
    return false;
  }

  public String getDescription() {
    return desc;
  }

}